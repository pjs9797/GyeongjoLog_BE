package com.example.gyeongjoLog.event.service;

import com.example.gyeongjoLog.event.dto.EventDetailDTO;
import com.example.gyeongjoLog.event.dto.IndividualStatisticsDTO;
import com.example.gyeongjoLog.event.dto.MonthlyStatisticsDTO;
import com.example.gyeongjoLog.event.entity.EventEntity;
import com.example.gyeongjoLog.event.repository.EventRepository;
import com.example.gyeongjoLog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<IndividualStatisticsDTO> fetchIndividualStatistics(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        List<EventEntity> events = eventRepository.findAllEvents(userId);

        // 이벤트를 이름과 전화번호로 그룹화
        Map<PersonKey, List<EventEntity>> groupedEvents = events.stream()
                .collect(Collectors.groupingBy(event -> new PersonKey(event.getName(), event.getPhoneNumber())));

        // 통계 계산
        return groupedEvents.values().stream()
                .map(this::calculateStatistics)
                .collect(Collectors.toList());
    }

    public List<MonthlyStatisticsDTO> fetchMonthlyStatistics(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email).getId();

        List<EventEntity> events = eventRepository.findAllEvents(userId);

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM");

        // 마지막 6개월의 월을 생성
        List<String> lastSixMonths = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            lastSixMonths.add(currentDate.minusMonths(i).format(formatter));
        }
        Collections.reverse(lastSixMonths);

        // 기본값으로 MonthlyStatisticsDTO 객체 생성
        Map<String, MonthlyStatisticsDTO> monthlyStatisticsMap = lastSixMonths.stream()
                .collect(Collectors.toMap(month -> month, month -> MonthlyStatisticsDTO.builder()
                        .month(month)
                        .sentAmount(0)
                        .receivedAmount(0)
                        .transactionCount(0)
                        .eventTypeAmounts(new HashMap<>())
                        .build()));

        // 이벤트를 날짜별로 그룹화
        Map<String, List<EventEntity>> groupedByMonth = events.stream()
                .collect(Collectors.groupingBy(event -> LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy.M.d")).format(formatter)));

        // 데이터를 기본값에 병합
        for (Map.Entry<String, List<EventEntity>> entry : groupedByMonth.entrySet()) {
            String month = entry.getKey();
            List<EventEntity> monthEvents = entry.getValue();

            MonthlyStatisticsDTO statistics = calculateMonthlyStatistics(monthEvents, month);
            monthlyStatisticsMap.put(month, statistics);
        }

        return lastSixMonths.stream()
                .map(monthlyStatisticsMap::get)
                .collect(Collectors.toList());
    }

    private IndividualStatisticsDTO calculateStatistics(List<EventEntity> events) {
        int totalInteractions = events.size();
        int totalAmount = events.stream().mapToInt(EventEntity::getAmount).sum();
        int totalReceivedAmount = events.stream().filter(event -> event.getAmount() > 0).mapToInt(EventEntity::getAmount).sum();
        int totalSentAmount = events.stream().filter(event -> event.getAmount() < 0).mapToInt(EventEntity::getAmount).sum();

        List<EventDetailDTO> eventDetails = events.stream()
                .map(event -> EventDetailDTO.builder()
                        .name(event.getName())
                        .date(event.getDate())
                        .eventType(event.getEventType())
                        .amount(event.getAmount())
                        .build())
                .collect(Collectors.toList());

        EventEntity firstEvent = events.get(0);
        return IndividualStatisticsDTO.builder()
                .name(firstEvent.getName())
                .phoneNumber(firstEvent.getPhoneNumber())
                .relationship(firstEvent.getRelationship())
                .totalInteractions(totalInteractions)
                .totalAmount(totalAmount)
                .totalReceivedAmount(totalReceivedAmount)
                .totalSentAmount(totalSentAmount)
                .eventDetails(eventDetails)
                .build();
    }

    private MonthlyStatisticsDTO calculateMonthlyStatistics(List<EventEntity> events, String month) {
        int sentAmount = 0;
        int receivedAmount = 0;
        int transactionCount = events.size();
        Map<String, Integer> eventTypeAmounts = new HashMap<>();

        for (EventEntity event : events) {
            if (event.getAmount() < 0) {
                sentAmount += event.getAmount();
                eventTypeAmounts.put(event.getEventType(), eventTypeAmounts.getOrDefault(event.getEventType(), 0) + event.getAmount());
            } else if (event.getAmount() > 0) {
                receivedAmount += event.getAmount();
            }
        }

        return MonthlyStatisticsDTO.builder()
                .month(month)
                .sentAmount(sentAmount)
                .receivedAmount(receivedAmount)
                .transactionCount(transactionCount)
                .eventTypeAmounts(eventTypeAmounts)
                .build();
    }

    // 추가: PersonKey 클래스를 정의하여 이름과 전화번호로 그룹화할 수 있도록 합니다.
    private static class PersonKey {
        private String name;
        private String phoneNumber;

        public PersonKey(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonKey personKey = (PersonKey) o;
            return Objects.equals(name, personKey.name) &&
                    Objects.equals(phoneNumber, personKey.phoneNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, phoneNumber);
        }
    }
}
