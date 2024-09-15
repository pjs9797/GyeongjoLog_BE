package com.example.gyeongjoLog.event.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.EventDTO;
import com.example.gyeongjoLog.event.entity.EventEntity;
import com.example.gyeongjoLog.event.repository.EventRepository;
import com.example.gyeongjoLog.user.dto.CustomUserDetails;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public APIResponse getMyEvents(Authentication authentication) {
        String email = authentication.getName();

        Long userId = userRepository.findByEmail(email).getId();

        // 사용자의 이벤트를 조회
        List<EventEntity> events = eventRepository.findMyEvents(userId);

        // EventEntity 리스트를 EventDTO 리스트로 변환
        List<EventDTO> eventDTOs = events.stream()
                .map(event -> EventDTO.builder()
                        .id(event.getId())
                        .name(event.getName())
                        .phoneNumber(event.getPhoneNumber())
                        .eventType(event.getEventType())
                        .date(event.getDate().toString())  // 필요한 포맷으로 변환
                        .relationship(event.getRelationship())
                        .amount(event.getAmount())
                        .memo(event.getMemo())
                        .build())
                .sorted(Comparator.comparing(EventDTO::getDate).reversed()) // 날짜 기준 내림차순 정렬
                .collect(Collectors.toList());

        // APIResponse에 담아 반환
        return APIResponse.builder().resultCode("200").resultMessage("나의 경조사 목록 조회 성공").data(eventDTOs).build();
    }

    public APIResponse getMyEventSummaries(Authentication authentication, String type, String date) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        List<EventEntity> events = eventRepository.findMyEventSummaries(userId, type, date);
        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return APIResponse.builder().resultCode("200").resultMessage("나의 경조사 요약 목록 조회 성공").data(eventDTOs).build();
    }

    public APIResponse getOtherEventSummaries(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        List<EventEntity> events = eventRepository.findOthersEventSummaries(userId);
        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return APIResponse.builder().resultCode("200").resultMessage("타인 경조사 요약 목록 조회 성공").data(eventDTOs).build();
    }

    public APIResponse getEventById(Long eventId) {
        Optional<EventEntity> eventEntityOptional = eventRepository.findById(eventId);

        if (eventEntityOptional.isPresent()) {
            EventEntity eventEntity = eventEntityOptional.get();
            EventDTO eventDTO = convertToDTO(eventEntity);
            return APIResponse.builder().resultCode("200").resultMessage("경조사 조회 성공").data(eventDTO).build();
        }
        else {
            return APIResponse.builder().resultCode("204").resultMessage("경조사 조회 실패").data(null).build();
        }
    }

    public APIResponse getEventsByYearMonth(Authentication authentication, String date) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        // 'yyyy.MM' 형식으로 입력된 날짜를 YearMonth로 파싱
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy.MM"));

        // 년-월을 'yyyy-MM' 형식으로 변환
        String formattedYearMonth = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<EventEntity> events = eventRepository.findEventsByYearMonth(userId, formattedYearMonth);

        List<EventDTO> eventDTOs = events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return APIResponse.builder().resultCode("200").resultMessage("해당 월의 이벤트 조회 성공").data(eventDTOs).build();
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

    public APIResponse saveEvent(Authentication authentication, EventDTO eventDto) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

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
        return APIResponse.builder().resultCode("200").resultMessage("이벤트 추가 성공").build();
    }

    public APIResponse deleteEventById(Long eventId) {
        eventRepository.deleteById(eventId);
        return APIResponse.builder().resultCode("200").resultMessage("이벤트 삭제 성공").build();
    }

    public APIResponse updateEvent(Long eventId, EventDTO eventDto) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
        event.setName(eventDto.getName());
        event.setPhoneNumber(eventDto.getPhoneNumber());
        event.setEventType(eventDto.getEventType());
        event.setDate(eventDto.getDate());
        event.setRelationship(eventDto.getRelationship());
        event.setAmount(eventDto.getAmount());
        event.setMemo(eventDto.getMemo());
        eventRepository.save(event);
        return APIResponse.builder().resultCode("200").resultMessage("이벤트 수정 성공").build();
    }
}
