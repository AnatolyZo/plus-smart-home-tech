package ru.practicum.telemetry.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.TelemetryTopics;
import ru.practicum.telemetry.config.CommonConsumerProps;
import ru.practicum.telemetry.config.HubConsumerProps;
import ru.practicum.telemetry.handlers.HubEventHandler;
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
    private final CommonConsumerProps commonProps;
    private final HubConsumerProps hubProps;
    private final Map<Class<?>, HubEventHandler> hubEventHandlers;

    public HubEventProcessor(CommonConsumerProps commonProps,
                             HubConsumerProps hubProps,
                             Set<HubEventHandler> hubEventHandlers) {
        this.commonProps = commonProps;
        this.hubProps = hubProps;
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getPayloadClass, Function.identity()));
    }

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final List<String> TOPICS = List.of(TelemetryTopics.TELEMETRY_HUBS_TOPIC);

    @Override
    public void run() {
        Consumer<String, SpecificRecordBase> consumer = ConsumerCreator.createKafkaConsumer(commonProps, hubProps);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(TOPICS);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    SpecificRecordBase base = record.value();

                    if (!(base instanceof HubEventAvro)) {
                        throw new IllegalArgumentException("Неизвестный тип записи " + base.getClass());
                    }

                    HubEventAvro hubEventAvro = (HubEventAvro) base;
                    Object payload = hubEventAvro.getPayload();

                    if (payload == null) {
                        throw new IllegalArgumentException("Отсутствует поле payload у объекта " + hubEventAvro);
                    }

                    Class<?> eventClass = payload.getClass();

                    if (hubEventHandlers.containsKey(eventClass)) {
                        hubEventHandlers.get(eventClass).handle(payload);
                    } else {
                        throw new IllegalArgumentException("Не могу найти обработчик для события " + hubEventAvro.getPayload());
                    }
                }
            }

        } catch (WakeupException e) {

        } finally {
            try (consumer) {
                consumer.commitSync();
            }
        }
    }
}
