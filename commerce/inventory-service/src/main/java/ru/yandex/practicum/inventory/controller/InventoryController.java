package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(InventoryController.URL_API + InventoryController.URL_INVENTORY)
public class InventoryController {
    public static final String URL_API = "/api";
    public static final String URL_INVENTORY = "/inventory";
    public static final String URL_RESERVE = "/reserve";
    public static final String ID_PRODUCT = "productId";
    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryDto> getAllInventoryUnits() {
        return inventoryService.getAllInventoryUnits();
    }

    @PutMapping
    public InventoryDto updateInventoryUnit(@Valid @RequestBody UpdateInventoryRequest request) {
        return inventoryService.updateInventoryUnit(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto createInventoryUnit(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.createInventoryUnit(request);
    }

    @PostMapping(URL_RESERVE)
    public ReserveResponse createProductReservation(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.createProductReservation(request);
    }

    @GetMapping("/{" + ID_PRODUCT + "}")
    public InventoryDto getRemainingProductQuantities(@PathVariable long productId) {
        return inventoryService.getRemainingProductQuantities(productId);
    }
}
