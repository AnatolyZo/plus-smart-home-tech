package ru.practicum.telemetry.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scenario_actions")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @MapsId("scenarioId")
    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @MapsId("actionId")
    @ManyToOne
    @JoinColumn(name = "action_id")
    private Action action;
}
