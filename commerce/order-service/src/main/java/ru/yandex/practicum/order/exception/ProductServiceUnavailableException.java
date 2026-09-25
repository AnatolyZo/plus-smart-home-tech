package ru.yandex.practicum.order.exception;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(Long productId, Throwable cause) {
        super(String.format("Сервис каталога товаров недоступен, загрузка данных товара с id %d невозможна", productId), cause);
    }
}
