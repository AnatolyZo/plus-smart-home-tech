package ru.practicum.telemetry;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.mapper.HubMapper;
import ru.practicum.telemetry.mapper.SensorMapper;
import ru.practicum.telemetry.sensors.SensorEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsServiceImpl implements EventsService {
    private final Producer<String, SpecificRecordBase> producer;

    @Override
    public void processSensorEvent(SensorEvent sensorEvent) {
        log.trace("Иницирована отправка показаний датчика - {}", sensorEvent);
        SpecificRecordBase sensor = SensorMapper.toAvro(sensorEvent);
        log.debug("Данные датчика преобразованы в тип SpecificRecordBase - {}", sensor);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(TelemetryTopics.TELEMETRY_SENSORS_TOPIC, sensorEvent.getId(), sensor);
        producer.send(record);
        log.debug("Создана и отправлена запись данных датчика в Kafka - {}", record);
    }

    @Override
    public void processHubEvent(HubEvent hubEvent) {
        log.trace("Иницирована отправка данных хаба/сценария - {}", hubEvent);
        SpecificRecordBase hub = HubMapper.toAvro(hubEvent);
        log.debug("Данные хаба/сценария преобразованы в тип SpecificRecordBase - {}", hub);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(TelemetryTopics.TELEMETRY_HUBS_TOPIC, hubEvent.getHubId(), hub);
        producer.send(record);
        log.debug("Создана и отправлена запись данных хаба/сценария в Kafka - {}", record);
    }

    @PreDestroy
    public void destroy() {
        producer.close();
        log.debug("Продюсер закрыт");
    }
}
