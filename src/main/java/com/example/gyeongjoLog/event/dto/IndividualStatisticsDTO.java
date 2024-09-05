package com.example.gyeongjoLog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualStatisticsDTO {
    private String name;
    private String phoneNumber;
    private String relationship;
    private int totalInteractions;
    private int totalAmount;
    private int totalReceivedAmount;
    private int totalSentAmount;
    private List<EventDetailDTO> eventDetails;
}
