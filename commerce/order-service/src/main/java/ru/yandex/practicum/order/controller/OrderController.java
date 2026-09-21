package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(OrderController.URL_API + OrderController.URL_ORDERS)
public class OrderController {
    public static final String URL_API = "/api";
    public static final String URL_ORDERS = "/orders";
    public static final String URL_BY_EMAIL = "/by-email";
    public static final String ID = "id";
    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{" + ID + "}")
    public OrderDto getOrderById(@PathVariable(name = ID) long orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping(URL_BY_EMAIL)
    public List<OrderDto> getCustomersOrders(@RequestParam String email) {
        return orderService.getCustomersOrders(email);
    }
}
