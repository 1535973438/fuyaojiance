package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@TableName("medication_schedule")
public class MedicationSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long medicineId;
    private String name;

    private LocalTime scheduleTime;

    private ScheduleType scheduleType;

    private Integer intervalDays;

    private String weekDays;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;
    private Map<String,Double> dosageConfig;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum ScheduleType {
        DAILY,      // 每日
        INTERVAL,   // 每隔N日
        WEEKLY ,     // 每周指定几天
        COURSE
    }
}
