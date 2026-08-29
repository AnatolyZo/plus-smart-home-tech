package ru.practicum.telemetry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.telemetry.ConstantValues;
import ru.practicum.telemetry.entities.*;
import ru.practicum.telemetry.enums.ActionType;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.enums.EnumMapper;
import ru.practicum.telemetry.repositories.ActionRepository;
import ru.practicum.telemetry.repositories.ConditionRepository;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.repositories.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DataProcessorServiceImpl implements DataProcessorService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Override
    public void addScenario(String hubId, ScenarioAddedEventAvro payload) {
        log.trace("Начат процес добавления сценария");
        Scenario scenario = getOrCreateScenario(hubId, payload.getName());

        Map<String, Sensor> conditionSensorsMap = getSensorsForConditions(hubId, payload.getConditions());
        setScenarioCondition(scenario, payload.getConditions(), conditionSensorsMap);

        Map<String, Sensor> actionSensorsMap = getSensorsForActions(hubId, payload.getActions());
        setScenarioAction(scenario, payload.getActions(), actionSensorsMap);

        scenarioRepository.save(scenario);
        log.debug("Сохранен сценарий scenario = {}", scenario);
    }

    @Override
    public void removeScenario(String hubId, ScenarioRemovedEventAvro payload) {
        log.trace("Начат процесс удаления сценария");
        scenarioRepository.deleteByHubIdAndName(hubId, payload.getName());
        log.debug("Удален сценарий с названием name = {} для хаба с hubId = {}", payload.getName(), hubId);
    }

    @Override
    public void addSensor(String hubId, DeviceAddedEventAvro payload) {
        log.trace("Начат процесс добавления датчика");
        Sensor sensor = new Sensor();
        sensor.setId(payload.getId());
        sensor.setHubId(hubId);
        Sensor savedSensor = sensorRepository.save(sensor);
        log.debug("Сохранен датчик savedSensor = {}", savedSensor);
    }

    @Override
    public void removeSensor(DeviceRemovedEventAvro payload, String hubId) {
        log.trace("Начат процесс удаления датчика");
        sensorRepository.deleteByIdAndHubId(payload.getId(), hubId);
        log.debug("Удален датчик с названием id = {} для хаба с hubId = {}", payload.getId(), hubId);
    }

    private Scenario getOrCreateScenario(String hubId, String name) {
        Scenario scenario;
        Optional<Scenario> scenarioDb = scenarioRepository.findByHubIdAndName(hubId, name);

        if (scenarioDb.isPresent()) {
            scenario = scenarioDb.get();
            scenario.getConditions().clear();
            scenario.getActions().clear();
            log.debug("Редактируется существующий сценарий scenario = {}", scenario);
        } else {
            scenario = Scenario.builder()
                    .hubId(hubId)
                    .name(name)
                    .build();
            scenario = scenarioRepository.save(scenario);
            log.debug("Создан новый сценарий scenario = {}", scenario);
        }

        return scenario;
    }

    private Map<String, Sensor> getSensorsForConditions(String hubId, List<ScenarioConditionAvro> conditions) {
        List<String> conditionSensorIds = conditions.stream()
                .map(ScenarioConditionAvro::getSensorId)
                .toList();

        return sensorRepository.findBySensorIdsAndHub(conditionSensorIds, hubId).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
    }

    private void setScenarioCondition(Scenario scenario,
                                      List<ScenarioConditionAvro> scenarioConditionsAvro,
                                      Map<String, Sensor> sensorsMap) {
        List<ScenarioCondition> scenarioConditions = scenarioConditionsAvro.stream()
                .map(conditionAvro -> createScenarioCondition(conditionAvro, scenario, sensorsMap))
                .collect(Collectors.toCollection(ArrayList::new));

        scenario.setConditions(scenarioConditions);
    }

    private ScenarioCondition createScenarioCondition(ScenarioConditionAvro conditionAvro,
                                                      Scenario scenario,
                                                      Map<String, Sensor> sensorsMap) {
        System.out.println(sensorsMap);
        Sensor sensor = sensorsMap.get(conditionAvro.getSensorId());
        Condition condition = formCondition(conditionAvro);

        if (sensor == null) {
            log.warn("Не найден сенсор с id = {}", conditionAvro.getSensorId());
            throw new IllegalArgumentException("Не найден сенсор с id = " + conditionAvro.getSensorId());
        }

        ScenarioConditionId id = ScenarioConditionId.builder()
                .scenarioId(scenario.getId())
                .sensorId(sensor.getId())
                .conditionId(condition.getId())
                .build();

        return ScenarioCondition.builder()
                .id(id)
                .scenario(scenario)
                .sensor(sensor)
                .condition(condition)
                .build();
    }

    private Condition formCondition(ScenarioConditionAvro conditionAvro) {
        ConditionType conditionType = EnumMapper.toAppEnum(ConditionType.values(), conditionAvro.getType().name())
                .orElseThrow(() -> new IllegalArgumentException("Передан неизвестный тип условия " + conditionAvro.getType().name()));

        ConditionOperation conditionOperation = EnumMapper.toAppEnum(ConditionOperation.values(), conditionAvro.getOperation().name())
                .orElseThrow(() -> new IllegalArgumentException("Передан неизвестный тип операции " + conditionAvro.getType().name()));;

        Condition condition = Condition.builder()
                .type(conditionType)
                .operation(conditionOperation)
                .value(setConditionValue(conditionAvro.getValue()))
                .build();

        return conditionRepository.save(condition);
    }

    private Integer setConditionValue(Object value) {
        return switch (value) {
            case null -> null;
            case Number number -> number.intValue();
            case Boolean b -> b ? ConstantValues.ONE : ConstantValues.ZERO;
            default ->
                    throw new IllegalArgumentException("Поле value должно быть Integer, Boolean или Null, передано " + value.getClass());
        };
    }

    private Map<String, Sensor> getSensorsForActions(String hubId, List<DeviceActionAvro> actions) {
        List<String> actionSensorIds = actions.stream()
                .map(DeviceActionAvro::getSensorId)
                .toList();

        return sensorRepository.findBySensorIdsAndHub(actionSensorIds, hubId).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
    }

    private void setScenarioAction(Scenario scenario,
                                   List<DeviceActionAvro> deviceActionAvro,
                                   Map<String, Sensor> sensorsMap) {
        List<ScenarioAction> scenarioActions = deviceActionAvro.stream()
                .map(actionAvro -> createScenarioAction(actionAvro, scenario, sensorsMap))
                .collect(Collectors.toCollection(ArrayList::new));

        scenario.setActions(scenarioActions);
    }

    private ScenarioAction createScenarioAction(DeviceActionAvro actionAvro,
                                                Scenario scenario,
                                                Map<String, Sensor> sensorsMap) {
        Sensor sensor = sensorsMap.get(actionAvro.getSensorId());
        Action action = formAction(actionAvro);

        ScenarioActionId id = ScenarioActionId.builder()
                .scenarioId(scenario.getId())
                .sensorId(sensor.getId())
                .actionId(action.getId())
                .build();

        return ScenarioAction.builder()
                .id(id)
                .scenario(scenario)
                .sensor(sensor)
                .action(action)
                .build();
    }

    private Action formAction(DeviceActionAvro deviceActionAvro) {
        ActionType actionType = EnumMapper.toAppEnum(ActionType.values(), deviceActionAvro.getType().name())
                .orElseThrow(() -> new IllegalArgumentException("Передан неизвестный тип устройства " + deviceActionAvro.getType().name()));

        Action action = Action.builder()
                .type(actionType)
                .value(deviceActionAvro.getValue())
                .build();

        return actionRepository.save(action);
    }
}
