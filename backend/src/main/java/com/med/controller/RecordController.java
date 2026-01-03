package com.med.controller;

import com.med.common.Result;
import com.med.dto.TakeRecordDTO;
import com.med.dto.TodayMedicationDTO;
import com.med.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/today")
    public Result<List<TodayMedicationDTO>> getTodayMedications(
            @RequestParam(required = false) String date) {
        java.time.LocalDate queryDate = null;
        if (date != null && !date.isEmpty()) {
            try {
                queryDate = java.time.LocalDate.parse(date);
            } catch (Exception e) {
                return Result.error(400, "日期格式错误，请使用YYYY-MM-DD格式");
            }
        }
        return Result.success(recordService.getTodayMedications(queryDate));
    }

    @PostMapping("/take")
    public Result<Void> takeMedication(@RequestBody TakeRecordDTO dto) {
        recordService.takeMedication(dto);
        return Result.success();
    }

    @PostMapping("/cancel")
    public Result<Void> cancelTakeMedication(@RequestBody TakeRecordDTO dto) {
        recordService.cancelTakeMedication(dto);
        return Result.success();
    }

    @PostMapping("/supplement")
    public Result<Void> supplementRecord(@RequestBody TakeRecordDTO dto) {
        recordService.supplementRecord(dto);
        return Result.success();
    }
}
