package ru.practicum.telemetry.sensors;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class TemperatureSensorEvent extends SensorEvent {
    private String id;
    private String hubId;
    private Instant timestamp;
    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}
