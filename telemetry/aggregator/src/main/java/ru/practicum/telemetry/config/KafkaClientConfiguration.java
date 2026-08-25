package ru.practicum.telemetry.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.telemetry.SensorEventDeserializer;
import ru.practicum.telemetry.TelemetryAvroSerializer;

import java.util.Properties;

@Configuration
public class KafkaClientConfiguration {

    @Bean
    public Producer<String, SpecificRecordBase> kafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TelemetryAvroSerializer.class.getName());
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "aggregator-producer");

        return new KafkaProducer<>(config);
    }

    @Bean
    public Consumer<String, SpecificRecordBase> kafkaConsumer() {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, "aggregator-consumer");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator.group");

        return new KafkaConsumer<>(config);
    }
}
