package ru.yandex.practicum.order.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class FeignRetryConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 500, 3);
    }
}
