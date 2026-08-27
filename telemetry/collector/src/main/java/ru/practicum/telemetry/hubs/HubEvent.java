package ru.practicum.telemetry.hubs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class HubEvent {
    private String hubId;
    private Instant timestamp = Instant.now();

    public abstract HubEventType getType();
}
