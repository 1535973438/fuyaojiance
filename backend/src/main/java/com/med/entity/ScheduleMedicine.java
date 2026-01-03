package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("schedule_medicine")
public class ScheduleMedicine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private Long medicineId;

    private BigDecimal dosage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String medicineName;

    @TableField(exist = false)
    private String specification;
}
