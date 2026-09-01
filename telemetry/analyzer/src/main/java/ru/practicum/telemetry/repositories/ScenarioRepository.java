package ru.practicum.telemetry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.telemetry.entities.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByHubIdAndName(String hubId, String name);

    @Query("""
        SELECT COUNT(c) > 0
        FROM ScenarioCondition c
        JOIN c.scenario sc
        WHERE c.sensor.id = :sensorId AND sc.hubId = :hubId
    """)
    boolean isSensorAssociatedWithConditions(@Param("sensorId") String sensorId, @Param("hubId") String hubId);

    @Query("""
        SELECT COUNT(a) > 0
        FROM ScenarioAction a
        JOIN a.scenario sc
        WHERE a.sensor.id = :sensorId AND sc.hubId = :hubId
    """)
    boolean isSensorAssociatedWithActions(@Param("sensorId") String sensorId, @Param("hubId") String hubId);
}
