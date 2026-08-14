package ru.practicum.telemetry.hubs;

import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

public enum ConditionType {
    MOTION(ConditionTypeAvro.MOTION),
    LUMINOSITY(ConditionTypeAvro.LUMINOSITY),
    SWITCH(ConditionTypeAvro.SWITCH),
    TEMPERATURE(ConditionTypeAvro.TEMPERATURE),
    CO2LEVEL(ConditionTypeAvro.CO2LEVEL),
    HUMIDITY(ConditionTypeAvro.HUMIDITY);

    private final ConditionTypeAvro conditionTypeAvro;

    ConditionType(ConditionTypeAvro conditionTypeAvro) {
        this.conditionTypeAvro = conditionTypeAvro;
    }

    public ConditionTypeAvro toAvro() {
        return conditionTypeAvro;
    }
}
