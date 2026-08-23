//package ru.practicum.telemetry;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.practicum.telemetry.hubs.HubEvent;
//import ru.practicum.telemetry.sensors.SensorEvent;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.mockito.Mockito.verify;
//
//@WebMvcTest(EventsController.class)
//public class EventsControllerTests {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @MockBean
//    private EventsService eventsService;
//
//    @Test
//    void processSensorEventTestWithCorrectSensorEvent() throws Exception {
//        SensorEvent correctSensorEvent = TestEntities.createLightSensorEvent(TestEntities.SENSOR_ID, TestEntities.HUB_ID);
//
//        String content = mapper.writeValueAsString(correctSensorEvent);
//        mvc.perform(post("/events/sensors")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().isOk());
//
//        verify(eventsService).processSensorEvent(any());
//    }
//
//    @Test
//    void processSensorEventTestWithSensorEventWithoutId() throws Exception {
//        SensorEvent sensorEventWithoutId = TestEntities.createLightSensorEvent(null, TestEntities.HUB_ID);
//
//        String content = mapper.writeValueAsString(sensorEventWithoutId);
//        mvc.perform(post("/events/sensors")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().isBadRequest());
//
//        verify(eventsService, never()).processSensorEvent(any());
//    }
//
//    @Test
//    void processSensorEventTestWithSensorEventWithoutHubId() throws Exception {
//        SensorEvent sensorEventWithoutHubId = TestEntities.createLightSensorEvent(TestEntities.SENSOR_ID, null);
//
//        String content = mapper.writeValueAsString(sensorEventWithoutHubId);
//        mvc.perform(post("/events/sensors")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().isBadRequest());
//
//        verify(eventsService, never()).processSensorEvent(any());
//    }
//
//    @Test
//    void processSensorEventTestWithSensorEventWithoutType() throws Exception {
//        mvc.perform(post("/events/sensors")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestEntities.JSON_SENSOR_EVENT_WITHOUT_TYPE))
//                .andExpect(status().isBadRequest());
//
//        verify(eventsService, never()).processSensorEvent(any());
//    }
//
//    @Test
//    void processHubEventTestWithCorrectHubEvent() throws Exception {
//        HubEvent correctHubEvent = TestEntities.createDeviceAddedEvent(TestEntities.HUB_ID);
//
//        String content = mapper.writeValueAsString(correctHubEvent);
//        mvc.perform(post("/events/hubs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().isOk());
//
//        verify(eventsService).processHubEvent(any());
//    }
//
//    @Test
//    void processHubEventTestWithHubEventWithoutHubId() throws Exception {
//        HubEvent correctHubEvent = TestEntities.createDeviceAddedEvent(null);
//
//        String content = mapper.writeValueAsString(correctHubEvent);
//        mvc.perform(post("/events/hubs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().isBadRequest());
//
//        verify(eventsService, never()).processHubEvent(any());
//    }
//
//    @Test
//    void processHubEventTestWithHubEventWithoutType() throws Exception {
//        mvc.perform(post("/events/hubs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestEntities.JSON_HUB_EVENT_WITHOUT_TYPE))
//                .andExpect(status().isBadRequest());
//
//        verify(eventsService, never()).processHubEvent(any());
//    }
//}
