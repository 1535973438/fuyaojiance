package com.med.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TodayMedicationDTO {
    private Long recordId;
    private Long scheduleId;
    private String scheduleName;
    private LocalDateTime scheduledTime;
    private String status;
    private LocalDateTime actualTime;

    // 包含的药品列表
    private List<MedicineItem> medicines;

    @Data
    public static class MedicineItem {
        private Long medicineId;
        private String medicineName;
        private String specification;
        private BigDecimal dosage;
    }
}
