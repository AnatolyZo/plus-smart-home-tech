package ru.practicum.telemetry.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.consumer.hub")
@Getter
@Setter
public class HubConsumerProps implements Props {
    private String clientId;
    private String groupId;
    private String valueDeserializer;
}
