package ru.yandex.practicum.order.mapper;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderStatuses;
import ru.yandex.practicum.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public class OrderMapper {
    public static OrderDto toOrderDto(Order order, List<OrderItemDto> itemDtos) {
        return OrderDto.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .statusDetails(order.getStatusDetails())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }

    public static Order toOrder(CreateOrderRequest request, OrderStatuses status) {
        return Order.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .status(status.name())
                .statusDetails("Создан новый заказ")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
