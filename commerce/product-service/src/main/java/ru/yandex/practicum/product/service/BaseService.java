package ru.yandex.practicum.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.product.exception.NotFoundException;

@Slf4j
public class BaseService {
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, String className, long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Не найден объект типа {} с id {}", className, id);
                    return new NotFoundException(String.format("Не найден объект типа %s с id %d", className, id));
                });
    }
}
