package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.practicum.telemetry.mapper.SensorMapper;
import ru.practicum.telemetry.sensors.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import static org.assertj.core.api.Assertions.assertThat;

public class SensorMapperTests {

    @Test
    void climateSensorMappingTest() {
        SensorEvent event = TestSensorEntities.createClimateSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);
        SpecificRecordBase climateSensorEventAvro = TestSensorEntities.createClimateSensorEventAvro();
        SpecificRecordBase expectedEvent = TestSensorEntities.createTestSensorEventAvro(climateSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void lightSensorMappingTest() {
        SensorEvent event = TestSensorEntities.createLightSensorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);
        SpecificRecordBase lightSensorEventAvro = TestSensorEntities.createLightSensorEventAvro();
        SpecificRecordBase expectedEvent = TestSensorEntities.createTestSensorEventAvro(lightSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void motionSensorMappingTest() {
        SensorEvent event = TestSensorEntities.createMotionSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);
        SpecificRecordBase motionSensorEventAvro = TestSensorEntities.createMotionSensorEventAvro();
        SpecificRecordBase expectedEvent = TestSensorEntities.createTestSensorEventAvro(motionSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void switchSensorMappingTest() {
        SensorEvent event = TestSensorEntities.createSwitchSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);
        SpecificRecordBase switchSensorEventAvro = TestSensorEntities.createSwitchSensorEventAvro();
        SpecificRecordBase expectedEvent = TestSensorEntities.createTestSensorEventAvro(switchSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void temperatureSensorMappingTest() {
        SensorEvent event = TestSensorEntities.createTemperatureSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);
        SpecificRecordBase temperatureSensorEventAvro = TestSensorEntities.createTemperatureSensorEventAvro();
        SpecificRecordBase expectedEvent = TestSensorEntities.createTestSensorEventAvro(temperatureSensorEventAvro);

        SpecificRecordBase result = SensorMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void climateSensorMappingToSensorEventTest() {
        SensorEventProto event = TestSensorEntities.createClimateSensorEventProto();
        SensorEvent expectedEvent = TestSensorEntities.createClimateSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);

        SensorEvent result = SensorMapper.mapToClimateSensorEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void lightSensorMappingToSensorEventTest() {
        SensorEventProto event = TestSensorEntities.createLightSensorEventProto();
        SensorEvent expectedEvent = TestSensorEntities.createLightSensorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);

        SensorEvent result = SensorMapper.mapToLightSensorEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void motionSensorMappingToSensorEventTest() {
        SensorEventProto event = TestSensorEntities.createMotionSensorEventProto();
        SensorEvent expectedEvent = TestSensorEntities.createMotionSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);

        SensorEvent result = SensorMapper.mapToMotionSensorEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void switchSensorMappingToSensorEventTest() {
        SensorEventProto event = TestSensorEntities.createSwitchSensorEventProto();
        SensorEvent expectedEvent = TestSensorEntities.createSwitchSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);

        SensorEvent result = SensorMapper.mapToSwitchSensorEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void temperatureSensorMappingToSensorEventTest() {
        SensorEventProto event = TestSensorEntities.createTemperatureSensorEventProto();
        SensorEvent expectedEvent = TestSensorEntities.createTemperatureSenorEvent(TestSensorEntities.SENSOR_ID, TestSensorEntities.HUB_ID);

        SensorEvent result = SensorMapper.mapToTemperatureSensorEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }
}
