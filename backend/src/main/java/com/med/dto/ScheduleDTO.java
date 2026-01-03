package com.med.dto;

import com.med.entity.MedicationSchedule.ScheduleType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleDTO {
    private Long id;
    private String name;
    private LocalTime scheduleTime;
    private ScheduleType scheduleType;
    private Integer intervalDays;
    private String weekDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    // 包含的药品列表
    private List<ScheduleMedicineDTO> medicines;

    @Data
    public static class ScheduleMedicineDTO {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private String specification;
        private BigDecimal dosage;
    }
}
