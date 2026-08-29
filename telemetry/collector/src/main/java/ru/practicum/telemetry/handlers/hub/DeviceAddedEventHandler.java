package ru.practicum.telemetry.handlers.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.KafkaSenderService;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.mapper.HubMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final KafkaSenderService kafkaSenderService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        HubEvent hubEvent = HubMapper.mapToDeviceAddedEvent(event);
        kafkaSenderService.processHubEvent(hubEvent);
    }
}
