package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventTypeDTO;
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

    @GetMapping("/list")
    public ResponseEntity<APIResponse> getEventTypes(Authentication authentication) {
        return new ResponseEntity<>(eventTypeService.getEventTypes(authentication), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse> addEventType(Authentication authentication, @RequestBody EventTypeDTO eventType) {
        return new ResponseEntity<>(eventTypeService.addEventType(authentication, eventType), HttpStatus.OK);
    }
}
