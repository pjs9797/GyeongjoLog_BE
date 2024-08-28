package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.service.EventTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evenTypes")
@RequiredArgsConstructor
public class EventTypeController {

    EventTypeService eventTypeService;
    @Autowired
    public EventTypeController(EventTypeService eventTypeService){
        this.eventTypeService = eventTypeService;
    }

    @GetMapping()
    public APIResponse getEventTypes(@RequestParam Long userId) {
        return eventTypeService.getEventTypes(userId);
    }

    @PostMapping("/add")
    public APIResponse addEventType(@RequestParam Long userId,
                                    @RequestParam String eventType,
                                    @RequestParam String color) {
        return eventTypeService.addEventType(userId, eventType, color);
    }
}
