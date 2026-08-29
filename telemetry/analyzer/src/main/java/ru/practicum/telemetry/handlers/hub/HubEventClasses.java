package ru.practicum.telemetry.handlers.hub;

import lombok.Getter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Getter
public enum HubEventClasses {
    ADD_DEVICE(DeviceAddedEventAvro.class),
    REMOVE_DEVICE(DeviceRemovedEventAvro.class),
    ADD_SCENARIO(ScenarioAddedEventAvro.class),
    REMOVE_SCENARIO(ScenarioRemovedEventAvro.class);

    private final Class<?> eventClass;

    HubEventClasses(Class<?> eventClass) {
        this.eventClass = eventClass;
    }
}
