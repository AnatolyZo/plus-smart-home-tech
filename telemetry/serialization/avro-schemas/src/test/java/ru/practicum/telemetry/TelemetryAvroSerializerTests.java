package ru.practicum.telemetry;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class TelemetryAvroSerializerTests {
    private static final String TEST_TOPIC = "test-topic";
    private static final Instant timestamp = Instant.now();
    public static final String SENSOR_ID = "sensor_id";
    public static final String HUB_ID = "hub-1";

    @Test
    void telemetryAvroSerializerTestWithSensorEvent() throws Exception {
        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();

        SpecificRecordBase climateSensorEventAvro = createClimateSensorEventAvro();
        SpecificRecordBase sensorEvent = createTestSensorEventAvro(climateSensorEventAvro);

        byte[] bytes = serializer.serialize(TEST_TOPIC, sensorEvent);
        assertThat(bytes).isNotEmpty();

        Schema schema = sensorEvent.getSchema();
        DatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(schema);

        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
            SpecificRecordBase restored = reader.read(null, decoder);

            assertThat(restored).isEqualTo(sensorEvent);
        }
    }

    @Test
    void telemetryAvroSerializerTestWithHubEvent() throws Exception {
        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();

        SpecificRecordBase deviceAddedAvro = createDeviceAddedEventAvro();
        SpecificRecordBase hubEvent = createTestHubEventAvro(deviceAddedAvro);

        byte[] bytes = serializer.serialize(TEST_TOPIC, hubEvent);
        assertThat(bytes).isNotEmpty();

        Schema schema = hubEvent.getSchema();
        DatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(schema);

        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
            SpecificRecordBase restored = reader.read(null, decoder);

            assertThat(restored).isEqualTo(hubEvent);
        }
    }

    @Test
    void telemetryAvroSerializerTestWithEmptyArray() {
        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();
        byte[] bytes = serializer.serialize(TEST_TOPIC, null);
        assertThat(bytes).isEmpty();
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

    private static <T extends SpecificRecordBase> SpecificRecordBase createTestHubEventAvro(T hubEvent) {
        var event = new HubEventAvro();
        event.setHubId(HUB_ID);
        event.setTimestamp(timestamp);
        event.setPayload(hubEvent);
        return event;
    }

    private static SpecificRecordBase createDeviceAddedEventAvro() {
        var event = new DeviceAddedEventAvro();
        event.setId("device_id");
        event.setType(DeviceTypeAvro.CLIMATE_SENSOR);
        return event;
    }
}
