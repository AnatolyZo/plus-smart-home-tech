package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.practicum.telemetry.mapper.SensorMapper;
import ru.practicum.telemetry.sensors.SensorEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class SensorMapperTests {

    @Test
    void climateSensorMappingTest() {
        SensorEvent event = TestEntities.createClimateSenorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
        SpecificRecordBase climateSensorEventAvro = TestEntities.createClimateSensorEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestSensorEventAvro(climateSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void lightSensorMappingTest() {
        SensorEvent event = TestEntities.createLightSensorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
        SpecificRecordBase lightSensorEventAvro = TestEntities.createLightSensorEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestSensorEventAvro(lightSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void motionSensorMappingTest() {
        SensorEvent event = TestEntities.createMotionSenorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
        SpecificRecordBase motionSensorEventAvro = TestEntities.createMotionSensorEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestSensorEventAvro(motionSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void switchSensorMappingTest() {
        SensorEvent event = TestEntities.createSwitchSenorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
        SpecificRecordBase switchSensorEventAvro = TestEntities.createSwitchSensorEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestSensorEventAvro(switchSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void temperatureSensorMappingTest() {
        SensorEvent event = TestEntities.createTemperatureSenorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
        SpecificRecordBase temperatureSensorEventAvro = TestEntities.createTemperatureSensorEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestSensorEventAvro(temperatureSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }
}
