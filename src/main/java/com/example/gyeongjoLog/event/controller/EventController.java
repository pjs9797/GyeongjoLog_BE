package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventDTO;
import com.example.gyeongjoLog.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    EventService eventService;

    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping("/myEvent")
    public APIResponse getMyEvents(@RequestParam Long userId) {
        return eventService.getMyEvents(userId);
    }

    @GetMapping("/myEvent/summary")
    public APIResponse getMyEventSummaries(@RequestParam Long userId,
                                           @RequestParam String eventType,
                                           @RequestParam String date) {
        return eventService.getMyEventSummaries(userId, eventType, date);
    }

    @GetMapping("/othersEvent/summary")
    public APIResponse getOtherEventSummaries(@RequestParam Long userId) {
        return eventService.getOtherEventSummaries(userId);
    }

    @GetMapping()
    public APIResponse getEventById(@RequestParam Long eventId) {
        return eventService.getEventById(eventId);
    }

    @GetMapping("/yearMonth")
    public APIResponse getEventsForUserByMonth(@RequestParam Long userId, @RequestParam String date) {
        return eventService.getEventsByYearMonth(userId, date);
    }

    @PostMapping("/add")
    public APIResponse createEvent(@RequestParam Long userId, @RequestBody EventDTO eventDto) {
        return eventService.saveEvent(userId, eventDto);
    }

    @DeleteMapping("/delete")
    public APIResponse deleteEvent(@RequestParam Long eventId) {
        return eventService.deleteEventById(eventId);
    }

    @PutMapping("/update")
    public APIResponse updateEvent(@RequestBody EventDTO eventDto) {
        return eventService.updateEvent(eventDto);
    }
}
