package ru.practicum.telemetry.enums;

import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

public enum ActionType {
    ACTIVATE(ActionTypeAvro.ACTIVATE, ActionTypeProto.ACTIVATE),
    DEACTIVATE(ActionTypeAvro.DEACTIVATE, ActionTypeProto.DEACTIVATE),
    INVERSE(ActionTypeAvro.INVERSE, ActionTypeProto.INVERSE),
    SET_VALUE(ActionTypeAvro.SET_VALUE, ActionTypeProto.SET_VALUE);

    private final ActionTypeAvro actionTypeAvro;
    private final ActionTypeProto actionTypeProto;

    ActionType(ActionTypeAvro actionTypeAvro, ActionTypeProto actionTypeProto) {
        this.actionTypeAvro = actionTypeAvro;
        this.actionTypeProto = actionTypeProto;
    }

    public ActionTypeAvro toAvro() {
        return actionTypeAvro;
    }

    public ActionTypeProto toProto() {
        return actionTypeProto;
    }
}
