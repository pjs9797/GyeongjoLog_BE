package com.example.gyeongjoLog.event.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventTypeDTO;
import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import com.example.gyeongjoLog.event.repository.EventTypeRepository;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;

    public APIResponse getEventTypes(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        List<EventTypeEntity> eventTypes = eventTypeRepository.findByUserId(userId);

        List<EventTypeDTO> eventTypeDTOs = eventTypes.stream()
                .map(eventType -> new EventTypeDTO(eventType.getEventType(), eventType.getColor()))
                .collect(Collectors.toList());

        return APIResponse.builder().resultCode("200").resultMessage("이벤트 타입 목록 조회 성공").data(eventTypeDTOs).build();
    }

    public APIResponse addEventType(Authentication authentication, EventTypeDTO eventTypeDTO) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        EventTypeEntity newEventType = EventTypeEntity.builder()
                .eventType(eventTypeDTO.getEventType())
                .color(eventTypeDTO.getColor())
                .user(UserEntity.builder().id(userId).build())
                .build();

        eventTypeRepository.save(newEventType);
        return APIResponse.builder().resultCode("200").resultMessage("이벤트 타입 추가 성공").build();
    }
}
