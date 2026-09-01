package ru.practicum.telemetry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.telemetry.entities.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
