package ru.yandex.practicum.order.feign;

import feign.FeignException;

public interface Retryable {
    default boolean isRetryable(FeignException e) {
        int status = e.status();
        return status >= 500 || status == -1;
    }
}
