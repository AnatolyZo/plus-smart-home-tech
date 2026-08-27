package ru.practicum.telemetry.enums;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

public enum ActionType {
    ACTIVATE(ActionTypeAvro.ACTIVATE),
    DEACTIVATE(ActionTypeAvro.DEACTIVATE),
    INVERSE(ActionTypeAvro.INVERSE),
    SET_VALUE(ActionTypeAvro.SET_VALUE);

    private final ActionTypeAvro actionTypeAvro;

    ActionType(ActionTypeAvro actionTypeAvro) {
        this.actionTypeAvro = actionTypeAvro;
    }

    public ActionTypeAvro toAvro() {
        return actionTypeAvro;
    }
}
