package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("check_item")
public class CheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String unit;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private Boolean isPreset;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
