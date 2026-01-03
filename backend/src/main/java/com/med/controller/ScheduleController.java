package com.med.controller;

import com.med.common.Result;
import com.med.dto.ScheduleDTO;
import com.med.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public Result<List<ScheduleDTO>> list() {
        return Result.success(scheduleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<ScheduleDTO> get(@PathVariable Long id) {
        return Result.success(scheduleService.getById(id));
    }

    @PostMapping
    public Result<ScheduleDTO> create(@RequestBody ScheduleDTO dto) {
        return Result.success(scheduleService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<ScheduleDTO> update(@PathVariable Long id, @RequestBody ScheduleDTO dto) {
        return Result.success(scheduleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.success();
    }
}
