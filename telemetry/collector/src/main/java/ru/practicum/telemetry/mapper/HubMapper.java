package ru.practicum.telemetry.mapper;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.hubs.*;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

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

    public static HubEvent toHubEvent(HubEventProto hubEventProto) {
        HubEventProto.PayloadCase payloadCase = hubEventProto.getPayloadCase();

        switch (payloadCase) {
            case DEVICE_ADDED:
                DeviceAddedEventProto deviceAddedEventProto = hubEventProto.getDeviceAdded();
                DeviceAddedEvent deviceAddedEvent = new DeviceAddedEvent();
                setHubEventFields(deviceAddedEvent, hubEventProto);
                deviceAddedEvent.setId(deviceAddedEventProto.getId());
                deviceAddedEvent.setDeviceType(toDeviceType(deviceAddedEventProto.getType()));
                return deviceAddedEvent;
            case DEVICE_REMOVED:
                DeviceRemovedEventProto deviceRemovedEventProto = hubEventProto.getDeviceRemoved();
                DeviceRemovedEvent deviceRemovedEvent = new DeviceRemovedEvent();
                setHubEventFields(deviceRemovedEvent, hubEventProto);
                deviceRemovedEvent.setId(deviceRemovedEventProto.getId());
                return deviceRemovedEvent;
            case SCENARIO_ADDED:
                ScenarioAddedEventProto scenarioAddedEventProto = hubEventProto.getScenarioAdded();
                ScenarioAddedEvent scenarioAddedEvent = new ScenarioAddedEvent();
                setHubEventFields(scenarioAddedEvent, hubEventProto);
                scenarioAddedEvent.setName(scenarioAddedEventProto.getName());
                scenarioAddedEvent.setConditions(toScenarioCondition(scenarioAddedEventProto.getConditionList()));
                scenarioAddedEvent.setActions(toDeviceActions(scenarioAddedEventProto.getActionList()));
                return scenarioAddedEvent;
            case SCENARIO_REMOVED:
                ScenarioRemovedEventProto scenarioRemovedEventProto = hubEventProto.getScenarioRemoved();
                ScenarioRemovedEvent scenarioRemovedEvent = new ScenarioRemovedEvent();
                setHubEventFields(scenarioRemovedEvent, hubEventProto);
                scenarioRemovedEvent.setName(scenarioRemovedEventProto.getName());
                return scenarioRemovedEvent;
            default:
                throw new IllegalArgumentException("Передан неизвестный тип хаба " + hubEventProto.getClass());
        }
    }

    private static void setHubEventFields(HubEvent hubEvent, HubEventProto hubEventProto) {
        Timestamp timestamp = hubEventProto.getTimestamp();
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        hubEvent.setHubId(hubEventProto.getHubId());
        hubEvent.setTimestamp(instant);
    }

    private static DeviceType toDeviceType(DeviceTypeProto typeProto) {
        if (typeProto.name().equals(DeviceType.CLIMATE_SENSOR.name())) {
            return DeviceType.CLIMATE_SENSOR;
        } else if (typeProto.name().equals(DeviceType.LIGHT_SENSOR.name())) {
            return DeviceType.LIGHT_SENSOR;
        } else if (typeProto.name().equals(DeviceType.MOTION_SENSOR.name())) {
            return DeviceType.MOTION_SENSOR;
        } else if (typeProto.name().equals(DeviceType.SWITCH_SENSOR.name())) {
            return DeviceType.SWITCH_SENSOR;
        } else if (typeProto.name().equals(DeviceType.TEMPERATURE_SENSOR.name())) {
            return DeviceType.TEMPERATURE_SENSOR;
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип устройства " + typeProto.getClass());
        }
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
        if (condition.name().equals(ConditionType.MOTION.name())) {
            return ConditionType.MOTION;
        } else if (condition.name().equals(ConditionType.LUMINOSITY.name())) {
            return ConditionType.LUMINOSITY;
        } else if (condition.name().equals(ConditionType.SWITCH.name())) {
            return ConditionType.SWITCH;
        } else if (condition.name().equals(ConditionType.TEMPERATURE.name())) {
            return ConditionType.TEMPERATURE;
        } else if (condition.name().equals(ConditionType.CO2LEVEL.name())) {
            return ConditionType.CO2LEVEL;
        } else if (condition.name().equals(ConditionType.HUMIDITY.name())) {
            return ConditionType.HUMIDITY;
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип состояния устройства " + condition.getClass());
        }
    }

    private static ConditionOperation toConditionOperation(ConditionOperationProto operation) {
        if (operation.name().equals(ConditionOperation.EQUALS.name())) {
            return ConditionOperation.EQUALS;
        } else if (operation.name().equals(ConditionOperation.GREATER_THAN.name())) {
            return ConditionOperation.GREATER_THAN;
        } else if (operation.name().equals(ConditionOperation.LOWER_THAN.name())) {
            return ConditionOperation.LOWER_THAN;
        } else {
            throw new IllegalArgumentException("Передан неизвестный тип операции " + operation.getClass());
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
        if (actionTypeProto.name().equals(ActionType.ACTIVATE.name())) {
            return ActionType.ACTIVATE;
        } else if (actionTypeProto.name().equals(ActionType.DEACTIVATE.name())) {
            return ActionType.DEACTIVATE;
        } else if (actionTypeProto.name().equals(ActionType.INVERSE.name())) {
            return ActionType.INVERSE;
        } else if (actionTypeProto.name().equals(ActionType.SET_VALUE.name())) {
            return ActionType.SET_VALUE;
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
            throw new IllegalArgumentException("Передан неизвестный тип поля value класса " + conditionProto.getClass());
        }
    }
}
