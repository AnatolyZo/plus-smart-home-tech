package ru.yandex.practicum.inventory.mapper;

import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.entity.InventoryUnit;

public class InventoryMapper {
    public static InventoryDto toInventoryDto(InventoryUnit inventoryUnit) {
        return InventoryDto.builder()
                .id(inventoryUnit.getId())
                .productId(inventoryUnit.getProductId())
                .quantity(inventoryUnit.getQuantity())
                .reservedQuantity(inventoryUnit.getReservedQuantity())
                .availableQuantity(inventoryUnit.getAvailableQuantity())
                .build();
    }

    public static InventoryUnit toInventoryUnit(ReserveRequest request) {
        return InventoryUnit.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .availableQuantity(request.quantity())
                .build();
    }
}
