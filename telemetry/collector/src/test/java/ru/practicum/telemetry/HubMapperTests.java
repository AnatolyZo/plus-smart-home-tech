package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.mapper.HubMapper;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import static org.assertj.core.api.Assertions.assertThat;

public class HubMapperTests {

    @Test
    void deviceAddedMappingTest() {
        HubEvent event = TestHubEntities.createDeviceAddedEvent(TestSensorEntities.HUB_ID);
        SpecificRecordBase deviceAddedAvro = TestHubEntities.createDeviceAddedEventAvro();
        SpecificRecordBase expectedEvent = TestHubEntities.createTestHubEventAvro(deviceAddedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void deviceRemovedMappingTest() {
        HubEvent event = TestHubEntities.createDeviceRemovedEvent(TestHubEntities.HUB_ID);
        SpecificRecordBase deviceRemovedAvro = TestHubEntities.createDeviceRemovedEventAvro();
        SpecificRecordBase expectedEvent = TestHubEntities.createTestHubEventAvro(deviceRemovedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioAddedMappingTest() {
        HubEvent event = TestHubEntities.createScenarioAddedEvent(TestHubEntities.HUB_ID);
        SpecificRecordBase scenarioAddedAvro = TestHubEntities.createScenarioAddedEventAvro();
        SpecificRecordBase expectedEvent = TestHubEntities.createTestHubEventAvro(scenarioAddedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioRemovedMappingTest() {
        HubEvent event = TestHubEntities.createScenarioRemovedEvent(TestHubEntities.HUB_ID);
        SpecificRecordBase scenarioRemovedAvro = TestHubEntities.createScenarioRemovedEventAvro();
        SpecificRecordBase expectedEvent = TestHubEntities.createTestHubEventAvro(scenarioRemovedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void deviceAddedMappingToHubEventTest() {
        HubEventProto event = TestHubEntities.createDeviceAddedEventProto();
        HubEvent expectedEvent = TestHubEntities.createDeviceAddedEvent(TestHubEntities.HUB_ID);

        HubEvent result = HubMapper.mapToDeviceAddedEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void deviceRemovedMappingToHubEventTest() {
        HubEventProto event = TestHubEntities.createDeviceRemovedEventProto();
        HubEvent expectedEvent = TestHubEntities.createDeviceRemovedEvent(TestHubEntities.HUB_ID);

        HubEvent result = HubMapper.mapToDeviceRemovedEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioAddedMappingToHubEventTest() {
        HubEventProto event = TestHubEntities.createScenarioAddedEventProto();
        HubEvent expectedEvent = TestHubEntities.createScenarioAddedEvent(TestHubEntities.HUB_ID);

        HubEvent result = HubMapper.mapToScenarioAddedEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioRemovedMappingToHubEventTest() {
        HubEventProto event = TestHubEntities.createScenarioRemovedEventProto();
        HubEvent expectedEvent = TestHubEntities.createScenarioRemovedEvent(TestHubEntities.HUB_ID);

        HubEvent result = HubMapper.mapToScenarioRemovedEvent(event);

        assertThat(result).isEqualTo(expectedEvent);
    }
}
