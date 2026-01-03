package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medication_record")
public class MedicationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private LocalDateTime scheduledTime;

    private LocalDateTime actualTime;

    private RecordStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum RecordStatus {
        PENDING,    // 待服
        TAKEN,      // 已服
        MISSED      // 漏服
    }
}
