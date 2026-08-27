package ru.practicum.telemetry.service;

import ru.practicum.telemetry.entities.Scenario;
import ru.practicum.telemetry.entities.Sensor;

public interface DataProcessorService {
    void addScenario(Scenario scenario);

    void removeScenario(Scenario scenario);

    void addSensor(Sensor sensor);

    void removeSensor(Sensor sensor);
}
