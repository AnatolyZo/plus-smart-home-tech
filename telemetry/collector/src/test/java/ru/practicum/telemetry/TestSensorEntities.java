package ru.practicum.telemetry;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.sensors.*;
import ru.practicum.telemetry.sensors.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

public class TestSensorEntities {
    private static final Instant timestamp = Instant.now();
    private static final Timestamp timestampProto = Timestamp.newBuilder()
            .setSeconds(timestamp.getEpochSecond())
            .setNanos(timestamp.getNano())
            .build();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";

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

    public static SensorEventProto createClimateSensorEventProto() {
        var sensor = ClimateSensorProto.newBuilder()
                .setTemperatureC(23)
                .setCo2Level(12)
                .setHumidity(15)
                .build();

        return SensorEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setClimateSensor(sensor)
                .build();
    }

    public static SensorEventProto createLightSensorEventProto() {
        var sensor = LightSensorProto.newBuilder()
                .setLinkQuality(234)
                .setLuminosity(123)
                .build();

        return SensorEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setLightSensor(sensor)
                .build();
    }

    public static SensorEventProto createMotionSensorEventProto() {
        var sensor = MotionSensorProto.newBuilder()
                .setLinkQuality(23)
                .setMotion(true)
                .setVoltage(220)
                .build();

        return SensorEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setMotionSensor(sensor)
                .build();
    }

    public static SensorEventProto createSwitchSensorEventProto() {
        var sensor = SwitchSensorProto.newBuilder()
                .setState(true)
                .build();

        return SensorEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setSwitchSensor(sensor)
                .build();
    }

    public static SensorEventProto createTemperatureSensorEventProto() {
        var sensor = TemperatureSensorProto.newBuilder()
                .setTemperatureC(23)
                .setTemperatureF(87)
                .build();

        return SensorEventProto.newBuilder()
                .setId(SENSOR_ID)
                .setHubId(HUB_ID)
                .setTimestamp(timestampProto)
                .setTemperatureSensor(sensor)
                .build();
    }
}
