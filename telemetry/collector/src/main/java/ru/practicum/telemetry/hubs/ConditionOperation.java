package ru.practicum.telemetry.hubs;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;

public enum ConditionOperation {
    EQUALS(ConditionOperationAvro.EQUALS),
    GREATER_THAN(ConditionOperationAvro.GREATER_THAN),
    LOWER_THAN(ConditionOperationAvro.LOWER_THAN);

    private final ConditionOperationAvro conditionOperationAvro;

    ConditionOperation(ConditionOperationAvro conditionOperationAvro) {
        this.conditionOperationAvro = conditionOperationAvro;
    }

    public ConditionOperationAvro toAvro() {
        return conditionOperationAvro;
    }
}
