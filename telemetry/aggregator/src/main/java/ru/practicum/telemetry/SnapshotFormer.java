package ru.practicum.telemetry;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SnapshotFormer {
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot;

        if (snapshots.get(event.getHubId()) == null) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(event.getHubId());
            snapshot.setSensorsState(new HashMap<>());
            snapshots.put(event.getHubId(), snapshot);
        } else {
            snapshot = snapshots.get(event.getHubId());
        }

        if (snapshot.getSensorsState().get(event.getId()) != null) {
            SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) || oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro sensorStateAvro = new SensorStateAvro();
        sensorStateAvro.setTimestamp(event.getTimestamp());
        sensorStateAvro.setData(event.getPayload());
        snapshot.getSensorsState().put(event.getId(), sensorStateAvro);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }
}
