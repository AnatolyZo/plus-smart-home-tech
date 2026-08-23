//package ru.practicum.telemetry;
//
//import org.apache.avro.Schema;
//import org.apache.avro.io.BinaryDecoder;
//import org.apache.avro.io.DatumReader;
//import org.apache.avro.io.DecoderFactory;
//import org.apache.avro.specific.SpecificDatumReader;
//import org.apache.avro.specific.SpecificRecordBase;
//import org.junit.jupiter.api.Test;
//
//import java.io.ByteArrayInputStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class TelemetryAvroSerializerTests {
//    private static final String TEST_TOPIC = "test-topic";
//
//    @Test
//    void telemetryAvroSerializerTestWithSensorEvent() throws Exception {
//        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();
//
//        SpecificRecordBase climateSensorEventAvro = TestEntities.createClimateSensorEventAvro();
//        SpecificRecordBase sensorEvent = TestEntities.createTestSensorEventAvro(climateSensorEventAvro);
//
//        byte[] bytes = serializer.serialize(TEST_TOPIC, sensorEvent);
//        assertThat(bytes).isNotEmpty();
//
//        Schema schema = sensorEvent.getSchema();
//        DatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(schema);
//
//        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
//            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
//            SpecificRecordBase restored = reader.read(null, decoder);
//
//            assertThat(restored).isEqualTo(sensorEvent);
//        }
//    }
//
//    @Test
//    void telemetryAvroSerializerTestWithHubEvent() throws Exception {
//        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();
//
//        SpecificRecordBase deviceAddedAvro = TestEntities.createDeviceAddedEventAvro();
//        SpecificRecordBase hubEvent = TestEntities.createTestHubEventAvro(deviceAddedAvro);
//
//        byte[] bytes = serializer.serialize(TEST_TOPIC, hubEvent);
//        assertThat(bytes).isNotEmpty();
//
//        Schema schema = hubEvent.getSchema();
//        DatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(schema);
//
//        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
//            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
//            SpecificRecordBase restored = reader.read(null, decoder);
//
//            assertThat(restored).isEqualTo(hubEvent);
//        }
//    }
//
//    @Test
//    void telemetryAvroSerializerTestWithEmptyArray() {
//        TelemetryAvroSerializer serializer = new TelemetryAvroSerializer();
//        byte[] bytes = serializer.serialize(TEST_TOPIC, null);
//        assertThat(bytes).isEmpty();
//    }
//}
