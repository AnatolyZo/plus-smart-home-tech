package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class SensorEventDeserializerTests {
    private static final String TEST_TOPIC = "test-topic";
    private static final Instant timestamp = Instant.now();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";

    @Test
    void sensorEventDeserializerTest() throws Exception {
        SpecificRecordBase climateSensorEventAvro = createClimateSensorEventAvro();
        SpecificRecordBase sensorEventOriginal = createTestSensorEventAvro(climateSensorEventAvro);


        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();
        byte[] bytes = serializer.serialize(TEST_TOPIC, sensorEventOriginal);
        assertThat(bytes).isNotEmpty();

        SensorEventDeserializer deserializer = new SensorEventDeserializer();
        SpecificRecordBase sensorEventRestored = deserializer.deserialize(TEST_TOPIC, bytes);
        assertThat(sensorEventRestored).isEqualTo(sensorEventOriginal);
    }

    private static SpecificRecordBase createClimateSensorEventAvro() {
        var event = new ClimateSensorAvro();
        event.setTemperatureC(23);
        event.setCo2Level(12);
        event.setHumidity(15);
        return event;
    }

    private static <T extends SpecificRecordBase> SpecificRecordBase createTestSensorEventAvro(T sensorEvent) {
        var eventAvro = new SensorEventAvro();
        eventAvro.setId(SENSOR_ID);
        eventAvro.setHubId(HUB_ID);
        eventAvro.setTimestamp(timestamp);
        eventAvro.setPayload(sensorEvent);
        return eventAvro;
    }
}
