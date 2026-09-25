package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.OrderStatuses;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.fallback.InventoryAdapter;
import ru.yandex.practicum.order.fallback.ProductAdapter;
import ru.yandex.practicum.order.fallback.RemoteCallResult;
import ru.yandex.practicum.order.feign.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {
    private final OrderService orderService;
    private final ProductAdapter productAdapter;
    private final InventoryAdapter inventoryAdapter;

    public OrderDto createOrder(CreateOrderRequest request) {
        log.trace("Инициировано создание заказа");
        AtomicBoolean isProductServiceDown = new AtomicBoolean(false);
        AtomicBoolean isInventoryServiceDown = new AtomicBoolean(false);

        Map<Long, ProductDto> productsMap = getUniqueProducts(request, isProductServiceDown);
        log.debug("Создана карта товаров {}", productsMap);

        if (isProductServiceDown.get()) {
            log.info("Заказ будет создан со статусом PENDING_CONFIRMATION");
            return orderService.saveOrder(request, productsMap, OrderStatuses.PENDING_CONFIRMATION);
        }

        List<Long> nonActiveProducts = findNonActiveProducts(productsMap);

        if (!nonActiveProducts.isEmpty()) {
            String ids = nonActiveProducts.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            log.warn("Товары с id: {} сняты с продажи.", ids);
            throw new OrderProcessingException(String.format("Товары с id: %s сняты с продажи.", ids));
        }

        reserveProducts(request, isInventoryServiceDown);

        if (isInventoryServiceDown.get()) {
            log.info("Заказ будет создан со статусом PENDING_CONFIRMATION");

            Map<Long, ProductDto> updatedMap = productsMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().withDescription(
                                    entry.getValue().description() + "Резервирование не удалось, требуется ручная проверка"
                            )
                    ));

            return orderService.saveOrder(request, updatedMap, OrderStatuses.PENDING_CONFIRMATION);
        }

        return orderService.saveOrder(request, productsMap, OrderStatuses.CONFIRMED);
    }

    private Map<Long, ProductDto> getUniqueProducts(CreateOrderRequest request, AtomicBoolean pendingConfirmation) {
        return request.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        productId -> getProduct(productId, pendingConfirmation)
                ));
    }

    private List<Long> findNonActiveProducts(Map<Long, ProductDto> productsMap) {
        return productsMap.values().stream()
                .filter(product -> !product.active())
                .map(ProductDto::id)
                .toList();
    }

    private void reserveProducts(CreateOrderRequest request, AtomicBoolean isInventoryServiceDown) {
        List<ReserveRequest> reservedOrders = new ArrayList<>();

        try {
            request.items().stream()
                    .collect(Collectors.groupingBy(
                            OrderItemRequest::productId,
                            Collectors.summingInt(OrderItemRequest::quantity)
                    ))
                    .entrySet().stream()
                    .map(entry -> new ReserveRequest(entry.getKey(), entry.getValue()))
                    .forEach(reserveRequest -> {
                        reserveProduct(reserveRequest, isInventoryServiceDown);

                        if (isInventoryServiceDown.get()) {
                            throw new OrderProcessingException("Сервис резервирования недоступен");
                        }

                        reservedOrders.add(reserveRequest);
                    });
        } catch (OrderProcessingException e) {
            if (!isInventoryServiceDown.get()) {
                rollbackReserve(reservedOrders);
                log.warn("Ошибка резервирования товаров");
                throw e;
            }

            log.warn("Сервис резервирования недоступен, откатываем зарезервированное");
        }

    }

    private ProductDto getProduct(long productId, AtomicBoolean isProductServiceDown) {
        RemoteCallResult<ProductDto> result = productAdapter.getProductById(productId);

        return switch (result) {
            case RemoteCallResult.Success<ProductDto> s -> s.value();
            case RemoteCallResult.BusinessFailure<ProductDto> bf -> {
                log.warn("Товар с id {} не найден: {}", productId, bf.message());
                throw new OrderProcessingException(
                        String.format("Товар с id %d не найден", productId));
            }
            case RemoteCallResult.TechnicalFailure<ProductDto> tf -> {
                log.warn("Сервис товаров недоступен: {}", tf.reason());
                isProductServiceDown.set(true);
                yield new ProductDto(
                        productId,
                        String.format("Товар #%d (ожидает проверки)", productId),
                        "Требуется ручная проверка",
                        BigDecimal.ZERO,
                        false
                );
            }
        };
    }

    private void reserveProduct(ReserveRequest request, AtomicBoolean isInventoryServiceDown) {
        RemoteCallResult<Void> result = inventoryAdapter.reserveStock(request);

        switch (result) {
            case RemoteCallResult.Success<Void> s -> {}
            case RemoteCallResult.BusinessFailure<?> bf -> {
                log.warn("Бизнес-отказ при резервировании: {}", bf.message());
                throw new OrderProcessingException("Ошибка резервирования: " + bf.message());
            }
            case RemoteCallResult.TechnicalFailure<?> tf -> {
                log.warn("Технический сбой при резервировании: {}", tf.reason());
                isInventoryServiceDown.set(true);
            }
        }
    }

    private void rollbackReserve(List<ReserveRequest> reservedOrders) {
        reservedOrders.stream()
                .map(reserveResponse -> new ReleaseRequest(
                        reserveResponse.productId(),
                        reserveResponse.quantity()
                ))
                .forEach(this::releaseProduct);
    }

    private void releaseProduct(ReleaseRequest request) {
        RemoteCallResult<Void> result = inventoryAdapter.releaseStock(request);

        switch (result) {
            case RemoteCallResult.Success<Void> s -> {}
            case RemoteCallResult.BusinessFailure<?> bf ->
                    log.warn("Бизнес-отказ при снятии резерва: {}", bf.message());
            case RemoteCallResult.TechnicalFailure<?> tf ->
                    log.warn("Технический сбой при снятии резерва: {}", tf.reason());
        }
    }
}
