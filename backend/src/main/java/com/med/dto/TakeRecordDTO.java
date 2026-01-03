package com.med.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TakeRecordDTO {
    private Long recordId;
    private Long scheduleId;
    private LocalDateTime actualTime; // 补服时的实际服用时间（可选）
}
