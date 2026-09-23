package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {
    private final OrderService orderService;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderDto createOrder(CreateOrderRequest request) {
        log.trace("Инициировано создание заказа");

        Map<Long, ProductDto> productsMap = getUniqueProducts(request);
        log.debug("Создана карта товаров {}", productsMap);

        List<Long> nonActiveProducts = findNonActiveProducts(productsMap);

        if (!nonActiveProducts.isEmpty()) {
            String ids = nonActiveProducts.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            log.warn("Товары с id: {} сняты с продажи.", ids);
            throw new OrderProcessingException(String.format("Товары с id: %s сняты с продажи.", ids));
        }

        reserveProducts(request);

        return orderService.saveOrder(request, productsMap);
    }

    private Map<Long, ProductDto> getUniqueProducts(CreateOrderRequest request) {
        return request.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getProduct
                ));
    }

    private List<Long> findNonActiveProducts(Map<Long, ProductDto> productsMap) {
        return productsMap.values().stream()
                .filter(product -> !product.active())
                .map(ProductDto::id)
                .toList();
    }

    private void reserveProducts(CreateOrderRequest request) {
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
                        reserveProduct(reserveRequest);
                        reservedOrders.add(reserveRequest);
                    });
        } catch (FeignException e) {
            rollbackReserve(reservedOrders);
            log.warn("Ошибка резервирования товаров");
            throw new OrderProcessingException("Ошибка резервирования товаров");
        }

    }

    private ProductDto getProduct(long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            log.warn("Товар с id {} не найден", productId);
            throw new OrderProcessingException(String.format("Товар с id %d не найден", productId));
        }
    }

    private void reserveProduct(ReserveRequest request) {
        try {
            inventoryClient.reserveStock(request);
        } catch (FeignException.ServiceUnavailable e) {
            log.warn("Сервис резервирования недоступен");
            throw new OrderProcessingException("Сервис резервирования недоступен");
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
        try {
            inventoryClient.releaseStock(request);
        } catch (FeignException.NotFound e) {
            log.warn("Складская запись при снятии резерва не найдена");
            throw new OrderProcessingException("Складская запись при снятии резерва не найдена");
        }
    }
}
