package ru.practicum.telemetry.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.service.DataProcessorService;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final DataProcessorService dataProcessorService;

    @Override
    public Class<?> getPayloadClass() {
        return HubEventClasses.REMOVE_DEVICE.getEventClass();
    }


    @Override
    public void handle(Object payload) {
    }
}
