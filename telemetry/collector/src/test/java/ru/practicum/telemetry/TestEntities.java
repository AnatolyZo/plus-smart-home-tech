package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.hubs.*;
import ru.practicum.telemetry.sensors.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

public class TestEntities {
    private static final Instant timestamp = Instant.now();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";
    public static final String JSON_SENSOR_EVENT_WITHOUT_TYPE = """
            {
              "id": "%s",
              "hubId": "%s",
              "timestamp": "%s",
              "linkQuality": 234,
              "luminosity": 123
            }
            """.formatted(SENSOR_ID, HUB_ID, Instant.now().toString());
    public static final String JSON_HUB_EVENT_WITHOUT_TYPE = """
            {
              "hubId": "%s",
              "timestamp": "%s",
              "id": "device_id",
              "deviceType": "CLIMATE_SENSOR",
            }
            """.formatted(HUB_ID, Instant.now().toString());

    public static SensorEvent createLightSensorEvent(String id, String hubId) {
        var event = new LightSensorEvent();
        setSensorEvent(id, hubId, event);
        event.setLinkQuality(234);
        event.setLuminosity(123);
        return event;
    }

    public static SensorEvent createClimateSenorEvent(String id, String hubId) {
        var event = new ClimateSensorEvent();
        setSensorEvent(id, hubId, event);
        event.setTemperatureC(23);
        event.setCo2Level(12);
        event.setHumidity(15);
        return event;
    }

    public static SensorEvent createMotionSenorEvent(String id, String hubId) {
        var event = new MotionSensorEvent();
        setSensorEvent(id, hubId, event);
        event.setLinkQuality(23);
        event.setMotion(true);
        event.setVoltage(220);
        return event;
    }

    public static SensorEvent createSwitchSenorEvent(String id, String hubId) {
        var event = new SwitchSensorEvent();
        setSensorEvent(id, hubId, event);
        event.setState(true);
        return event;
    }

    public static SensorEvent createTemperatureSenorEvent(String id, String hubId) {
        var event = new TemperatureSensorEvent();
        setSensorEvent(id, hubId, event);
        event.setTemperatureC(23);
        event.setTemperatureF(87);
        return event;
    }

    private static void setSensorEvent(String id, String hubId, SensorEvent event) {
        event.setId(id);
        event.setHubId(hubId);
        event.setTimestamp(timestamp);
    }

    public static HubEvent createDeviceAddedEvent(String hubId) {
        var event = new DeviceAddedEvent();
        setHubEvent(hubId, event);
        event.setId("device_id");
        event.setDeviceType(DeviceType.CLIMATE_SENSOR);
        return event;
    }

    public static HubEvent createDeviceRemovedEvent(String hubId) {
        var event = new DeviceRemovedEvent();
        setHubEvent(hubId, event);
        event.setId("device_id");
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
        action.setValue(null);
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

    public static <T extends SpecificRecordBase> SpecificRecordBase createTestSensorEventAvro(T sensorEvent) {
        var eventAvro = new SensorEventAvro();
        eventAvro.setId(SENSOR_ID);
        eventAvro.setHubId(HUB_ID);
        eventAvro.setTimestamp(timestamp);
        eventAvro.setPayload(sensorEvent);
        return eventAvro;
    }

    public static SpecificRecordBase createLightSensorEventAvro() {
        var event = new LightSensorAvro();
        event.setLinkQuality(234);
        event.setLuminosity(123);
        return event;
    }

    public static SpecificRecordBase createClimateSensorEventAvro() {
        var event = new ClimateSensorAvro();
        event.setTemperatureC(23);
        event.setCo2Level(12);
        event.setHumidity(15);
        return event;
    }

    public static SpecificRecordBase createMotionSensorEventAvro() {
        var event = new MotionSensorAvro();
        event.setLinkQuality(23);
        event.setMotion(true);
        event.setVoltage(220);
        return event;
    }

    public static SpecificRecordBase createSwitchSensorEventAvro() {
        var event = new SwitchSensorAvro();
        event.setState(true);
        return event;
    }

    public static SpecificRecordBase createTemperatureSensorEventAvro() {
        var event = new TemperatureSensorAvro();
        event.setTemperatureC(23);
        event.setTemperatureF(87);
        return event;
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
        event.setId("device_id");
        event.setType(DeviceTypeAvro.CLIMATE_SENSOR);
        return event;
    }

    public static SpecificRecordBase createDeviceRemovedEventAvro() {
        var event = new DeviceRemovedEventAvro();
        event.setId("device_id");
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
        action.setValue(null);
        return action;
    }

    public static SpecificRecordBase createScenarioRemovedEventAvro() {
        var event = new ScenarioRemovedEventAvro();
        event.setName("name");
        return event;
    }
}
