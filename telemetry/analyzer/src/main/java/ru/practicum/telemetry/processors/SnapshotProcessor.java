package ru.practicum.telemetry.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.TelemetryTopics;
import ru.practicum.telemetry.service.SnapshotProcessorService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

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

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    SpecificRecordBase base = record.value();

                    if (!(base instanceof SensorsSnapshotAvro)) {
                        log.warn("Получен неизвестный тип записи {}", base.getClass());
                        throw new IllegalArgumentException("Получен неизвестный тип записи " + base.getClass());
                    }

                    SensorsSnapshotAvro sensorsSnapshotAvro = (SensorsSnapshotAvro) base;

                    try {
                        snapshotProcessorService.processSnapshot(sensorsSnapshotAvro);
                        consumer.commitAsync();
                    } catch (Exception e) {
                        log.error("Ошибка обработки снапшота, offset = {}, key = {}", record.offset(), record.key(), e);
                        break;
                    }
                }
            }
        } catch (WakeupException e) {

        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
