package ru.practicum.telemetry.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensors", schema = "public")
public class Sensor {
    @Id
    private String id;

    private String hubId;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    private List<ScenarioCondition> scenarioConditions = new ArrayList<>();

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    private List<ScenarioAction> scenarioActions = new ArrayList<>();
}
