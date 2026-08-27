package ru.practicum.telemetry.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.TelemetryTopics;
import ru.practicum.telemetry.config.CommonConsumerProps;
import ru.practicum.telemetry.config.SnapshotConsumerProps;
import ru.practicum.telemetry.service.DataProcessorService;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final CommonConsumerProps commonProps;
    private final SnapshotConsumerProps snapshotProps;
    private final DataProcessorService dataProcessorService;

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final List<String> TOPICS = List.of(TelemetryTopics.TELEMETRY_SNAPSHOT_TOPIC);

    public void start() {
        Consumer<String, SpecificRecordBase> consumer = ConsumerCreator.createKafkaConsumer(commonProps, snapshotProps);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
            }

        } catch (WakeupException e) {

        } finally {
            try (consumer) {
                consumer.commitSync();
            }
        }
    }
}
