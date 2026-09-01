package ru.practicum.telemetry.handlers.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.service.functions.Metrics;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements SnapshotHandler {
    @Override
    public Class<?> getSensorDataClass() {
        return SensorClasses.SWITCH_SENSOR.getSensorClass();
    }

    @Override
    public int handle(ConditionType conditionType, SensorStateAvro sensorsState) {
        SwitchSensorAvro data = (SwitchSensorAvro) sensorsState.getData();

        if (Metrics.SWITCH_SENSORS_METRICS.get(conditionType).apply(data)) {
            return 1;
        } else {
            return 0;
        }
    }
}
