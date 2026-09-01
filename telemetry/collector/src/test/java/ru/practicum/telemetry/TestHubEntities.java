package ru.practicum.telemetry;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.enums.ActionType;
import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.practicum.telemetry.hubs.*;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

public class TestHubEntities {
    private static final Instant timestamp = Instant.now();
    private static final Timestamp timestampProto = Timestamp.newBuilder()
            .setSeconds(timestamp.getEpochSecond())
            .setNanos(timestamp.getNano())
            .build();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";

    public static HubEvent createDeviceAddedEvent(String hubId) {
        var event = new DeviceAddedEvent();
        setHubEvent(hubId, event);
        event.setId(SENSOR_ID);
        event.setDeviceType(DeviceType.CLIMATE_SENSOR);
        return event;
    }

    public static HubEvent createDeviceRemovedEvent(String hubId) {
        var event = new DeviceRemovedEvent();
        setHubEvent(hubId, event);
        event.setId(SENSOR_ID);
        return event;
    }

    public static HubEvent createScenarioAddedEvent(String hubId) {
        var event = new ScenarioAddedEvent();
        setHubEvent(hubId, event);
        event.setName("name");
        event.setConditions(List.of(createCondition(SENSOR_ID + "1"), createCondition(SENSOR_ID + "2")));
        event.setActions(List.of(createAction(SENSOR_ID + "1"), createAction(SENSOR_ID + "2")));
        return event;
    }

    private static ScenarioCondition createCondition(String sensorId) {
        var condition = new ScenarioCondition();
        condition.setSensorId(sensorId);
        condition.setType(ConditionType.MOTION);
        condition.setOperation(ConditionOperation.EQUALS);
        condition.setValue(null);
        return condition;
    }

    private static DeviceAction createAction(String sensorId) {
        var action = new DeviceAction();
        action.setSensorId(sensorId);
        action.setType(ActionType.ACTIVATE);
        action.setValue(1);
        return action;
    }

    public static HubEvent createScenarioRemovedEvent(String hubId) {
        var event = new ScenarioRemovedEvent();
        setHubEvent(hubId, event);
        event.setName("name");
        return event;
    }

    private static void setHubEvent(String hubId, HubEvent event) {
        event.setHubId(hubId);
        event.setTimestamp(timestamp);
    }

    public static <T extends SpecificRecordBase> SpecificRecordBase createTestHubEventAvro(T hubEvent) {
        var event = new HubEventAvro();
        event.setHubId(HUB_ID);
        event.setTimestamp(timestamp);
        event.setPayload(hubEvent);
        return event;
    }

    public static SpecificRecordBase createDeviceAddedEventAvro() {
        var event = new DeviceAddedEventAvro();
        event.setId(SENSOR_ID);
        event.setType(DeviceTypeAvro.CLIMATE_SENSOR);
        return event;
    }

    public static SpecificRecordBase createDeviceRemovedEventAvro() {
        var event = new DeviceRemovedEventAvro();
        event.setId(SENSOR_ID);
        return event;
    }

    public static SpecificRecordBase createScenarioAddedEventAvro() {
        var event = new ScenarioAddedEventAvro();
        event.setName("name");
        event.setConditions(List.of(createConditionAvro(SENSOR_ID + "1"), createConditionAvro(SENSOR_ID + "2")));
        event.setActions(List.of(createActionAvro(SENSOR_ID + "1"), createActionAvro(SENSOR_ID + "2")));
        return event;
    }

    private static ScenarioConditionAvro createConditionAvro(String sensorId) {
        var condition = new ScenarioConditionAvro();
        condition.setSensorId(sensorId);
        condition.setType(ConditionTypeAvro.MOTION);
        condition.setOperation(ConditionOperationAvro.EQUALS);
        condition.setValue(null);
        return condition;
    }

    private static DeviceActionAvro createActionAvro(String sensorId) {
        var action = new DeviceActionAvro();
        action.setSensorId(sensorId);
        action.setType(ActionTypeAvro.ACTIVATE);
        action.setValue(1);
        return action;
    }

    public static SpecificRecordBase createScenarioRemovedEventAvro() {
        var event = new ScenarioRemovedEventAvro();
        event.setName("name");
        return event;
    }

    public static HubEventProto createDeviceAddedEventProto() {
        var event = DeviceAddedEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setType(DeviceTypeProto.CLIMATE_SENSOR)
                .build();

        return HubEventProto.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setDeviceAdded(event)
                .build();

    }

    public static HubEventProto createDeviceRemovedEventProto() {
        var event = DeviceRemovedEventProto.newBuilder()
                .setId(SENSOR_ID)
                .build();

        return HubEventProto.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setDeviceRemoved(event)
                .build();
    }

    public static HubEventProto createScenarioAddedEventProto() {
        var event = ScenarioAddedEventProto.newBuilder()
                .setName("name")
                .addCondition(setScenarioConditionProto(SENSOR_ID + "1"))
                .addCondition(setScenarioConditionProto(SENSOR_ID + "2"))
                .addAction(setDeviceActionProto(SENSOR_ID + "1"))
                .addAction(setDeviceActionProto(SENSOR_ID + "2"))
                .build();

        return HubEventProto.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setScenarioAdded(event)
                .build();

    }

    private static ScenarioConditionProto setScenarioConditionProto(String sensorId) {
        return ScenarioConditionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ConditionTypeProto.MOTION)
                .setOperation(ConditionOperationProto.EQUALS)
                .build();
    }

    private static DeviceActionProto setDeviceActionProto(String sensorId) {
        return DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.ACTIVATE)
                .setValue(1)
                .build();
    }

    public static HubEventProto createScenarioRemovedEventProto() {
        var event = ScenarioRemovedEventProto.newBuilder()
                .setName("name")
                .build();

        return HubEventProto.newBuilder()
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setScenarioRemoved(event)
                .build();

    }
}
