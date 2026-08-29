package ru.practicum.telemetry.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.consumer.common")
@Getter
@Setter
public class CommonConsumerProps {
    private String bootstrapServers;
    private String keyDeserializer;
    private String enableAutoCommit;
}
