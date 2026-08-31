package ru.practicum.telemetry.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.TelemetryTopics;
import ru.practicum.telemetry.service.SnapshotProcessorService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SnapshotProcessor {
    private final Consumer<String, SpecificRecordBase> consumer;
    private final SnapshotProcessorService snapshotProcessorService;

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final List<String> TOPICS = List.of(TelemetryTopics.TELEMETRY_SNAPSHOT_TOPIC);

    public SnapshotProcessor(@Qualifier("snapshotConsumer") Consumer<String, SpecificRecordBase> consumer,
                             SnapshotProcessorService snapshotProcessorService) {
        this.consumer = consumer;
        this.snapshotProcessorService = snapshotProcessorService;
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    try {
                        SpecificRecordBase base = record.value();

                        if (base == null) {
                            log.warn("Переданное сообщение пусто");
                            offsets.put(
                                    new TopicPartition(record.topic(), record.partition()),
                                    new OffsetAndMetadata(record.offset() + 1)
                            );
                            continue;
                        }

                        if (!(base instanceof SensorsSnapshotAvro)) {
                            log.warn("Получен неизвестный тип записи {}", base.getClass());
                            offsets.put(
                                    new TopicPartition(record.topic(), record.partition()),
                                    new OffsetAndMetadata(record.offset() + 1)
                            );
                            continue;
                        }

                        SensorsSnapshotAvro sensorsSnapshotAvro = (SensorsSnapshotAvro) base;

                        snapshotProcessorService.processSnapshot(sensorsSnapshotAvro);
                        offsets.put(
                                new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)
                        );
                    } catch (Exception e) {
                        log.error("Ошибка обработки снапшота, offset = {}, key = {}", record.offset(), record.key(), e);
                        break;
                    }
                }

                if (!offsets.isEmpty()) {
                    consumer.commitSync(offsets);
                }
            }
        } catch (WakeupException e) {

        } finally {
            consumer.close();
        }
    }
}
