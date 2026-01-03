package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("check_record")
public class CheckRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkItemId;

    private LocalDate checkDate;

    private BigDecimal value;

    private String reportImage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
