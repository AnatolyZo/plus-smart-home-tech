package ru.yandex.practicum.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(Long productId, Throwable cause) {
        super(String.format("Сервис склада недоступен, резерв товара с id %d невозможен", productId), cause);
    }
}
