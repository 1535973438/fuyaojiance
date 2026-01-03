package com.med.config;

import com.med.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTask {

    private final RecordService recordService;

    /**
     * 每天凌晨1点生成今日服药记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateTodayRecords() {
        try {
            LocalDate today = LocalDate.now();
            log.info("开始生成今日服药记录: {}", today);
            recordService.generateDailyRecords(today);
            log.info("今日服药记录生成完成: {}", today);
        } catch (Exception e) {
            log.error("生成今日服药记录失败", e);
        }
    }
}

