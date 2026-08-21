package ru.practicum.telemetry.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.telemetry.sensors.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

public class SensorMapper {
    public static SpecificRecordBase toAvro(SensorEvent sensorEvent) {
        if (sensorEvent.getType().equals(SensorEventType.CLIMATE_SENSOR_EVENT)) {
            return mapClimateSensor((ClimateSensorEvent) sensorEvent);
        } else if (sensorEvent.getType().equals(SensorEventType.LIGHT_SENSOR_EVENT)) {
            return mapLightSensor((LightSensorEvent) sensorEvent);
        } else if (sensorEvent.getType().equals(SensorEventType.MOTION_SENSOR_EVENT)) {
            return mapMotionSensor((MotionSensorEvent) sensorEvent);
        } else if (sensorEvent.getType().equals(SensorEventType.SWITCH_SENSOR_EVENT)) {
            return mapSwitchSensor((SwitchSensorEvent) sensorEvent);
        } else if (sensorEvent.getType().equals(SensorEventType.TEMPERATURE_SENSOR_EVENT)) {
            return mapTemperatureSensor((TemperatureSensorEvent) sensorEvent);
        }

        throw new IllegalArgumentException("Передан неизвестный тип датчика " + sensorEvent.getClass());
    }

    private static SpecificRecordBase mapClimateSensor(ClimateSensorEvent e) {
        var payload = new ClimateSensorAvro();
        payload.setTemperatureC(e.getTemperatureC());
        payload.setHumidity(e.getHumidity());
        payload.setCo2Level(e.getCo2Level());
        return setSensorEventAvro(e, payload);
    }

    private static SpecificRecordBase mapLightSensor(LightSensorEvent e) {
        var payload = new LightSensorAvro();
        payload.setLinkQuality(e.getLinkQuality());
        payload.setLuminosity(e.getLuminosity());
        return setSensorEventAvro(e, payload);
    }

    private static SpecificRecordBase mapMotionSensor(MotionSensorEvent e) {
        var payload = new MotionSensorAvro();
        payload.setLinkQuality(e.getLinkQuality());
        payload.setMotion(e.isMotion());
        payload.setVoltage(e.getVoltage());
        return setSensorEventAvro(e, payload);
    }

    private static SpecificRecordBase mapSwitchSensor(SwitchSensorEvent e) {
        var payload = new SwitchSensorAvro();
        payload.setState(e.isState());
        return setSensorEventAvro(e, payload);
    }

    private static SpecificRecordBase mapTemperatureSensor(TemperatureSensorEvent e) {
        var payload = new TemperatureSensorAvro();
        payload.setTemperatureC(e.getTemperatureC());
        payload.setTemperatureF(e.getTemperatureF());
        return setSensorEventAvro(e, payload);
    }

    private static <T extends SpecificRecordBase> SpecificRecordBase setSensorEventAvro(SensorEvent sensorEvent, T payload) {
        var sensorEventAvro = new SensorEventAvro();
        sensorEventAvro.setId(sensorEvent.getId());
        sensorEventAvro.setHubId(sensorEvent.getHubId());
        sensorEventAvro.setTimestamp(sensorEvent.getTimestamp());
        sensorEventAvro.setPayload(payload);
        return sensorEventAvro;
    }
}
