package com.med.service.impl;

import com.med.mapper.CheckRecordMapper;
import com.med.mapper.MedicationRecordMapper;
import com.med.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MedicationRecordMapper recordMapper;
    private final CheckRecordMapper checkRecordMapper;

    @Override
    public Map<String, Object> getCompletionRate(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Map<String, Object> stats = recordMapper.getCompletionStats(start, end);

        Map<String, Object> result = new HashMap<>();

        long total = stats.get("total") != null ? ((Number) stats.get("total")).longValue() : 0;
        long taken = stats.get("taken") != null ? ((Number) stats.get("taken")).longValue() : 0;
        long missed = stats.get("missed") != null ? ((Number) stats.get("missed")).longValue() : 0;

        result.put("total", total);
        result.put("taken", taken);
        result.put("missed", missed);

        if (total > 0) {
            BigDecimal rate = BigDecimal.valueOf(taken)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
            result.put("rate", rate);
        } else {
            result.put("rate", BigDecimal.ZERO);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getCheckTrends(Long checkItemId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        return checkRecordMapper.getTrendData(checkItemId, startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getLatestChecks() {
        return checkRecordMapper.getLatestPresetChecks();
    }
}
