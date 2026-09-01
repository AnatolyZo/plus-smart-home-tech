package ru.practicum.telemetry.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.telemetry.ConstantValues;
import ru.practicum.telemetry.entities.*;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.handlers.snapshot.SnapshotHandler;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.service.functions.Metrics;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SnapshotProcessorServiceImpl implements SnapshotProcessorService {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private final Map<Class<?>, SnapshotHandler> snapshotHandlers;
    private final ScenarioRepository scenarioRepository;

    public SnapshotProcessorServiceImpl(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient,
                                        Set<SnapshotHandler> snapshotHandlers,
                                        ScenarioRepository scenarioRepository) {
        this.hubRouterClient = hubRouterClient;
        this.snapshotHandlers = snapshotHandlers.stream()
                .collect(Collectors.toMap(SnapshotHandler::getSensorDataClass, Function.identity()));
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void processSnapshot(SensorsSnapshotAvro sensorsSnapshotAvro) {
        log.trace("Начата обработка снапшота {}", sensorsSnapshotAvro);
        String hubId = sensorsSnapshotAvro.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.debug("Получен список сценариев {} для hubId = '{}'", scenarios, hubId);
        Map<String, SensorStateAvro> sensorsState = sensorsSnapshotAvro.getSensorsState();
        log.debug("Получено отображение sensorsState = {}", sensorsState);

        if (sensorsState == null) {
            throw new IllegalArgumentException("Отсутствует поле sensorsState у объекта " + sensorsSnapshotAvro);
        }

        scenarios.stream()
                .filter(scenario -> processConditions(scenario, sensorsState))
                .flatMap(scenario -> formActionsForHub(scenario).stream())
                .forEach(this::sendCommandToHub);
    }

    private boolean processConditions(Scenario scenario, Map<String, SensorStateAvro> sensorsState) {
        log.trace("Начат процесс обработки условий");
        return scenario.getConditions().stream()
                .allMatch(scenarioCondition -> matchCondition(scenarioCondition, sensorsState));
    }

    private boolean matchCondition(ScenarioCondition scenarioCondition, Map<String, SensorStateAvro> sensorsState) {
        Sensor sensor = scenarioCondition.getSensor();
        Condition condition = scenarioCondition.getCondition();
        ConditionType conditionType = condition.getType();
        SensorStateAvro state = sensorsState.get(sensor.getId());
        log.debug("Получены данные: sensor = {}, condition = {}, conditionType = {}, state = {}", sensor, condition, conditionType, state);

        if (state == null) {
            log.debug("Поле state = null, возвращается результат обработки условий - false");
            return false;
        }

        int conditionValue = condition.getValue() != null ? condition.getValue() : ConstantValues.ZERO;
        int sensorsData = getSensorsData(conditionType, state);
        ConditionOperation operation = condition.getOperation();
        log.debug("Получены данные: conditionValue = {}, sensorsData = {}, operation = {}", conditionValue, sensorsData, operation);

        boolean comparisonResult = Metrics.COMPARATORS.get(operation).apply(sensorsData, conditionValue);
        log.debug("Результат сравнения comparisonResult = {}", comparisonResult);
        return comparisonResult;
    }

    private int getSensorsData(ConditionType conditionType, SensorStateAvro state) {
        log.trace("Начат процес получения данных датчика");
        Class<?> sensorClass = state.getData().getClass();

        if (snapshotHandlers.containsKey(sensorClass)) {
            int sensorsData = snapshotHandlers.get(sensorClass).handle(conditionType, state);
            log.debug("Обработчиком {} получены данные датчика sensorsData = {}", snapshotHandlers.get(sensorClass), sensorsData);
            return sensorsData;
        } else {
            log.warn("Не получилось найти обработчик для датчика {}", sensorClass);
            throw new IllegalArgumentException("Не получилось найти обработчик для датчика " + sensorClass);
        }
    }

    private List<DeviceActionRequest> formActionsForHub (Scenario scenario) {
        return scenario.getActions().stream()
                .map(scenarioAction -> buildAction(scenario, scenarioAction))
                .toList();
    }

    private DeviceActionRequest buildAction(Scenario scenario, ScenarioAction scenarioAction) {
        Sensor sensor = scenarioAction.getSensor();
        Action action = scenarioAction.getAction();

        DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                .setSensorId(sensor.getId())
                .setType(action.getType().toProto())
                .setValue(action.getValue() != null ? action.getValue() : ConstantValues.ZERO)
                .build();
        log.debug("Сформирован объект deviceActionProto = {}", deviceActionProto);

        Instant instant = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(scenario.getHubId())
                .setScenarioName(scenario.getName())
                .setAction(deviceActionProto)
                .setTimestamp(timestamp)
                .build();
        log.debug("Сформирован объект request = {}", request);

        return request;
    }

    private void sendCommandToHub(DeviceActionRequest request) {
        log.trace("Начат процесс отправки действий на хаб");
        hubRouterClient.handleDeviceAction(request);
        log.debug("Отправлено grpc-сообщение request = {}", request);
    }
}
