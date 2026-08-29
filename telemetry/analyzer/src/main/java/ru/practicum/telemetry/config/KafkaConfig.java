package ru.practicum.telemetry.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final CommonConsumerProps commonProps;
    private final HubConsumerProps hubProps;
    private final SnapshotConsumerProps snapshotProps;

    @Bean("hubConsumer")
    public Consumer<String, SpecificRecordBase> hubConsumer() {
        return createKafkaConsumer(commonProps, hubProps);
    }

    @Bean("snapshotConsumer")
    public Consumer<String, SpecificRecordBase> snapshotConsumer() {
        return createKafkaConsumer(commonProps, snapshotProps);
    }

    private <T extends Props> Consumer<String, SpecificRecordBase> createKafkaConsumer(CommonConsumerProps commonProps, T props) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, commonProps.getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, commonProps.getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, props.getValueDeserializer());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, commonProps.getEnableAutoCommit());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, props.getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, props.getGroupId());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getAutoOffsetReset());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, props.getMaxPollRecords());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, props.getMaxPollInterval());

        return new KafkaConsumer<>(config);
    }
}
