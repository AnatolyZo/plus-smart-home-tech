package ru.practicum.telemetry.handlers.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.service.functions.Metrics;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SnapshotHandler {
    @Override
    public Class<?> getSensorDataClass() {
        return SensorClasses.MOTION_SENSOR.getSensorClass();
    }

    @Override
    public int handle(ConditionType conditionType, SensorStateAvro sensorsState) {
        MotionSensorAvro data = (MotionSensorAvro) sensorsState.getData();

        if (Metrics.MOTION_SENSORS_METRICS.get(conditionType).apply(data)) {
            return 1;
        } else {
            return 0;
        }
    }
}
