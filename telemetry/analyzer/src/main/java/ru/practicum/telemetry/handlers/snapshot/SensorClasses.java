package ru.practicum.telemetry.handlers.snapshot;

import lombok.Getter;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Getter
public enum SensorClasses {
    CLIMATE_SENSOR(ClimateSensorAvro.class),
    LIGHT_SENSOR(LightSensorAvro.class),
    MOTION_SENSOR(MotionSensorAvro.class),
    SWITCH_SENSOR(SwitchSensorAvro.class),
    TEMPERATURE_SENSOR(TemperatureSensorAvro.class);

    private final Class<?> sensorClass;

    SensorClasses(Class<?> sensorClass) {
        this.sensorClass = sensorClass;
    }
}
