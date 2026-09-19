package ru.yandex.practicum.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_units", schema = "public")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long productId;

    private int quantity;

    @Builder.Default
    private int reservedQuantity = 0;

    private int availableQuantity;

    @Version
    private Long version;
}
