package ru.practicum.telemetry.handlers.snapshot;

import ru.practicum.telemetry.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface SnapshotHandler {
    Class<?> getSensorDataClass();

    int handle(ConditionType conditionType, SensorStateAvro state);
}
