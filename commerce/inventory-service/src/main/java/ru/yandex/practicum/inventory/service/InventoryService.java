package ru.yandex.practicum.inventory.service;

import ru.yandex.practicum.inventory.dto.*;

import java.util.List;

public interface InventoryService {
    List<InventoryDto> getAllInventoryUnits();

    InventoryDto updateInventoryUnit(UpdateInventoryRequest request);

    InventoryDto createInventoryUnit(CreateInventoryRequest request);

    ReserveResponse createProductReservation(ReserveRequest request);

    InventoryDto getRemainingProductQuantities(long productId);

    ReserveResponse releaseProductReservation(ReleaseRequest request);
}
