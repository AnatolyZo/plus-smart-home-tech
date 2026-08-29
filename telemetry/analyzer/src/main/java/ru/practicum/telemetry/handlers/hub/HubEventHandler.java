package ru.practicum.telemetry.handlers.hub;

public interface HubEventHandler {
    Class<?> getPayloadClass();

    void handle(Object hubEvent);
}
