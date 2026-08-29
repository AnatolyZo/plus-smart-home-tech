package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotFormerTests {
    private static final Instant timestamp = Instant.now();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";

    @Test
    void addEventWithNoSnapshotsExist() {
        SnapshotFormer snapshotFormer = new SnapshotFormer();

        SpecificRecordBase light = createLightSensorEventAvro(20, 30);
        SensorEventAvro event = (SensorEventAvro) createTestSensorEventAvro(light, timestamp);
        SensorsSnapshotAvro expectedSnapshot = createTestSensorsSnapshotAvro(timestamp, 20, 30);

        Optional<SensorsSnapshotAvro> snapshotAvroOptional = snapshotFormer.updateState(event);
        boolean result = snapshotAvroOptional.isPresent();

        assertThat(result).isEqualTo(true);
        assertThat(snapshotAvroOptional.get()).isEqualTo(expectedSnapshot);
    }

    @Test
    void updateSnapshotWithOldTimestamp() {
        SnapshotFormer snapshotFormer = new SnapshotFormer();

        SpecificRecordBase lightSensor = createLightSensorEventAvro(20, 30);
        SensorEventAvro event = (SensorEventAvro) createTestSensorEventAvro(lightSensor, timestamp);

        snapshotFormer.updateState(event);

        SpecificRecordBase oldLightSensor = createLightSensorEventAvro(30, 40);
        SensorEventAvro oldEvent = (SensorEventAvro) createTestSensorEventAvro(oldLightSensor, timestamp.minusSeconds(10));

        Optional<SensorsSnapshotAvro> snapshotAvroOptional = snapshotFormer.updateState(oldEvent);
        boolean result = snapshotAvroOptional.isEmpty();

        assertThat(result).isEqualTo(true);
    }

    @Test
    void updateSnapshotWithNewestTimestamp() {
        SnapshotFormer snapshotFormer = new SnapshotFormer();

        SpecificRecordBase lightSensor = createLightSensorEventAvro(20, 30);
        SensorEventAvro event = (SensorEventAvro) createTestSensorEventAvro(lightSensor, timestamp);

        snapshotFormer.updateState(event);

        SpecificRecordBase newLightSensor = createLightSensorEventAvro(30, 40);
        SensorEventAvro newEvent = (SensorEventAvro) createTestSensorEventAvro(newLightSensor, timestamp.plusSeconds(10));

        Optional<SensorsSnapshotAvro> snapshotAvroOptional = snapshotFormer.updateState(newEvent);
        boolean result = snapshotAvroOptional.isPresent();

        assertThat(result).isEqualTo(true);

        SensorsSnapshotAvro expectedSnapshot = createTestSensorsSnapshotAvro(timestamp.plusSeconds(10), 30, 40);

        assertThat(snapshotAvroOptional.get()).isEqualTo(expectedSnapshot);
    }

    private <T extends SpecificRecordBase> SpecificRecordBase createTestSensorEventAvro(T sensorEvent, Instant timestamp) {
        var eventAvro = new SensorEventAvro();
        eventAvro.setId(SENSOR_ID);
        eventAvro.setHubId(HUB_ID);
        eventAvro.setTimestamp(timestamp);
        eventAvro.setPayload(sensorEvent);
        return eventAvro;
    }

    private SpecificRecordBase createLightSensorEventAvro(int linkQuality, int luminosity) {
        var event = new LightSensorAvro();
        event.setLinkQuality(linkQuality);
        event.setLuminosity(luminosity);
        return event;
    }

    private SensorsSnapshotAvro createTestSensorsSnapshotAvro(Instant sensorStateTimestamp, int linkQuality, int luminosity) {
        SensorStateAvro sensorStateAvro = createTestSensorStateAvro(sensorStateTimestamp, linkQuality, luminosity);
        Map<String, SensorStateAvro> stateMap = new HashMap<>();
        stateMap.put(SENSOR_ID, sensorStateAvro);

        var snapshot = new SensorsSnapshotAvro();
        snapshot.setHubId(HUB_ID);
        snapshot.setTimestamp(sensorStateTimestamp);
        snapshot.setSensorsState(stateMap);

        return snapshot;
    }

    private SensorStateAvro createTestSensorStateAvro(Instant timestamp, int linkQuality, int luminosity) {
        var state = new SensorStateAvro();
        state.setTimestamp(timestamp);
        state.setData(createLightSensorEventAvro(linkQuality, luminosity));

        return state;
    }
}
