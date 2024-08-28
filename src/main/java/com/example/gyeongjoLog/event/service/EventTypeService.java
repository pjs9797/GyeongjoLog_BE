package com.example.gyeongjoLog.event.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventTypeDTO;
import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import com.example.gyeongjoLog.event.repository.EventTypeRepository;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventTypeService {

    EventTypeRepository eventTypeRepository;
    UserRepository userRepository;

    @Autowired
    public EventTypeService(EventTypeRepository eventTypeRepository, UserRepository userRepository){
        this.eventTypeRepository = eventTypeRepository;
        this.userRepository = userRepository;
    }

    public APIResponse getEventTypes(Long userId) {
        List<EventTypeEntity> eventTypes = eventTypeRepository.findByUserId(userId);

        List<EventTypeDTO> eventTypeDTOs = eventTypes.stream()
                .map(eventType -> new EventTypeDTO(eventType.getEventType(), eventType.getColor()))
                .collect(Collectors.toList());

        return APIResponse.createWithData("200", "이벤트 타입 목록 조회 성공", eventTypeDTOs);
    }

    public APIResponse addEventType(Long userId, String eventType, String color) {
        EventTypeEntity newEventType = EventTypeEntity.builder()
                .eventType(eventType)
                .color(color)
                .user(UserEntity.builder().id(userId).build())
                .build();

        eventTypeRepository.save(newEventType);
        return APIResponse.createWithoutData("200", "이벤트 타입 추가 성공");
    }
}
