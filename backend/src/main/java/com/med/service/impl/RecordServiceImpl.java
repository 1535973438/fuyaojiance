package com.med.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.med.dto.TakeRecordDTO;
import com.med.dto.TodayMedicationDTO;
import com.med.dto.TodayMedicationDTO.MedicineItem;
import com.med.entity.Medicine;
import com.med.entity.MedicationRecord;
import com.med.entity.MedicationSchedule;
import com.med.entity.ScheduleMedicine;
import com.med.mapper.MedicationRecordMapper;
import com.med.mapper.MedicationScheduleMapper;
import com.med.mapper.MedicineMapper;
import com.med.mapper.ScheduleMedicineMapper;
import com.med.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MedicationRecordMapper recordMapper;
    private final MedicationScheduleMapper scheduleMapper;
    private final ScheduleMedicineMapper scheduleMedicineMapper;
    private final MedicineMapper medicineMapper;

    @Override
    public List<TodayMedicationDTO> getTodayMedications(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // 查询指定日期的记录
        LambdaQueryWrapper<MedicationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(MedicationRecord::getScheduledTime, startOfDay, endOfDay)
               .orderByAsc(MedicationRecord::getScheduledTime);

        List<MedicationRecord> records = recordMapper.selectList(wrapper);

        // 获取计划信息
        Set<Long> scheduleIds = records.stream()
                .map(MedicationRecord::getScheduleId)
                .collect(Collectors.toSet());

        Map<Long, MedicationSchedule> scheduleMap = new HashMap<>();
        Map<Long, List<MedicineItem>> scheduleMedicinesMap = new HashMap<>();

        if (!scheduleIds.isEmpty()) {
            scheduleMapper.selectBatchIds(scheduleIds)
                    .forEach(s -> scheduleMap.put(s.getId(), s));

            // 获取每个计划的药品列表
            for (Long scheduleId : scheduleIds) {
                LambdaQueryWrapper<ScheduleMedicine> smWrapper = new LambdaQueryWrapper<>();
                smWrapper.eq(ScheduleMedicine::getScheduleId, scheduleId);
                List<ScheduleMedicine> smList = scheduleMedicineMapper.selectList(smWrapper);

                List<MedicineItem> items = new ArrayList<>();
                for (ScheduleMedicine sm : smList) {
                    Medicine medicine = medicineMapper.selectById(sm.getMedicineId());
                    if (medicine != null) {
                        MedicineItem item = new MedicineItem();
                        item.setMedicineId(medicine.getId());
                        item.setMedicineName(medicine.getName());
                        item.setSpecification(medicine.getSpecification());
                        item.setDosage(sm.getDosage());
                        items.add(item);
                    }
                }
                scheduleMedicinesMap.put(scheduleId, items);
            }
        }

        // 转换为DTO
        return records.stream().map(record -> {
            TodayMedicationDTO dto = new TodayMedicationDTO();
            dto.setRecordId(record.getId());
            dto.setScheduleId(record.getScheduleId());
            dto.setScheduledTime(record.getScheduledTime());
            dto.setStatus(record.getStatus().name());
            dto.setActualTime(record.getActualTime());

            MedicationSchedule schedule = scheduleMap.get(record.getScheduleId());
            if (schedule != null) {
                dto.setScheduleName(schedule.getName());
            }

            dto.setMedicines(scheduleMedicinesMap.getOrDefault(record.getScheduleId(), new ArrayList<>()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void takeMedication(TakeRecordDTO dto) {
        MedicationRecord record;

        if (dto.getRecordId() != null) {
            record = recordMapper.selectById(dto.getRecordId());
            if (record == null) {
                throw new IllegalArgumentException("记录不存在");
            }
        } else {
            throw new IllegalArgumentException("记录ID不能为空");
        }

        record.setStatus(MedicationRecord.RecordStatus.TAKEN);
        record.setActualTime(LocalDateTime.now());
        recordMapper.updateById(record);

        // 扣减库存 - 获取该计划下所有药品并扣减
        LambdaQueryWrapper<ScheduleMedicine> smWrapper = new LambdaQueryWrapper<>();
        smWrapper.eq(ScheduleMedicine::getScheduleId, record.getScheduleId());
        List<ScheduleMedicine> smList = scheduleMedicineMapper.selectList(smWrapper);

        for (ScheduleMedicine sm : smList) {
            Medicine medicine = medicineMapper.selectById(sm.getMedicineId());
            if (medicine != null && medicine.getStockQuantity() != null) {
                int newStock = medicine.getStockQuantity() - sm.getDosage().intValue();
                medicine.setStockQuantity(Math.max(0, newStock));
                medicineMapper.updateById(medicine);
            }
        }
    }

    @Override
    @Transactional
    public void cancelTakeMedication(TakeRecordDTO dto) {
        MedicationRecord record;

        if (dto.getRecordId() != null) {
            record = recordMapper.selectById(dto.getRecordId());
            if (record == null) {
                throw new IllegalArgumentException("记录不存在");
            }
        } else {
            throw new IllegalArgumentException("记录ID不能为空");
        }

        // 检查记录状态是否为已服
        if (record.getStatus() != MedicationRecord.RecordStatus.TAKEN) {
            throw new IllegalArgumentException("该记录不是已服状态，无法撤销");
        }

        // 将状态改为待服
        record.setStatus(MedicationRecord.RecordStatus.PENDING);
        record.setActualTime(null);
        recordMapper.updateById(record);

        // 恢复库存 - 获取该计划下所有药品并恢复库存
        LambdaQueryWrapper<ScheduleMedicine> smWrapper = new LambdaQueryWrapper<>();
        smWrapper.eq(ScheduleMedicine::getScheduleId, record.getScheduleId());
        List<ScheduleMedicine> smList = scheduleMedicineMapper.selectList(smWrapper);

        for (ScheduleMedicine sm : smList) {
            Medicine medicine = medicineMapper.selectById(sm.getMedicineId());
            if (medicine != null && medicine.getStockQuantity() != null) {
                // 恢复库存（加回扣减的剂量）
                int newStock = medicine.getStockQuantity() + sm.getDosage().intValue();
                medicine.setStockQuantity(newStock);
                medicineMapper.updateById(medicine);
            }
        }
    }

    @Override
    @Transactional
    public void supplementRecord(TakeRecordDTO dto) {
        MedicationRecord record;

        if (dto.getRecordId() != null) {
            record = recordMapper.selectById(dto.getRecordId());
            if (record == null) {
                throw new IllegalArgumentException("记录不存在");
            }
        } else {
            throw new IllegalArgumentException("记录ID不能为空");
        }

        // 检查记录状态
        if (record.getStatus() == MedicationRecord.RecordStatus.TAKEN) {
            throw new IllegalArgumentException("该记录已经是已服状态，无需补服");
        }

        // 判断是否需要填写服用时间
        LocalDateTime actualTime;
        LocalDateTime scheduledTime = record.getScheduledTime();
        LocalDateTime now = LocalDateTime.now();
        
        // 如果设定时间后超过两小时，必须填写服用时间
        long hoursDiff = ChronoUnit.HOURS.between(scheduledTime, now);
        if (hoursDiff > 2) {
            // 超过两小时，必须填写服用时间
            if (dto.getActualTime() == null) {
                throw new IllegalArgumentException("超过设定时间两小时，必须填写服用时间");
            }
            actualTime = dto.getActualTime();
        } else {
            // 两小时内，使用当前时间
            actualTime = now;
        }

        // 设置状态和服用时间
        record.setStatus(MedicationRecord.RecordStatus.TAKEN);
        record.setActualTime(actualTime);
        recordMapper.updateById(record);

        // 扣减库存 - 获取该计划下所有药品并扣减
        LambdaQueryWrapper<ScheduleMedicine> smWrapper = new LambdaQueryWrapper<>();
        smWrapper.eq(ScheduleMedicine::getScheduleId, record.getScheduleId());
        List<ScheduleMedicine> smList = scheduleMedicineMapper.selectList(smWrapper);

        for (ScheduleMedicine sm : smList) {
            Medicine medicine = medicineMapper.selectById(sm.getMedicineId());
            if (medicine != null && medicine.getStockQuantity() != null) {
                int newStock = medicine.getStockQuantity() - sm.getDosage().intValue();
                medicine.setStockQuantity(Math.max(0, newStock));
                medicineMapper.updateById(medicine);
            }
        }
    }

    @Override
    @Transactional
    public void generateDailyRecords(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // 获取所有活跃的计划
        LambdaQueryWrapper<MedicationSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(MedicationSchedule::getIsActive, true);
        List<MedicationSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);

        for (MedicationSchedule schedule : schedules) {
            // 先删除该计划当天的所有记录（确保幂等性）
            LambdaQueryWrapper<MedicationRecord> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(MedicationRecord::getScheduleId, schedule.getId())
                    .between(MedicationRecord::getScheduledTime, startOfDay, endOfDay);
            recordMapper.delete(deleteWrapper);

            // 检查该计划是否应该在当天服药
            if (!shouldTakeMedicineOnDate(schedule, date)) {
                continue;
            }

            // 为该时间点创建一条记录
            LocalDateTime scheduledTime = date.atTime(schedule.getScheduleTime());

            MedicationRecord record = new MedicationRecord();
            record.setScheduleId(schedule.getId());
            record.setScheduledTime(scheduledTime);
            record.setStatus(MedicationRecord.RecordStatus.PENDING);

            recordMapper.insert(record);
        }
    }

    private boolean shouldTakeMedicineOnDate(MedicationSchedule schedule, LocalDate date) {
        // 检查日期范围
        if (schedule.getStartDate() != null && date.isBefore(schedule.getStartDate())) {
            return false;
        }
        if (schedule.getEndDate() != null && date.isAfter(schedule.getEndDate())) {
            return false;
        }

        switch (schedule.getScheduleType()) {
            case DAILY:
                return true;

            case INTERVAL:
                if (schedule.getStartDate() != null && schedule.getIntervalDays() != null) {
                    long daysSinceStart = ChronoUnit.DAYS.between(schedule.getStartDate(), date);
                    return daysSinceStart % schedule.getIntervalDays() == 0;
                }
                return false;

            case WEEKLY:
                if (schedule.getWeekDays() != null && !schedule.getWeekDays().isEmpty()) {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    int dayValue = dayOfWeek.getValue();
                    String[] days = schedule.getWeekDays().split(",");
                    for (String day : days) {
                        if (Integer.parseInt(day.trim()) == dayValue) {
                            return true;
                        }
                    }
                }
                return false;

            default:
                return false;
        }
    }
}
