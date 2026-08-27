package ru.practicum.telemetry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.telemetry.entities.Sensor;

import java.util.Collection;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<Sensor> findByIdAndHubId(String id, String hubId);
}
