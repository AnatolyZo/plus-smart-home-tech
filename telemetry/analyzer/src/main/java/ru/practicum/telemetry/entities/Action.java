package ru.practicum.telemetry.entities;

import jakarta.persistence.*;
import ru.practicum.telemetry.enums.ActionType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actions", schema = "public")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType type;

    private Integer value;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL)
    private List<ScenarioAction> scenarioActions = new ArrayList<>();
}
