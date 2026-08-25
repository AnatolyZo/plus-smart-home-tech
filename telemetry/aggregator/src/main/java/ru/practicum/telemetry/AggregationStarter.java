package ru.practicum.telemetry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStarter {
    private final Consumer<String, SpecificRecordBase> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final SnapshotFormer snapshotFormer;

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final List<String> TOPICS = List.of(TelemetryTopics.TELEMETRY_SENSORS_TOPIC);

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    SpecificRecordBase rawValue = record.value();

                    if (rawValue instanceof SensorEventAvro event) {
                        Optional<SensorsSnapshotAvro> snapshotOpt = snapshotFormer.updateState(event);

                        if (snapshotOpt.isPresent()) {
                            SensorsSnapshotAvro snapshot = snapshotOpt.get();
                            ProducerRecord<String, SpecificRecordBase> snapshotRecord = new ProducerRecord<>(TelemetryTopics.TELEMETRY_SNAPSHOT_TOPIC, snapshot.getHubId(), snapshot);
                            producer.send(snapshotRecord);
                        }
                    }
                }

                consumer.commitAsync();
            }
        } catch (WakeupException e) {

        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                producer.close();
                consumer.close();
            }
        }
    }
}
