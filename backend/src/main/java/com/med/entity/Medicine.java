package com.med.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medicine")
public class Medicine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String specification;

    private Integer stockQuantity;

    private Integer perBoxQuantity;

    private LocalDate purchaseDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
