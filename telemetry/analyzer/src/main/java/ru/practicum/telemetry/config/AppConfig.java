package ru.practicum.telemetry.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CommonConsumerProps.class, SnapshotConsumerProps.class, HubConsumerProps.class})
public class AppConfig {
}
