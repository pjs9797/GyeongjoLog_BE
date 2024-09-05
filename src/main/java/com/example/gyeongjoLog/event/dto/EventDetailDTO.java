package com.example.gyeongjoLog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDTO {
    private String name;
    private String date;
    private String eventType;
    private int amount;
}
