//package ru.practicum.telemetry;
//
//import jakarta.annotation.PreDestroy;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.avro.specific.SpecificRecordBase;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.springframework.stereotype.Service;
//import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaSenderServiceImpl implements KafkaSenderService {
//    private final Producer<String, SpecificRecordBase> producer;
//
//    @Override
//    public void sendSnapshot(SensorsSnapshotAvro snapshotAvro) {
//        log.trace("Иницирована отправка показаний датчика - {}", snapshotAvro);
//        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(TelemetryTopics.TELEMETRY_SNAPSHOT_TOPIC, snapshotAvro.getHubId(), snapshotAvro);
//        producer.send(record);
//        log.debug("Создана и отправлена запись данных датчика в Kafka - {}", record);
//    }
//
//    @PreDestroy
//    public void destroy() {
//        producer.close();
//        log.debug("Продюсер закрыт");
//    }
//}
