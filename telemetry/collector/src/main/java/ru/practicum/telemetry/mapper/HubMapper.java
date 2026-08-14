package ru.practicum.telemetry.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.hubs.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

public class HubMapper {
    public static SpecificRecordBase toAvro(HubEvent hubEvent) {
        if (hubEvent.getHubEventType().equals(HubEventType.DEVICE_ADDED)) {
            return mapDeviceAdded((DeviceAddedEvent) hubEvent);
        } else if (hubEvent.getHubEventType().equals(HubEventType.DEVICE_REMOVED)) {
            return mapDeviceRemoved((DeviceRemovedEvent) hubEvent);
        } else if (hubEvent.getHubEventType().equals(HubEventType.SCENARIO_ADDED)) {
            ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) hubEvent;
            List<ScenarioConditionAvro> conditionsAvro = mapScenarioConditions(scenarioAddedEvent.getConditions());
            List<DeviceActionAvro> actionsAvro = mapDeviceActions(scenarioAddedEvent.getActions());
            return mapScenarioAdded((ScenarioAddedEvent) hubEvent, conditionsAvro, actionsAvro);
        } else if (hubEvent.getHubEventType().equals(HubEventType.SCENARIO_REMOVED)) {
            return mapScenarioRemoved((ScenarioRemovedEvent) hubEvent);
        }
        throw new IllegalArgumentException("Передан неизвестный тип хаба " + hubEvent.getClass());
    }

    private static SpecificRecordBase mapDeviceAdded(DeviceAddedEvent e) {
        System.out.println(e);
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
                    actionAvro.setValue(actionAvro.getValue());
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
}
