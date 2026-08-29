package ru.practicum.telemetry.handlers.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.service.functions.Metrics;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SnapshotHandler {
    @Override
    public Class<?> getSensorDataClass() {
        return SensorClasses.CLIMATE_SENSOR.getSensorClass();
    }

    @Override
    public int handle(ConditionType conditionType, SensorStateAvro sensorsState) {
        ClimateSensorAvro data = (ClimateSensorAvro) sensorsState.getData();
        return Metrics.CLIMATE_SENSORS_METRICS.get(conditionType).apply(data);
    }
}
