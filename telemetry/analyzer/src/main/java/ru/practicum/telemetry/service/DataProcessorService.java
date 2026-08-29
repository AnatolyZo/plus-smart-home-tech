package ru.practicum.telemetry.service;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

public interface DataProcessorService {
    void addScenario(String hubId, ScenarioAddedEventAvro payload);

    void removeScenario(String hubId, ScenarioRemovedEventAvro payload);

    void addSensor(String hubId, DeviceAddedEventAvro payload);

    void removeSensor(DeviceRemovedEventAvro payload, String hubId);
}
