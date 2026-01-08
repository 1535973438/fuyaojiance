package com.med.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TakeRecordDTO {
    private Long recordId;
    private Long scheduleId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualTime; // 补服时的实际服用时间（可选）
}
