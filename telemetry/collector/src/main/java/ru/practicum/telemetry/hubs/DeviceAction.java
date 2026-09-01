package ru.practicum.telemetry.hubs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.telemetry.enums.ActionType;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Object value;
}
