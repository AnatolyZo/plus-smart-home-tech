package ru.practicum.telemetry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.telemetry.entities.*;
import ru.practicum.telemetry.enums.ActionType;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.repositories.ActionRepository;
import ru.practicum.telemetry.repositories.ConditionRepository;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.repositories.SensorRepository;
import ru.practicum.telemetry.service.DataProcessorServiceImpl;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataProcessorServiceTests {
    @InjectMocks
    private DataProcessorServiceImpl dataProcessorService;
    @Mock
    private ScenarioRepository scenarioRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private ConditionRepository conditionRepository;
    @Mock
    private ActionRepository actionRepository;

    @Test
    void addSameScenario() {
        String hubId = "hub-1";
        String scenarioName = "cool-when-hot";

        ScenarioAddedEventAvro payload = createScenarioAddedEvent(List.of(createScenarioConditionAvro(1)), List.of(createDeviceActionAvro()));

        Sensor conditionSensor = createSensor("climate-sensor-001", hubId);
        Sensor actionSensor = createSensor("ac-unit-001", hubId);

        Condition condition = createCondition(25);
        Action action = createAction();
        Scenario existingScenario = createScenario(conditionSensor, actionSensor, condition, action);

        when(scenarioRepository.findByHubIdAndName(eq(hubId), eq(scenarioName)))
                .thenReturn(Optional.of(existingScenario));
        when(sensorRepository.findBySensorIdsAndHub(anyList(), eq(hubId)))
                .thenReturn(List.of(conditionSensor, actionSensor));
        when(conditionRepository.save(any(Condition.class))).thenReturn(condition);
        when(actionRepository.save(any(Action.class))).thenReturn(action);

        dataProcessorService.addScenario(hubId, payload);

        verify(scenarioRepository).findByHubIdAndName(eq(hubId), eq(scenarioName));
        verify(conditionRepository).deleteAllById(anyList());
        verify(actionRepository).deleteAllById(anyList());

        //save был вызван ровно 1 раз для существующего сценария
        verify(scenarioRepository, times(1)).save(eq(existingScenario));
    }

    @Test
    void removeScenario() {
        String hubId = "hub-1";
        String scenarioName = "non-existing-scenario";

        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(scenarioName)
                .build();

        when(scenarioRepository.findByHubIdAndName(eq(hubId), eq(scenarioName)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataProcessorService.removeScenario(hubId, payload)
        );

        Assertions.assertTrue(exception.getMessage().contains("сценарий для удаления в БД отсутствует"));
    }

    @Test
    void setNullConditionValue() {
        String hubId = "hub-1";
        String scenarioName = "cool-when-hot";

        ScenarioAddedEventAvro payload = createScenarioAddedEvent(List.of(createScenarioConditionAvro(null)), List.of());

        Sensor sensor = createSensor("climate-sensor-001", hubId);
        when(sensorRepository.findBySensorIdsAndHub(anyList(), eq(hubId)))
                .thenReturn(List.of(sensor));
        when(scenarioRepository.findByHubIdAndName(eq(hubId), eq(scenarioName)))
                .thenReturn(Optional.empty());

        Condition savedCondition = createCondition(1);
        Scenario createdScenario = createScenario(sensor, sensor, savedCondition, createAction());
        when(scenarioRepository.save(any(Scenario.class))).thenReturn(createdScenario);

        when(conditionRepository.save(any(Condition.class))).thenReturn(savedCondition);

        dataProcessorService.addScenario(hubId, payload);

        ArgumentCaptor<Condition> captor = ArgumentCaptor.forClass(Condition.class);
        verify(conditionRepository).save(captor.capture());

        Condition captured = captor.getValue();
        Assertions.assertNull(captured.getValue(), "Для null в Avro должно сохраняться null");
    }

    @Test
    void setNumberConditionValue() {
        String hubId = "hub-1";
        String scenarioName = "cool-when-hot";

        ScenarioAddedEventAvro payload = createScenarioAddedEvent(List.of(createScenarioConditionAvro(42)), List.of());

        Sensor sensor = createSensor("climate-sensor-001", hubId);
        when(sensorRepository.findBySensorIdsAndHub(anyList(), eq(hubId)))
                .thenReturn(List.of(sensor));

        when(scenarioRepository.findByHubIdAndName(eq(hubId), eq(scenarioName)))
                .thenReturn(Optional.empty());

        Condition condition = createCondition(42);
        Scenario createdScenario = createScenario(sensor, sensor, condition, createAction());
        when(scenarioRepository.save(any(Scenario.class))).thenReturn(createdScenario);
        when(conditionRepository.save(any(Condition.class)))
                .thenReturn(condition);

        dataProcessorService.addScenario(hubId, payload);

        ArgumentCaptor<Condition> captor = ArgumentCaptor.forClass(Condition.class);
        verify(conditionRepository, times(1)).save(captor.capture());
        Condition capturedCondition = captor.getValue();

        Assertions.assertEquals(42, capturedCondition.getValue());
    }

    @Test
    void setBooleanConditionValue() {
        String hubId = "hub-1";
        String scenarioName = "cool-when-hot";

        ScenarioAddedEventAvro payload = createScenarioAddedEvent(List.of(createScenarioConditionAvro(true)), List.of());

        Sensor sensor = createSensor("climate-sensor-001", hubId);
        when(sensorRepository.findBySensorIdsAndHub(anyList(), eq(hubId)))
                .thenReturn(List.of(sensor));

        Condition condition = createCondition(ConstantValues.ONE);

        Scenario createdScenario = createScenario(sensor, sensor, condition, createAction());
        when(scenarioRepository.save(any(Scenario.class))).thenReturn(createdScenario);
        when(conditionRepository.save(any(Condition.class)))
                .thenReturn(condition);

        dataProcessorService.addScenario(hubId, payload);

        ArgumentCaptor<Condition> captor = ArgumentCaptor.forClass(Condition.class);
        verify(conditionRepository, times(1)).save(captor.capture());
        Condition capturedCondition = captor.getValue();

        Assertions.assertEquals(ConstantValues.ONE, capturedCondition.getValue(), "true должно стать 1");
    }

    @Test
    void setInvalidConditionValue() {
        String hubId = "hub-1";
        String scenarioName = "test-invalid-type";

        ScenarioAddedEventAvro payload = createScenarioAddedEvent(List.of(createScenarioConditionAvro("INVALID TYPE")), List.of());

        Sensor sensor = createSensor("climate-sensor-001", hubId);
        when(sensorRepository.findBySensorIdsAndHub(anyList(), eq(hubId)))
                .thenReturn(List.of(sensor));

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataProcessorService.addScenario(hubId, payload)
        );

        Assertions.assertTrue(
                exception.getMessage().contains("Поле value должно быть Integer, Boolean или Null"),
                "Сообщение об ошибке должно содержать ожидаемый текст"
        );
    }

    private ScenarioConditionAvro createScenarioConditionAvro(Object value) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId("climate-sensor-001")
                .setType(ConditionTypeAvro.TEMPERATURE)
                .setOperation(ConditionOperationAvro.GREATER_THAN)
                .setValue(value)
                .build();
    }

    private DeviceActionAvro createDeviceActionAvro() {
        return DeviceActionAvro.newBuilder()
                .setSensorId("ac-unit-001")
                .setType(ActionTypeAvro.ACTIVATE)
                .setValue(1)
                .build();
    }

    public ScenarioAddedEventAvro createScenarioAddedEvent(List<ScenarioConditionAvro> conditions, List<DeviceActionAvro> actions) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName("cool-when-hot")
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }

    private Sensor createSensor(String sensorId, String hubId) {
        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);

        return sensor;
    }

    private Condition createCondition(Integer value) {
        return Condition.builder()
                .type(ConditionType.TEMPERATURE)
                .operation(ConditionOperation.GREATER_THAN)
                .value(value)
                .build();
    }

    private Action createAction() {
        return Action.builder()
                .type(ActionType.ACTIVATE)
                .value(1)
                .build();
    }

    private Scenario createScenario(Sensor conditionSensor, Sensor actionSensor, Condition condition, Action action) {
        Scenario scenario = Scenario.builder()
                .id(1L)
                .hubId(conditionSensor.getHubId())
                .name("test-scenario-for-" + conditionSensor.getId())
                .conditions(new ArrayList<>())
                .actions(new ArrayList<>())
                .build();

        ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                .scenario(scenario)
                .sensor(conditionSensor)
                .condition(condition)
                .build();

        ScenarioAction scenarioAction = ScenarioAction.builder()
                .scenario(scenario)
                .sensor(actionSensor)
                .action(action)
                .build();

        ScenarioConditionId conditionId = new ScenarioConditionId();
        conditionId.setScenarioId(scenario.getId());
        conditionId.setSensorId(conditionSensor.getId());
        conditionId.setConditionId(condition.getId());
        scenarioCondition.setId(conditionId);

        ScenarioActionId actionId = new ScenarioActionId();
        actionId.setScenarioId(scenario.getId());
        actionId.setSensorId(actionSensor.getId());
        actionId.setActionId(action.getId());
        scenarioAction.setId(actionId);

        scenario.getConditions().add(scenarioCondition);
        scenario.getActions().add(scenarioAction);

        return scenario;
    }
}
