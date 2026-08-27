package ru.practicum.telemetry.handlers;

public interface HubEventHandler {
    Class<?> getPayloadClass();

    void handle(Object payload);
}
