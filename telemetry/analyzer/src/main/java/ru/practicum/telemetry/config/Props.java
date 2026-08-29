package ru.practicum.telemetry.config;

public interface Props {
    String getClientId();

    String getGroupId();

    String getValueDeserializer();

    String getAutoOffsetReset();

    String getMaxPollRecords();

    String getMaxPollInterval();
}
