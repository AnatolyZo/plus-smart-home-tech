package ru.practicum.telemetry.handlers.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.KafkaSenderService;
import ru.practicum.telemetry.mapper.SensorMapper;
import ru.practicum.telemetry.sensors.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {
    private final KafkaSenderService kafkaSenderService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        SensorEvent sensorEvent = SensorMapper.mapToTemperatureSensorEvent(event);
        kafkaSenderService.processSensorEvent(sensorEvent);
    }
}
