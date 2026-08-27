package ru.practicum.telemetry.entities;

import jakarta.persistence.*;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conditions", schema = "public")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    private ConditionOperation operation;

    private Integer value;

    @OneToMany(mappedBy = "condition", cascade = CascadeType.ALL)
    private List<ScenarioCondition> scenarioConditions = new ArrayList<>();
}
