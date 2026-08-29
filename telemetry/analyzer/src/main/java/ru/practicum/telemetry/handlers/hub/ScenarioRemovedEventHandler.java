package ru.practicum.telemetry.handlers.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.service.DataProcessorService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final DataProcessorService dataProcessorService;

    @Override
    public Class<?> getPayloadClass() {
        return HubEventClasses.REMOVE_SCENARIO.getEventClass();
    }

    @Override
    public void handle(Object hubEvent) {
        HubEventAvro event = (HubEventAvro) hubEvent;
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) event.getPayload();
        dataProcessorService.removeScenario(event.getHubId(), payload);
    }
}
