package ru.practicum.telemetry.service;

import ru.yandex.practicum.kafka.telemetry.event.*;

public interface SnapshotProcessorService {
    void processSnapshot(SensorsSnapshotAvro snapshot);
}
