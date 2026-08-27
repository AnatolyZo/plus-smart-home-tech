package ru.practicum.telemetry.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.telemetry.entities.Scenario;
import ru.practicum.telemetry.entities.Sensor;
import ru.practicum.telemetry.repositories.ScenarioRepository;
import ru.practicum.telemetry.repositories.SensorRepository;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Service
@RequiredArgsConstructor
@Transactional
public class DataProcessorServiceImpl implements DataProcessorService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    @Override
    public void addScenario(Scenario scenario) {
        scenarioRepository.save(scenario);
    }

    @Override
    public void removeScenario(Scenario scenario) {
        scenarioRepository.delete(scenario);
    }

    @Override
    public void addSensor(Sensor sensor) {
        sensorRepository.save(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        sensorRepository.delete(sensor);
    }
}
