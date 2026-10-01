package ru.yandex.practicum.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.OrderStatuses;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.fallback.InventoryAdapter;
import ru.yandex.practicum.order.fallback.ProductAdapter;
import ru.yandex.practicum.order.fallback.RemoteCallResult;
import ru.yandex.practicum.order.feign.*;
import ru.yandex.practicum.order.service.OrderOrchestrationService;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderOrchestrationServiceTests {

    @Mock
    private OrderService orderService;
    @Mock
    private ProductAdapter productAdapter;
    @Mock
    private InventoryAdapter inventoryAdapter;

    @InjectMocks
    private OrderOrchestrationService orderOrchestrationService;

    @Test
    void createOrder_success() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(
                100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product));

        when(inventoryAdapter.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new RemoteCallResult.Success<>(null));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(new BigDecimal("39998.00"))
                .statusDetails("Подтверждён")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any(), eq(OrderStatuses.CONFIRMED)))
                .thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(productAdapter).getProductById(100L);
        verify(inventoryAdapter).reserveStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 2));
        verify(orderService).saveOrder(eq(request), argThat(map ->
                map.size() == 1 && map.containsKey(100L)), eq(OrderStatuses.CONFIRMED));
    }

    @Test
    void createOrder_sameProductMultipleItems_sumsQuantityForReservation() {
        OrderItemRequest item1 = new OrderItemRequest(100L, 2);
        OrderItemRequest item2 = new OrderItemRequest(100L, 3);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(item1, item2));

        ProductDto product = new ProductDto(
                100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product));

        when(inventoryAdapter.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new RemoteCallResult.Success<>(null));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(new BigDecimal("99995.00"))
                .statusDetails("Подтверждён")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any(), eq(OrderStatuses.CONFIRMED)))
                .thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(productAdapter, times(1)).getProductById(100L);
        verify(inventoryAdapter, times(1)).reserveStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 5));
    }

    @Test
    void createOrder_nonActiveProduct_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), false);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product));

        OrderProcessingException ex = assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        assertThat(ex.getMessage()).contains("100");
        assertThat(ex.getMessage()).contains("сняты с продажи");
        verify(inventoryAdapter, never()).reserveStock(any());
        verify(orderService, never()).saveOrder(any(), any(), any());
    }

    @Test
    void createOrder_productNotFound_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        when(productAdapter.getProductById(999L))
                .thenReturn(new RemoteCallResult.BusinessFailure<>("Товар не найден"));

        assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        verify(inventoryAdapter, never()).reserveStock(any());
        verify(orderService, never()).saveOrder(any(), any(), any());
    }

    @Test
    void createOrder_productServiceDown_returnsPendingConfirmation() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.TechnicalFailure<>("Сервис недоступен"));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(BigDecimal.ZERO)
                .statusDetails("Ожидает подтверждения")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any(), eq(OrderStatuses.PENDING_CONFIRMATION)))
                .thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(inventoryAdapter, never()).reserveStock(any());
        verify(orderService).saveOrder(eq(request), any(), eq(OrderStatuses.PENDING_CONFIRMATION));
    }

    @Test
    void createOrder_reserveBusinessFailure_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product));

        when(inventoryAdapter.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new RemoteCallResult.BusinessFailure<>("Недостаточно товара на складе"));

        OrderProcessingException ex = assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        assertThat(ex.getMessage()).contains("Ошибка резервирования");
        verify(orderService, never()).saveOrder(any(), any(), any());
    }

    @Test
    void createOrder_reserveTechnicalFailure_returnsPendingConfirmation() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product));

        when(inventoryAdapter.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new RemoteCallResult.TechnicalFailure<>("Сервис склада недоступен"));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(new BigDecimal("19999.00"))
                .statusDetails("Ожидает подтверждения")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any(), eq(OrderStatuses.PENDING_CONFIRMATION)))
                .thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(orderService).saveOrder(eq(request), argThat(map ->
                        map.get(100L).description().contains("требуется ручная проверка")),
                eq(OrderStatuses.PENDING_CONFIRMATION));
    }

    @Test
    void createOrder_reserveFails_rollsBackReservedProducts() {
        OrderItemRequest item1 = new OrderItemRequest(100L, 2);
        OrderItemRequest item2 = new OrderItemRequest(200L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(item1, item2));

        ProductDto product1 = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        ProductDto product2 = new ProductDto(200L, "Ноутбук", "Описание",
                new BigDecimal("50000.00"), true);
        when(productAdapter.getProductById(100L))
                .thenReturn(new RemoteCallResult.Success<>(product1));
        when(productAdapter.getProductById(200L))
                .thenReturn(new RemoteCallResult.Success<>(product2));

        when(inventoryAdapter.reserveStock(any(ReserveRequest.class)))
                .thenAnswer(inv -> {
                    ReserveRequest rr = inv.getArgument(0);
                    if (rr.productId() == 100L) {
                        return new RemoteCallResult.Success<>(null);
                    }
                    return new RemoteCallResult.BusinessFailure<>("Недостаточно товара");
                });

        when(inventoryAdapter.releaseStock(any(ReleaseRequest.class)))
                .thenReturn(new RemoteCallResult.Success<>(null));

        OrderProcessingException ex = assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        assertThat(ex.getMessage()).contains("Ошибка резервирования");

        verify(inventoryAdapter).releaseStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 2));
        verify(orderService, never()).saveOrder(any(), any(), any());
    }
}