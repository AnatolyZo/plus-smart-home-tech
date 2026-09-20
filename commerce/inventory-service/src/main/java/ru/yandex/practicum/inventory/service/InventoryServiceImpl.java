package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.*;
import ru.yandex.practicum.inventory.entity.InventoryUnit;
import ru.yandex.practicum.inventory.exception.EntityAlreadyExistsException;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    @Override
    public List<InventoryDto> getAllInventoryUnits() {
        log.trace("Инициировано получение списка всех складских записей");
        List<InventoryUnit> inventoryUnits = inventoryRepository.findAll();
        log.debug("Получен список складских записей {}", inventoryUnits);
        return inventoryUnits.stream()
                .map(InventoryMapper::toInventoryDto)
                .toList();
    }

    @Override
    @Transactional
    public InventoryDto updateInventoryUnit(UpdateInventoryRequest request) {
        log.trace("Инициировано обновление складской записи");
        InventoryUnit inventoryUnit = downloadInventoryUnit(request.productId());
        
        int updatedAvailableQuantity = request.quantity() - inventoryUnit.getReservedQuantity();
        if (updatedAvailableQuantity < 0) {
            log.warn("Запрашиваемое изменение количества товара ({}) меньше зарезервированного количества товара ({}). Обновление отклонено. Для такого уменьшения товара следует отменить уже зарезервированные товары.", request.quantity(), inventoryUnit.getReservedQuantity());
            throw new InsufficientStockException(String.format("Запрашиваемое изменение количества товара (%d)" +
                    " меньше зарезервированного количества товара (%d). Обновление отклонено. Для такого уменьшения" +
                    " товара следует отменить уже зарезервированные товары.", request.quantity(), inventoryUnit.getReservedQuantity()));
        }
        
        inventoryUnit.setQuantity(request.quantity());
        inventoryUnit.setAvailableQuantity(updatedAvailableQuantity);
        InventoryUnit updatedInventoryUnit = inventoryRepository.save(inventoryUnit);
        log.debug("Обновлена складская запись {}", updatedInventoryUnit);
        return InventoryMapper.toInventoryDto(updatedInventoryUnit);
    }

    @Override
    @Transactional
    public InventoryDto createInventoryUnit(CreateInventoryRequest request) {
        log.trace("Инициировано создание складской записи");
        boolean isProductAlreadyExists = inventoryRepository.existsByProductId(request.productId());

        if (isProductAlreadyExists) {
            log.warn("Складская запись для товара с id {} уже существует, создание новой записи отклонено", request.productId());
            throw new EntityAlreadyExistsException(String.format("Складская запись для товара с id %d уже существует, создание новой записи отклонено", request.productId()));
        }

        InventoryUnit inventoryUnit = InventoryUnit.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .availableQuantity(request.quantity())
                .build();
        InventoryUnit savedInventoryUnit = inventoryRepository.save(inventoryUnit);
        log.debug("Создана складская запись {}", savedInventoryUnit);
        return InventoryMapper.toInventoryDto(savedInventoryUnit);
    }

    @Override
    @Transactional
    public ReserveResponse createProductReservation(ReserveRequest request) {
        log.trace("Инициировано резервирование товара");
        InventoryUnit inventoryUnit = downloadInventoryUnit(request.productId());

        if (request.quantity() > inventoryUnit.getAvailableQuantity()) {
            log.warn("Недостаточное количество товара с id {} на складе, резервирование отклонено", inventoryUnit.getProductId());
            throw new InsufficientStockException(String.format("Недостаточное количество товара с id %d на складе, резервирование отклонено", inventoryUnit.getProductId()));
        }

        inventoryUnit.setReservedQuantity(inventoryUnit.getReservedQuantity() + request.quantity());
        inventoryUnit.setAvailableQuantity(inventoryUnit.getAvailableQuantity() - request.quantity());
        InventoryUnit savedInventoryUnit = inventoryRepository.save(inventoryUnit);
        inventoryRepository.flush();
        log.debug("Зарезервирован товар {}, складская запись обновлена {}", request, savedInventoryUnit);
        return new ReserveResponse(true, savedInventoryUnit.getAvailableQuantity(), "Товар успешно зарезервирован");
    }

    @Override
    public InventoryDto getRemainingProductQuantities(long productId) {
        log.trace("Инициировано получение складских остатков товара");
        InventoryUnit inventoryUnit = downloadInventoryUnit(productId);
        log.debug("Получены складские остатки товара {} по productId {}", inventoryUnit, productId);
        return InventoryMapper.toInventoryDto(inventoryUnit);
    }

    private InventoryUnit downloadInventoryUnit(long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Складские остатки товара с id {} не найдены", productId);
                    return new NotFoundException(String.format("Складские остатки товара с id %d не найдены", productId));
                });
    }
}
