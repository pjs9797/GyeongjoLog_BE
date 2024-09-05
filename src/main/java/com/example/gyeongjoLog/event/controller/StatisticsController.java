package com.example.gyeongjoLog.event.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.dto.IndividualStatisticsDTO;
import com.example.gyeongjoLog.event.dto.MonthlyStatisticsDTO;
import com.example.gyeongjoLog.event.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/individual")
    public ResponseEntity<APIResponse> getIndividualStatistics(Authentication authentication) {
        List<IndividualStatisticsDTO> statistics = statisticsService.fetchIndividualStatistics(authentication);
        return new ResponseEntity<>(APIResponse.builder().resultCode("200").resultMessage("개인별 통계 조회 성공").data(statistics).build(), HttpStatus.OK);
    }

    @GetMapping("/monthly")
    public ResponseEntity<APIResponse> getMonthlyStatistics(Authentication authentication) {
        List<MonthlyStatisticsDTO> statistics = statisticsService.fetchMonthlyStatistics(authentication);
        return new ResponseEntity<>(APIResponse.builder().resultCode("200").resultMessage("월별 통계 조회 성공").data(statistics).build(), HttpStatus.OK);
    }
}
