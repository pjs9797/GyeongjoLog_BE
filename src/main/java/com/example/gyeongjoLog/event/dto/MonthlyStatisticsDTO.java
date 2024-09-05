package com.example.gyeongjoLog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsDTO {
    private String month;
    private int sentAmount;
    private int receivedAmount;
    private int transactionCount;
    private Map<String, Integer> eventTypeAmounts;
}
