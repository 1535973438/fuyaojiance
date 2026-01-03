package com.med.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getCompletionRate(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getCheckTrends(Long checkItemId, int months);
    List<Map<String, Object>> getLatestChecks();
}
