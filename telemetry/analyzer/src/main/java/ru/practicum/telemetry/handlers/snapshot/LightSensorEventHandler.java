package ru.practicum.telemetry.handlers.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.service.functions.Metrics;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements SnapshotHandler {
    @Override
    public Class<?> getSensorDataClass() {
        return SensorClasses.LIGHT_SENSOR.getSensorClass();
    }

    @Override
    public int handle(ConditionType conditionType, SensorStateAvro sensorsState) {
        LightSensorAvro data = (LightSensorAvro) sensorsState.getData();
        return Metrics.LIGHT_SENSORS_METRICS.get(conditionType).apply(data);
    }
}
