package com.example.gyeongjoLog.event.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventDTO;
import com.example.gyeongjoLog.event.dto.MyEventDTO;
import com.example.gyeongjoLog.event.entity.EventEntity;
import com.example.gyeongjoLog.event.repository.EventRepository;
import com.example.gyeongjoLog.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    public APIResponse getMyEvents(Long userId) {
        // 사용자의 이벤트를 조회
        List<EventEntity> events = eventRepository.findMyEvents(userId);

        // 이벤트를 날짜와 이벤트 타입으로 그룹화하고, 해당 그룹의 이벤트 건수를 계산
        Map<String, Map<String, Long>> groupedEvents = events.stream()
                .collect(Collectors.groupingBy(EventEntity::getDate,
                        Collectors.groupingBy(EventEntity::getEventType,
                                Collectors.counting())));

        // MyEventDTO 리스트로 변환
        List<MyEventDTO> myEventDTOs = groupedEvents.entrySet().stream()
                .flatMap(dateEntry -> dateEntry.getValue().entrySet().stream()
                        .map(eventTypeEntry -> MyEventDTO.builder()
                                .date(dateEntry.getKey())
                                .eventType(eventTypeEntry.getKey())
                                .eventCnt(eventTypeEntry.getValue().intValue())
                                .build()))
                .sorted(Comparator.comparing(MyEventDTO::getDate).reversed())
                .collect(Collectors.toList());

        return APIResponse.createWithData("200", "나의 경조사 목록 조회 성공", myEventDTOs);
    }

    public APIResponse getMyEventSummaries(Long userId, String type, String date) {
        List<EventEntity> events = eventRepository.findMyEventSummaries(userId, type, date);
        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return APIResponse.createWithData("200", "나의 경조사 요약 목록 조회 성공", eventDTOs);
    }

    public APIResponse getOtherEventSummaries(Long userId) {
        List<EventEntity> events = eventRepository.findOthersEventSummaries(userId);
        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return APIResponse.createWithData("200", "타인 경조사 요약 목록 조회 성공", eventDTOs);
    }

    public APIResponse getEventById(Long eventId) {
        Optional<EventEntity> eventEntityOptional = eventRepository.findById(eventId);

        if (eventEntityOptional.isPresent()) {
            EventEntity eventEntity = eventEntityOptional.get();
            EventDTO eventDTO = convertToDTO(eventEntity);
            return APIResponse.createWithData("200", "경조사 조회 성공", eventDTO);
        }
        else {
            return APIResponse.createWithoutData("204", "경조사를 찾을 수 없습니다.");
        }
    }

    public APIResponse getEventsByYearMonth(Long userId, String date) {

        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy.M.d"));

        String yearMonth = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<EventEntity> events = eventRepository.findEventsByYearMonth(userId, yearMonth);

        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return APIResponse.createWithData("200", "해당 월의 이벤트 조회 성공", eventDTOs);
    }

    private EventDTO convertToDTO(EventEntity eventEntity) {
        return EventDTO.builder()
                .id(eventEntity.getId())
                .name(eventEntity.getName())
                .phoneNumber(eventEntity.getPhoneNumber())
                .eventType(eventEntity.getEventType())
                .date(eventEntity.getDate())
                .relationship(eventEntity.getRelationship())
                .amount(eventEntity.getAmount())
                .memo(eventEntity.getMemo())
                .build();
    }

    public APIResponse saveEvent(Long userId, EventDTO eventDto) {
        EventEntity event = EventEntity.builder()
                .name(eventDto.getName())
                .phoneNumber(eventDto.getPhoneNumber())
                .eventType(eventDto.getEventType())
                .date(eventDto.getDate())
                .relationship(eventDto.getRelationship())
                .amount(eventDto.getAmount())
                .memo(eventDto.getMemo())
                .user(UserEntity.builder().id(userId).build())
                .build();
        eventRepository.save(event);
        return APIResponse.createWithoutData("200", "이벤트 추가 성공");
    }

    public APIResponse deleteEventById(Long eventId) {
        eventRepository.deleteById(eventId);
        return APIResponse.createWithoutData("200", "이벤트 삭제 성공");
    }

    public APIResponse updateEvent(EventDTO eventDto) {
        EventEntity event = eventRepository.findById(eventDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        event.setName(eventDto.getName());
        event.setPhoneNumber(eventDto.getPhoneNumber());
        event.setEventType(eventDto.getEventType());
        event.setDate(eventDto.getDate());
        event.setRelationship(eventDto.getRelationship());
        event.setAmount(eventDto.getAmount());
        event.setMemo(eventDto.getMemo());
        eventRepository.save(event);
        return APIResponse.createWithoutData("200", "이벤트 수정 성공");
    }
}
