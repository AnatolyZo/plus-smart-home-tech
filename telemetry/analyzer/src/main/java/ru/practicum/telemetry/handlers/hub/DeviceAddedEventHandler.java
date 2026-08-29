package ru.practicum.telemetry.handlers.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.entities.Sensor;
import ru.practicum.telemetry.repositories.SensorRepository;
import ru.practicum.telemetry.service.DataProcessorService;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAddedEventHandler implements HubEventHandler {
    private final DataProcessorService dataProcessorService;
    private final SensorRepository sensorRepository;

    @Override
    public Class<?> getPayloadClass() {
        return HubEventClasses.ADD_DEVICE.getEventClass();
    }

    @Override
    public void handle(Object hubEvent) {
        HubEventAvro event = (HubEventAvro) hubEvent;
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) event.getPayload();

        Optional<Sensor> sensor = sensorRepository.findByIdAndHubId(payload.getId(), event.getHubId());

        if (sensor.isPresent()) {
            log.debug("Устройство с id = {} для хаба с hubId = {} уже имеется в БД", payload.getId(), event.getHubId());
        } else {
            dataProcessorService.addSensor(event.getHubId(), payload);
        }
    }
}
