package ru.yandex.practicum.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.InventoryUnit;
import ru.yandex.practicum.inventory.exception.EntityAlreadyExistsException;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.repository.InventoryRepository;
import ru.yandex.practicum.inventory.service.InventoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTests {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void getAllInventoryUnits_returnsList() {
        InventoryUnit unit1 = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(10)
                .availableQuantity(40)
                .build();

        InventoryUnit unit2 = InventoryUnit.builder()
                .id(2L)
                .productId(200L)
                .quantity(30)
                .reservedQuantity(0)
                .availableQuantity(30)
                .build();

        when(inventoryRepository.findAll()).thenReturn(List.of(unit1, unit2));

        List<InventoryDto> result = inventoryService.getAllInventoryUnits();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).productId()).isEqualTo(100L);
        assertThat(result.get(0).availableQuantity()).isEqualTo(40);
        assertThat(result.get(1).productId()).isEqualTo(200L);
        verify(inventoryRepository).findAll();
    }

    @Test
    void getAllInventoryUnits_emptyList() {
        when(inventoryRepository.findAll()).thenReturn(List.of());

        List<InventoryDto> result = inventoryService.getAllInventoryUnits();

        assertThat(result).isEmpty();
        verify(inventoryRepository).findAll();
    }


    @Test
    void updateInventoryUnit_success() {
        InventoryUnit existing = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(10)
                .availableQuantity(40)
                .build();

        UpdateInventoryRequest request = new UpdateInventoryRequest(100L, 60);

        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(InventoryUnit.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryDto result = inventoryService.updateInventoryUnit(request);

        assertThat(result.quantity()).isEqualTo(60);
        assertThat(result.availableQuantity()).isEqualTo(50);
        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(InventoryUnit.class));
    }

    @Test
    void updateInventoryUnit_throwsWhenQuantityLessThanReserved() {
        InventoryUnit existing = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(30)
                .availableQuantity(20)
                .build();

        UpdateInventoryRequest request = new UpdateInventoryRequest(100L, 20);

        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.updateInventoryUnit(request));

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void updateInventoryUnit_throwsWhenNotFound() {
        UpdateInventoryRequest request = new UpdateInventoryRequest(999L, 100);

        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> inventoryService.updateInventoryUnit(request));

        verify(inventoryRepository).findByProductId(999L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventoryUnit_success() {
        ReserveRequest request = new ReserveRequest(100L, 50);

        when(inventoryRepository.existsByProductId(100L)).thenReturn(false);
        when(inventoryRepository.save(any(InventoryUnit.class))).thenAnswer(inv -> {
            InventoryUnit unit = inv.getArgument(0);
            unit.setId(1L);
            return unit;
        });

        InventoryDto result = inventoryService.createInventoryUnit(request);

        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.quantity()).isEqualTo(50);
        assertThat(result.availableQuantity()).isEqualTo(50);
        verify(inventoryRepository).existsByProductId(100L);
        verify(inventoryRepository).save(any(InventoryUnit.class));
    }

    @Test
    void createInventoryUnit_throwsWhenAlreadyExists() {
        ReserveRequest request = new ReserveRequest(100L, 50);

        when(inventoryRepository.existsByProductId(100L)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> inventoryService.createInventoryUnit(request));

        verify(inventoryRepository).existsByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createProductReservation_success() {
        InventoryUnit existing = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(0)
                .availableQuantity(50)
                .build();

        ReserveRequest request = new ReserveRequest(100L, 10);

        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(InventoryUnit.class))).thenAnswer(inv -> inv.getArgument(0));

        ReserveResponse result = inventoryService.createProductReservation(request);

        assertThat(result.success()).isTrue();
        assertThat(result.availableQuantity()).isEqualTo(40);
        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(InventoryUnit.class));
        verify(inventoryRepository).flush();
    }

    @Test
    void createProductReservation_throwsWhenInsufficientStock() {
        InventoryUnit existing = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(0)
                .availableQuantity(5)
                .build();

        ReserveRequest request = new ReserveRequest(100L, 10);

        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.createProductReservation(request));

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createProductReservation_throwsWhenNotFound() {
        ReserveRequest request = new ReserveRequest(999L, 10);

        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> inventoryService.createProductReservation(request));

        verify(inventoryRepository).findByProductId(999L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void getRemainingProductQuantities_success() {
        InventoryUnit unit = InventoryUnit.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(10)
                .availableQuantity(40)
                .build();

        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(unit));

        InventoryDto result = inventoryService.getRemainingProductQuantities(100L);

        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.availableQuantity()).isEqualTo(40);
        verify(inventoryRepository).findByProductId(100L);
    }

    @Test
    void getRemainingProductQuantities_throwsWhenNotFound() {
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> inventoryService.getRemainingProductQuantities(999L));

        verify(inventoryRepository).findByProductId(999L);
    }
}