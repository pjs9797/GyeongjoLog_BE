package com.example.gyeongjoLog.event.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTypeDTO {
    private String eventType;
    private String color;
}
