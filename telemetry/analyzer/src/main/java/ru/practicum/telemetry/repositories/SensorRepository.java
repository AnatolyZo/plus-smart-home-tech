package ru.practicum.telemetry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.telemetry.entities.Sensor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<Sensor> findByIdAndHubId(String id, String hubId);

    void deleteByIdAndHubId(String id, String hubId);

    @Query("SELECT s FROM Sensor s WHERE s.id IN :ids AND s.hubId = :hubId")
    List<Sensor> findBySensorIdsAndHub(@Param("ids") List<String> ids, @Param("hubId") String hubId);
}
