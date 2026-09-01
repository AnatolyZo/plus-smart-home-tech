package ru.practicum.telemetry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.telemetry.entities.*;
import ru.practicum.telemetry.handlers.hub.DeviceAddedEventHandler;
import ru.practicum.telemetry.repositories.ActionRepository;
import ru.practicum.telemetry.repositories.ConditionRepository;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.repositories.SensorRepository;
import ru.practicum.telemetry.service.DataProcessorServiceImpl;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceAddedEventHandlerTests {
    @Mock
    private DataProcessorServiceImpl dataProcessorService;
    @Mock
    private ScenarioRepository scenarioRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private ConditionRepository conditionRepository;
    @Mock
    private ActionRepository actionRepository;

    @InjectMocks
    private DeviceAddedEventHandler handler;

    @Test
    void addNotExistingSensor() {
        HubEventAvro hubEventAvro = createDeviceAddedEvent("hub-1", "sensor_id");
        when(sensorRepository.findByIdAndHubId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        handler.handle(hubEventAvro);
        verify(dataProcessorService, times(1)).addSensor(any(), any());
    }

    @Test
    void addExistingSensor() {
        HubEventAvro hubEventAvro = createDeviceAddedEvent("hub-1", "sensor_id");
        when(sensorRepository.findByIdAndHubId(anyString(), anyString()))
                .thenReturn(Optional.of(new Sensor()));
        handler.handle(hubEventAvro);
        verify(dataProcessorService, never()).addSensor(any(), any());
    }

    private HubEventAvro createDeviceAddedEvent(String hubId, String sensorId) {
        DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
        payload.setId(sensorId);
        payload.setType(DeviceTypeAvro.CLIMATE_SENSOR);

        HubEventAvro event = new HubEventAvro();
        event.setHubId(hubId);
        event.setTimestamp(Instant.now());
        event.setPayload(payload);

        return event;
    }
}
