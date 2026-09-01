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
import ru.practicum.telemetry.handlers.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HubEventProcessor implements Runnable {
    private final Consumer<String, SpecificRecordBase> consumer;
    private final Map<Class<?>, HubEventHandler> hubEventHandlers;

    public HubEventProcessor(@Qualifier("hubConsumer") Consumer<String, SpecificRecordBase> consumer,
                             Set<HubEventHandler> hubEventHandlers) {
        this.consumer = consumer;
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getPayloadClass, Function.identity()));
    }

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final List<String> TOPICS = List.of(TelemetryTopics.TELEMETRY_HUBS_TOPIC);

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    try {
                        SpecificRecordBase base = record.value();

                        if (base == null) {
                            log.warn("Переданное сообщение пусто");
                            continue;
                        }

                        if (!(base instanceof HubEventAvro)) {
                            log.warn("Неизвестный тип записи {}", base.getClass());
                            continue;
                        }

                        HubEventAvro hubEventAvro = (HubEventAvro) base;
                        Object payload = hubEventAvro.getPayload();

                        if (payload == null) {
                            log.warn("Отсутствует поле payload у объекта {}", hubEventAvro);
                            continue;
                        }

                        Class<?> eventClass = payload.getClass();

                        if (hubEventHandlers.containsKey(eventClass)) {
                            hubEventHandlers.get(eventClass).handle(hubEventAvro);
                        } else {
                            log.warn("Не могу найти обработчик для события {}", hubEventAvro.getPayload());
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка обработки записи, offset = {}, key = {}", record.offset(), record.key(), e);
                    }
                }

                consumer.commitSync();
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
