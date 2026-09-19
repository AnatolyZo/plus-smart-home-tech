package ru.yandex.practicum.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.inventory.entity.InventoryUnit;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryUnit, Long> {
    Optional<InventoryUnit> findByProductId(long productId);

    boolean existsByProductId(long productId);
}
