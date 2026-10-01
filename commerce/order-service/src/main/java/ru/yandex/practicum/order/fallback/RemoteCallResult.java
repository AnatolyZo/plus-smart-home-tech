package ru.yandex.practicum.order.fallback;

public sealed interface RemoteCallResult<T>
        permits RemoteCallResult.Success,
        RemoteCallResult.BusinessFailure,
        RemoteCallResult.TechnicalFailure {

    record Success<T>(T value) implements RemoteCallResult<T> { }

    record BusinessFailure<T>(String message) implements RemoteCallResult<T> { }

    record TechnicalFailure<T>(String reason) implements RemoteCallResult<T> { }
}
