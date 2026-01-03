package com.med.config;

import com.med.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements CommandLineRunner {

    private final RecordService recordService;

    @Override
    public void run(String... args) {
        try {
            LocalDate today = LocalDate.now();
            log.info("应用启动，开始生成今日服药记录: {}", today);
            recordService.generateDailyRecords(today);
            log.info("今日服药记录生成完成: {}", today);
        } catch (Exception e) {
            log.error("应用启动时生成今日服药记录失败", e);
        }
    }
}

