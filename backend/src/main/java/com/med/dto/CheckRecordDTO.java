package com.med.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CheckRecordDTO {
    private Long id;
    private Long checkItemId;
    private String itemName;
    private String unit;
    private LocalDate checkDate;
    private BigDecimal value;
    private String reportImage;
    private BigDecimal referenceMin;
    private BigDecimal referenceMax;
    private Boolean isNormal;
}
