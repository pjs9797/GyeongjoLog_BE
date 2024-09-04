package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.service.EventTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evenTypes")
@RequiredArgsConstructor
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @GetMapping()
    public ResponseEntity<APIResponse> getEventTypes(Authentication authentication) {
        return new ResponseEntity<>(eventTypeService.getEventTypes(authentication), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse> addEventType(Authentication authentication,
                                    @RequestParam String eventType,
                                    @RequestParam String color) {
        return new ResponseEntity<>(eventTypeService.addEventType(authentication, eventType, color), HttpStatus.OK);
    }
}
