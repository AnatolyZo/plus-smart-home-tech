package ru.practicum.telemetry.hubs;

import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

public enum DeviceType {
    MOTION_SENSOR(DeviceTypeAvro.MOTION_SENSOR),
    TEMPERATURE_SENSOR(DeviceTypeAvro.TEMPERATURE_SENSOR),
    LIGHT_SENSOR(DeviceTypeAvro.LIGHT_SENSOR),
    CLIMATE_SENSOR(DeviceTypeAvro.CLIMATE_SENSOR),
    SWITCH_SENSOR(DeviceTypeAvro.SWITCH_SENSOR);

    private final DeviceTypeAvro deviceTypeAvro;

    DeviceType(DeviceTypeAvro deviceTypeAvro) {
        this.deviceTypeAvro = deviceTypeAvro;
    }

    public DeviceTypeAvro toAvro() {
        return deviceTypeAvro;
    }
}
