package com.example.gyeongjoLog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Long id;
    private String name;
    private String phoneNumber;
    private String eventType;
    private String date;
    private String relationship;
    private int amount;
    private String memo;
}
