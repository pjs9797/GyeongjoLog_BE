package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventDTO;
import com.example.gyeongjoLog.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/myEvents")
    public ResponseEntity<APIResponse> getMyEvents(Authentication authentication) {
        return new ResponseEntity<>(eventService.getMyEvents(authentication), HttpStatus.OK);
    }

    @GetMapping("/myEvents/summary")
    public ResponseEntity<APIResponse> getMyEventSummaries(Authentication authentication,
                                           @RequestParam("eventType") String eventType,
                                           @RequestParam("date") String date) {
        return new ResponseEntity<>(eventService.getMyEventSummaries(authentication, eventType, date), HttpStatus.OK);
    }

    @GetMapping("/othersEvents/summary")
    public ResponseEntity<APIResponse> getOtherEventSummaries(Authentication authentication) {
        return new ResponseEntity<>(eventService.getOtherEventSummaries(authentication), HttpStatus.OK);
    }

    @GetMapping("/singleEvent")
    public ResponseEntity<APIResponse> getEventById(@RequestParam("eventId") Long eventId) {
        return new ResponseEntity<>(eventService.getEventById(eventId), HttpStatus.OK);
    }

    @GetMapping("/yearMonth")
    public ResponseEntity<APIResponse> getEventsForUserByMonth(Authentication authentication, @RequestParam("date") String date) {
        return new ResponseEntity<>(eventService.getEventsByYearMonth(authentication, date), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse> createEvent(Authentication authentication, @RequestBody EventDTO eventDto) {

        return new ResponseEntity<>(eventService.saveEvent(authentication, eventDto), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<APIResponse> deleteEvent(@RequestParam("eventId") Long eventId) {
        return new ResponseEntity<>(eventService.deleteEventById(eventId), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<APIResponse> updateEvent(@RequestParam("eventId") Long eventId,@RequestBody EventDTO eventDto) {
        return new ResponseEntity<>(eventService.updateEvent(eventId, eventDto), HttpStatus.OK);
    }
}
