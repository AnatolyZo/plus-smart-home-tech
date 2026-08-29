package ru.practicum.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.telemetry.entities.*;
import ru.practicum.telemetry.enums.ActionType;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.handlers.snapshot.ClimateSensorEventHandler;
import ru.practicum.telemetry.handlers.snapshot.SnapshotHandler;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.service.SnapshotProcessorServiceImpl;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SnapshotProcessorServiceTests {
    private SnapshotProcessorServiceImpl snapshotProcessorService;

    @Mock
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    @Mock
    private ScenarioRepository scenarioRepository;

    @BeforeEach
    void setup() {
        ClimateSensorEventHandler mockHandler = mock(ClimateSensorEventHandler.class);
        ClimateSensorEventHandler handler = new ClimateSensorEventHandler();

        Set<SnapshotHandler> handlerSet = new HashSet<>();
        handlerSet.add(handler);

        snapshotProcessorService = new SnapshotProcessorServiceImpl(hubRouterStub, handlerSet, scenarioRepository);
    }

    @Test
    void processSnapshotConditionMatched() {
        Sensor sensor = createSensor();
        Scenario scenario = createScenario(sensor, 20);
        SensorsSnapshotAvro snapshotAvro = createSnapshot();

        when(scenarioRepository.findByHubId(any())).thenReturn(List.of(scenario));
        when(hubRouterStub.handleDeviceAction(any()))
                .thenReturn(null);

        snapshotProcessorService.processSnapshot(snapshotAvro);

        ArgumentCaptor<DeviceActionRequest> captor = ArgumentCaptor.forClass(DeviceActionRequest.class);
        verify(hubRouterStub, times(1)).handleDeviceAction(captor.capture());

        DeviceActionRequest actionRequest = captor.getValue();
        assertThat(actionRequest.getHubId()).isEqualTo("hub-1");
        assertThat(actionRequest.getScenarioName()).isEqualTo("test-scenario-for-climate-sensor-001");
        assertThat(actionRequest.getAction().getSensorId()).isEqualTo("climate-sensor-001");
        assertThat(actionRequest.getAction().getType()).isEqualTo(ActionTypeProto.ACTIVATE);
    }

    @Test
    void processSnapshotConditionNotMatched() {
        Sensor sensor = createSensor();
        Scenario scenario = createScenario(sensor, 40);
        SensorsSnapshotAvro snapshotAvro = createSnapshot();

        when(scenarioRepository.findByHubId(any())).thenReturn(List.of(scenario));

        snapshotProcessorService.processSnapshot(snapshotAvro);

        verify(hubRouterStub, never()).handleDeviceAction(any());
    }

    private Sensor createSensor() {
        Sensor sensor = new Sensor();
        sensor.setId("climate-sensor-001");
        sensor.setHubId("hub-1");

        return sensor;
    }

    private Scenario createScenario(Sensor sensor, Integer value) {
        Condition condition = Condition.builder()
                .type(ConditionType.TEMPERATURE)
                .operation(ConditionOperation.GREATER_THAN)
                .value(value)
                .build();

        Action action = Action.builder()
                .type(ActionType.ACTIVATE)
                .value(1)
                .build();

        Scenario scenario = Scenario.builder()
                .id(1L)
                .hubId(sensor.getHubId())
                .name("test-scenario-for-" + sensor.getId())
                .conditions(new ArrayList<>())
                .actions(new ArrayList<>())
                .build();

        ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                .scenario(scenario)
                .sensor(sensor)
                .condition(condition)
                .build();

        ScenarioAction scenarioAction = ScenarioAction.builder()
                .scenario(scenario)
                .sensor(sensor)
                .action(action)
                .build();

        ScenarioConditionId conditionId = new ScenarioConditionId();
        conditionId.setScenarioId(scenario.getId());
        conditionId.setSensorId(sensor.getId());
        conditionId.setConditionId(condition.getId());
        scenarioCondition.setId(conditionId);

        ScenarioActionId actionId = new ScenarioActionId();
        actionId.setScenarioId(scenario.getId());
        actionId.setSensorId(sensor.getId());
        actionId.setActionId(action.getId());
        scenarioAction.setId(actionId);

        scenario.getConditions().add(scenarioCondition);
        scenario.getActions().add(scenarioAction);

        return scenario;
    }

    private SensorsSnapshotAvro createSnapshot() {
        Instant timestamp = Instant.now();

        ClimateSensorAvro climateData = new ClimateSensorAvro();
        climateData.setTemperatureC(30);
        climateData.setHumidity(40);

        SensorStateAvro sensorState = new SensorStateAvro();
        sensorState.setTimestamp(timestamp);
        sensorState.setData(climateData);

        Map<String, SensorStateAvro> sensorsStateMap = Map.of("climate-sensor-001", sensorState);

        return new SensorsSnapshotAvro(
                "hub-1",
                timestamp,
                sensorsStateMap
        );
    }
}
