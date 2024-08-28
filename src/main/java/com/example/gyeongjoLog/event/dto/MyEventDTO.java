package com.example.gyeongjoLog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyEventDTO {
    private String eventType;
    private String date;
    private int eventCnt;
}
