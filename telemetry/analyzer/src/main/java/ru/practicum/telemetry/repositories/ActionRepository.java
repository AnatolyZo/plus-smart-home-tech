package ru.practicum.telemetry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.telemetry.entities.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
