package ru.practicum.telemetry.processors;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ru.practicum.telemetry.config.CommonConsumerProps;
import ru.practicum.telemetry.config.Props;

import java.util.Properties;

public class ConsumerCreator {
    public static <T extends Props> Consumer<String, SpecificRecordBase> createKafkaConsumer(CommonConsumerProps commonProps, T props) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, commonProps.getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, commonProps.getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, props.getValueDeserializer());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, props.getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, props.getGroupId());

        return new KafkaConsumer<>(config);
    }
}
