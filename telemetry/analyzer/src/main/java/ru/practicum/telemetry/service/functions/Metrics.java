package ru.practicum.telemetry.service.functions;

import ru.practicum.telemetry.enums.ConditionOperation;
import ru.practicum.telemetry.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Metrics {
    public static final Map<ConditionType, Function<ClimateSensorAvro, Integer>> CLIMATE_SENSORS_METRICS = Map.of(
            ConditionType.TEMPERATURE, ClimateSensorAvro::getTemperatureC,
            ConditionType.HUMIDITY, ClimateSensorAvro::getHumidity,
            ConditionType.CO2LEVEL, ClimateSensorAvro::getCo2Level
    );

    public static final Map<ConditionType, Function<LightSensorAvro, Integer>> LIGHT_SENSORS_METRICS = Map.of(
            ConditionType.LUMINOSITY, LightSensorAvro::getLuminosity
    );

    public static final Map<ConditionType, Function<MotionSensorAvro, Boolean>> MOTION_SENSORS_METRICS = Map.of(
            ConditionType.MOTION, MotionSensorAvro::getMotion
    );

    public static final Map<ConditionType, Function<SwitchSensorAvro, Boolean>> SWITCH_SENSORS_METRICS = Map.of(
            ConditionType.SWITCH, SwitchSensorAvro::getState
    );

    public static final Map<ConditionType, Function<TemperatureSensorAvro, Integer>> TEMPERATURE_SENSORS_METRICS = Map.of(
            ConditionType.TEMPERATURE, TemperatureSensorAvro::getTemperatureC
    );

    public static final Map<ConditionOperation, BiFunction<Integer, Integer, Boolean>> COMPARATORS = Map.of(
            ConditionOperation.EQUALS, (sensorsData,conditionValue) -> sensorsData.equals(conditionValue),
            ConditionOperation.GREATER_THAN, (sensorsData,conditionValue) -> sensorsData > conditionValue,
            ConditionOperation.LOWER_THAN, (sensorsData,conditionValue) -> sensorsData < conditionValue
    );
}
