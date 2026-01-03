package com.med.controller;

import com.med.common.Result;
import com.med.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/completion-rate")
    public Result<Map<String, Object>> getCompletionRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 默认本周
        if (startDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.with(DayOfWeek.MONDAY);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        return Result.success(statisticsService.getCompletionRate(startDate, endDate));
    }

    @GetMapping("/check-trends")
    public Result<List<Map<String, Object>>> getCheckTrends(
            @RequestParam Long checkItemId,
            @RequestParam(defaultValue = "3") int months) {
        return Result.success(statisticsService.getCheckTrends(checkItemId, months));
    }

    @GetMapping("/latest-checks")
    public Result<List<Map<String, Object>>> getLatestChecks() {
        return Result.success(statisticsService.getLatestChecks());
    }
}
