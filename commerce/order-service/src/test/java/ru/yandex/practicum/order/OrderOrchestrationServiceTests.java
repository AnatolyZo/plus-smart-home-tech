package ru.yandex.practicum.order;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.exception.OrderProcessingException;
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
    private ProductClient productClient;
    @Mock
    private InventoryClient inventoryClient;

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
        when(productClient.getProductById(100L)).thenReturn(product);

        when(inventoryClient.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new ReserveResponse(100L, 2, 1));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(new BigDecimal("39998.00"))
                .statusDetails("Ожидает резервации")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any()))
                .thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(productClient).getProductById(100L);
        verify(inventoryClient).reserveStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 2));
        verify(orderService).saveOrder(eq(request), argThat(map ->
                map.size() == 1 && map.containsKey(100L)));
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
        when(productClient.getProductById(100L)).thenReturn(product);

        when(inventoryClient.reserveStock(any(ReserveRequest.class)))
                .thenReturn(new ReserveResponse(100L, 5, 1));

        OrderDto expectedDto = OrderDto.builder()
                .id(1L)
                .customerName("Иван Иванов")
                .customerEmail("user@test.com")
                .totalPrice(new BigDecimal("99995.00"))
                .statusDetails(null)
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.saveOrder(eq(request), any())).thenReturn(expectedDto);

        OrderDto result = orderOrchestrationService.createOrder(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(productClient, times(1)).getProductById(100L);
        verify(inventoryClient, times(1)).reserveStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 5));
    }

    @Test
    void createOrder_nonActiveProduct_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), false);
        when(productClient.getProductById(100L)).thenReturn(product);

        OrderProcessingException ex = assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        assertThat(ex.getMessage()).contains("100");
        assertThat(ex.getMessage()).contains("сняты с продажи");
        verify(inventoryClient, never()).reserveStock(any());
        verify(orderService, never()).saveOrder(any(), any());
    }

    @Test
    void createOrder_productNotFound_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(productClient.getProductById(999L)).thenThrow(notFound);

        assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        verify(inventoryClient, never()).reserveStock(any());
        verify(orderService, never()).saveOrder(any(), any());
    }

    @Test
    void createOrder_reserveNotFound_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest(100L, 1);
        CreateOrderRequest request = new CreateOrderRequest(
                "user@test.com", "Иван Иванов", List.of(itemRequest));

        ProductDto product = new ProductDto(100L, "Смартфон", "Описание",
                new BigDecimal("19999.00"), true);
        when(productClient.getProductById(100L)).thenReturn(product);

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(inventoryClient.reserveStock(any(ReserveRequest.class))).thenThrow(notFound);

        assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        verify(orderService, never()).saveOrder(any(), any());
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
        when(productClient.getProductById(100L)).thenReturn(product1);
        when(productClient.getProductById(200L)).thenReturn(product2);

        FeignException feignException = mock(FeignException.InternalServerError.class);
        when(inventoryClient.reserveStock(any(ReserveRequest.class))).thenAnswer(inv -> {
            ReserveRequest rr = inv.getArgument(0);
            if (rr.productId() == 200L) {
                throw feignException;
            }
            return null;
        });

        OrderProcessingException ex = assertThrows(OrderProcessingException.class,
                () -> orderOrchestrationService.createOrder(request));

        assertThat(ex.getMessage()).contains("Ошибка резервирования товаров");

        verify(inventoryClient).releaseStock(argThat(rr ->
                rr.productId() == 100L && rr.quantity() == 2));
        verify(orderService, never()).saveOrder(any(), any());
    }
}