package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.feign.ProductDto;
import ru.yandex.practicum.order.mapper.OrderItemMapper;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<OrderDto> getAllOrders() {
        log.trace("Инициировано получение списка всех заказов");
        List<Order> orders = orderRepository.findAll();
        log.debug("Получен список заказов {}", orders);
        return formOrderDtoList(orders);
    }

    @Override
    @Transactional
    public OrderDto saveOrder(CreateOrderRequest request, Map<Long, ProductDto> productsMap) {
        log.trace("Инициировано сохранение заказа");
        Order order = formOrder(request, productsMap);
        Order savedOrder = orderRepository.save(order);
        log.debug("Сохранен заказ {}", savedOrder);
        List<OrderItemDto> savedOrderItemDtos = formOrderItemDtoList(savedOrder);
        return OrderMapper.toOrderDto(savedOrder, savedOrderItemDtos);
    }

    @Override
    public OrderDto getOrderById(long orderId) {
        log.trace("Инициировано получение заказа по id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Заказ с id {} не найден", orderId);
                    return new NotFoundException(String.format("Заказ с id %d не найден", orderId));
                });
        log.debug("Получен заказ {} по orderId {}", order, orderId);
        List<OrderItemDto> orderItemDtos = formOrderItemDtoList(order);
        return OrderMapper.toOrderDto(order, orderItemDtos);
    }

    @Override
    public List<OrderDto> getCustomersOrders(String email) {
        log.trace("Инициировано получение заказов по email");
        List<Order> orders = orderRepository.findByCustomerEmail(email);
        log.debug("Получен список заказов {} по email {}", orders, email);
        return formOrderDtoList(orders);
    }

    private Order formOrder(CreateOrderRequest request, Map<Long, ProductDto> productsMap) {
        Order order = OrderMapper.toOrder(request);

        //Установление связей: каждому OrderItem добавляем Order и формируем список из OrderItem
        for (OrderItemRequest itemRequest : request.items()) {
            ProductDto productDto = productsMap.get(itemRequest.productId());
            OrderItem orderItem = OrderItemMapper.toOrderItem(itemRequest);
            orderItem.setProductName(productDto.name());
            orderItem.setPrice(productDto.price());
            order.addItem(orderItem);
        }

        BigDecimal totalPrice = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);

        return order;
    }

    private List<OrderDto> formOrderDtoList(List<Order> orders) {
        return orders.stream()
                .map(order -> {
                    List<OrderItemDto> orderItems = formOrderItemDtoList(order);
                    return OrderMapper.toOrderDto(order, orderItems);
                })
                .toList();
    }

    private List<OrderItemDto> formOrderItemDtoList(Order order) {
        return order.getItems().stream()
                .map(OrderItemMapper::toOrderItemDto)
                .toList();
    }
}
