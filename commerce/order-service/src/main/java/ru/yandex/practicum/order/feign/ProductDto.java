package ru.yandex.practicum.order.feign;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean active
) {
    public ProductDto withDescription(String newDescription) {
        return new ProductDto(id, name, newDescription, price, active);
    }
}
