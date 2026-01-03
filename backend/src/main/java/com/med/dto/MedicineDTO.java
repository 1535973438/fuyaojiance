package com.med.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MedicineDTO {
    private Long id;
    private String name;
    private String specification;
    private Integer stockQuantity;
    private Integer perBoxQuantity;
    private LocalDate purchaseDate;

    // 计算字段
    private Integer remainingDays;
    private Boolean stockWarning;
}
