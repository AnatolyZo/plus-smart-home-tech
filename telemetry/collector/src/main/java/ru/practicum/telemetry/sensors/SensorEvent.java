package ru.practicum.telemetry.sensors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class SensorEvent {
    private String id;
    private String hubId;
    private Instant timestamp = Instant.now();

    public abstract SensorEventType getType();
}
