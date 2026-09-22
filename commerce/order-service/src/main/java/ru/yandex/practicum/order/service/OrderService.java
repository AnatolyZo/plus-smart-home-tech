package ru.yandex.practicum.order.service;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.feign.ProductDto;

import java.util.List;
import java.util.Map;

public interface OrderService {
    List<OrderDto> getAllOrders();

    OrderDto saveOrder(CreateOrderRequest request, Map<Long, ProductDto> productsMap);

    OrderDto getOrderById(long orderId);

    List<OrderDto> getCustomersOrders(String email);
}
