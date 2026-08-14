package ru.practicum.telemetry;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.telemetry.hubs.HubEvent;
import ru.practicum.telemetry.sensors.SensorEvent;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsController {
    private final EventsService eventsService;

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.OK)
    public void processSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        eventsService.processSensorEvent(sensorEvent);
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.OK)
    public void processHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        eventsService.processHubEvent(hubEvent);
    }
}
