package ru.practicum.telemetry;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Test;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.mapper.HubMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class HubMapperTests {

    @Test
    void deviceAddedMappingTest() {
        HubEvent event = TestEntities.createDeviceAddedEvent(TestEntities.HUB_ID);
        SpecificRecordBase deviceAddedAvro = TestEntities.createDeviceAddedEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestHubEventAvro(deviceAddedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void deviceRemovedMappingTest() {
        HubEvent event = TestEntities.createDeviceRemovedEvent(TestEntities.HUB_ID);
        SpecificRecordBase deviceRemovedAvro = TestEntities.createDeviceRemovedEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestHubEventAvro(deviceRemovedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioAddedMappingTest() {
        HubEvent event = TestEntities.createScenarioAddedEvent(TestEntities.HUB_ID);
        SpecificRecordBase scenarioAddedAvro = TestEntities.createScenarioAddedEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestHubEventAvro(scenarioAddedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }

    @Test
    void scenarioRemovedMappingTest() {
        HubEvent event = TestEntities.createScenarioRemovedEvent(TestEntities.HUB_ID);
        SpecificRecordBase scenarioRemovedAvro = TestEntities.createScenarioRemovedEventAvro();
        SpecificRecordBase expectedEvent = TestEntities.createTestHubEventAvro(scenarioRemovedAvro);

        SpecificRecordBase result = HubMapper.toAvro(event);

        assertThat(result).isEqualTo(expectedEvent);
    }
}
