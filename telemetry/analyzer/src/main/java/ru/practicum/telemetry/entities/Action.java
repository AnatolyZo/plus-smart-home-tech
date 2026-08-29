package ru.practicum.telemetry.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.telemetry.enums.ActionType;

@Entity
@Table(name = "actions", schema = "public")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType type;

    private Integer value;
}
