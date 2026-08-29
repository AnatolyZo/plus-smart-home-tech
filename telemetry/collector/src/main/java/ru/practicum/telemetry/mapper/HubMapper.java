package ru.practicum.telemetry.mapper;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.enums.ActionType;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.enums.EnumMapper;
import ru.practicum.telemetry.hubs.*;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class HubMapper {
    public static SpecificRecordBase toAvro(HubEvent hubEvent) {
        if (hubEvent.getType().equals(HubEventType.DEVICE_ADDED)) {
            return mapDeviceAdded((DeviceAddedEvent) hubEvent);
        } else if (hubEvent.getType().equals(HubEventType.DEVICE_REMOVED)) {
            return mapDeviceRemoved((DeviceRemovedEvent) hubEvent);
        } else if (hubEvent.getType().equals(HubEventType.SCENARIO_ADDED)) {
            ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) hubEvent;
            List<ScenarioConditionAvro> conditionsAvro = mapScenarioConditions(scenarioAddedEvent.getConditions());
            List<DeviceActionAvro> actionsAvro = mapDeviceActions(scenarioAddedEvent.getActions());
            return mapScenarioAdded((ScenarioAddedEvent) hubEvent, conditionsAvro, actionsAvro);
        } else if (hubEvent.getType().equals(HubEventType.SCENARIO_REMOVED)) {
            return mapScenarioRemoved((ScenarioRemovedEvent) hubEvent);
        }

        throw new IllegalArgumentException("Передан неизвестный тип хаба " + hubEvent.getClass());
    }

    private static SpecificRecordBase mapDeviceAdded(DeviceAddedEvent e) {
        var payload = new DeviceAddedEventAvro();
        payload.setId(e.getId());
        payload.setType(e.getDeviceType().toAvro());
        return setHubEventAvro(e, payload);
    }

    private static SpecificRecordBase mapDeviceRemoved(DeviceRemovedEvent e) {
        var payload = new DeviceRemovedEventAvro();
        payload.setId(e.getId());
        return setHubEventAvro(e, payload);
    }

    private static SpecificRecordBase mapScenarioAdded(ScenarioAddedEvent e,
                                                       List<ScenarioConditionAvro> conditions,
                                                       List<DeviceActionAvro> actions) {
        var payload = new ScenarioAddedEventAvro();
        payload.setName(e.getName());
        payload.setConditions(conditions);
        payload.setActions(actions);
        return setHubEventAvro(e, payload);
    }

    private static List<ScenarioConditionAvro> mapScenarioConditions(List<ScenarioCondition> e) {
        return e.stream()
                .map(condition -> {
                    var conditionAvro = new ScenarioConditionAvro();
                    conditionAvro.setSensorId(condition.getSensorId());
                    conditionAvro.setType(condition.getType().toAvro());
                    conditionAvro.setOperation(condition.getOperation().toAvro());
                    conditionAvro.setValue(condition.getValue());
                    return conditionAvro;
                })
                .toList();
    }

    private static List<DeviceActionAvro> mapDeviceActions(List<DeviceAction> e) {
        return e.stream()
                .map(action -> {
                    var actionAvro = new DeviceActionAvro();
                    actionAvro.setSensorId(action.getSensorId());
                    actionAvro.setType(action.getType().toAvro());
                    actionAvro.setValue((Integer) action.getValue());
                    return actionAvro;
                })
                .toList();
    }

    private static SpecificRecordBase mapScenarioRemoved(ScenarioRemovedEvent e) {
        var payload = new ScenarioRemovedEventAvro();
        payload.setName(e.getName());
        return setHubEventAvro(e, payload);
    }

    private static <T extends SpecificRecordBase> SpecificRecordBase setHubEventAvro(HubEvent hubEvent, T payload) {
        var hubEventAvro = new HubEventAvro();
        hubEventAvro.setHubId(hubEvent.getHubId());
        hubEventAvro.setTimestamp(hubEvent.getTimestamp());
        hubEventAvro.setPayload(payload);
        return hubEventAvro;
    }

    private static void setHubEventFields(HubEvent hubEvent, HubEventProto hubEventProto) {
        Timestamp timestamp = hubEventProto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        hubEvent.setHubId(hubEventProto.getHubId());
        hubEvent.setTimestamp(instant);
    }

    public static HubEvent mapToDeviceAddedEvent(HubEventProto event) {
        DeviceAddedEventProto deviceAddedEventProto = event.getDeviceAdded();
        DeviceAddedEvent deviceAddedEvent = new DeviceAddedEvent();
        setHubEventFields(deviceAddedEvent, event);
        deviceAddedEvent.setId(deviceAddedEventProto.getId());
        deviceAddedEvent.setDeviceType(toDeviceType(deviceAddedEventProto.getType()));
        return deviceAddedEvent;
    }

    private static DeviceType toDeviceType(DeviceTypeProto typeProto) {
        Optional<DeviceType> deviceType = EnumMapper.toAppEnum(DeviceType.values(), typeProto.name());

        if (deviceType.isPresent()) {
            return deviceType.get();
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип устройства " + typeProto.name());
        }
    }

    public static HubEvent mapToDeviceRemovedEvent(HubEventProto event) {
        DeviceRemovedEventProto deviceRemovedEventProto = event.getDeviceRemoved();
        DeviceRemovedEvent deviceRemovedEvent = new DeviceRemovedEvent();
        setHubEventFields(deviceRemovedEvent, event);
        deviceRemovedEvent.setId(deviceRemovedEventProto.getId());
        return deviceRemovedEvent;
    }

    public static HubEvent mapToScenarioAddedEvent(HubEventProto event) {
        ScenarioAddedEventProto scenarioAddedEventProto = event.getScenarioAdded();
        ScenarioAddedEvent scenarioAddedEvent = new ScenarioAddedEvent();
        setHubEventFields(scenarioAddedEvent, event);
        scenarioAddedEvent.setName(scenarioAddedEventProto.getName());
        scenarioAddedEvent.setConditions(toScenarioCondition(scenarioAddedEventProto.getConditionList()));
        scenarioAddedEvent.setActions(toDeviceActions(scenarioAddedEventProto.getActionList()));
        return scenarioAddedEvent;
    }

    private static List<ScenarioCondition> toScenarioCondition(List<ScenarioConditionProto> conditions) {
        return conditions.stream()
                .map(conditionProto -> {
                    var condition = new ScenarioCondition();
                    condition.setSensorId(conditionProto.getSensorId());
                    condition.setType(toConditionType(conditionProto.getType()));
                    condition.setOperation(toConditionOperation(conditionProto.getOperation()));
                    condition.setValue(getValueField(conditionProto));
                    return condition;
                })
                .toList();
    }

    private static ConditionType toConditionType(ConditionTypeProto condition) {
        Optional<ConditionType> conditionType = EnumMapper.toAppEnum(ConditionType.values(), condition.name());

        if (conditionType.isPresent()) {
            return conditionType.get();
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип состояния устройства " + condition.name());
        }
    }

    private static ConditionOperation toConditionOperation(ConditionOperationProto operation) {
        Optional<ConditionOperation> conditionOperation = EnumMapper.toAppEnum(ConditionOperation.values(), operation.name());

        if (conditionOperation.isPresent()) {
            return conditionOperation.get();
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип операции " + operation.name());
        }
    }

    private static List<DeviceAction> toDeviceActions(List<DeviceActionProto> actions) {
        return actions.stream()
                .map(actionProto -> {
                    var action = new DeviceAction();
                    action.setSensorId(actionProto.getSensorId());
                    action.setType(toActionType(actionProto.getType()));
                    action.setValue(actionProto.getValue());
                    return action;
                })
                .toList();
    }

    private static ActionType toActionType(ActionTypeProto actionTypeProto) {
        Optional<ActionType> actionType = EnumMapper.toAppEnum(ActionType.values(), actionTypeProto.name());

        if (actionType.isPresent()) {
            return actionType.get();
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип действия " + actionTypeProto.getClass());
        }
    }

    private static Object getValueField(ScenarioConditionProto conditionProto) {
        if (conditionProto.hasBoolValue()) {
            return conditionProto.getBoolValue();
        } else if (conditionProto.hasIntValue()) {
            return conditionProto.getIntValue();
        } else {
            return null;
        }
    }

    public static HubEvent mapToScenarioRemovedEvent(HubEventProto event) {
        ScenarioRemovedEventProto scenarioRemovedEventProto = event.getScenarioRemoved();
        ScenarioRemovedEvent scenarioRemovedEvent = new ScenarioRemovedEvent();
        setHubEventFields(scenarioRemovedEvent, event);
        scenarioRemovedEvent.setName(scenarioRemovedEventProto.getName());
        return scenarioRemovedEvent;
    }
}
