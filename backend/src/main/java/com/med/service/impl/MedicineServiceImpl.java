package com.med.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.med.dto.MedicineDTO;
import com.med.entity.Medicine;
import com.med.entity.MedicationSchedule;
import com.med.mapper.MedicineMapper;
import com.med.mapper.MedicationScheduleMapper;
import com.med.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineMapper medicineMapper;
    private final MedicationScheduleMapper scheduleMapper;

    @Override
    public List<MedicineDTO> listAll() {
        List<Medicine> medicines = medicineMapper.selectList(null);
        return medicines.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public MedicineDTO getById(Long id) {
        Medicine medicine = medicineMapper.selectById(id);
        if (medicine == null) {
            throw new IllegalArgumentException("药品不存在");
        }
        return convertToDTO(medicine);
    }

    @Override
    public Medicine create(Medicine medicine) {
        medicineMapper.insert(medicine);
        return medicine;
    }

    @Override
    public Medicine update(Long id, Medicine medicine) {
        Medicine existing = medicineMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("药品不存在");
        }
        medicine.setId(id);
        medicineMapper.updateById(medicine);
        return medicine;
    }

    @Override
    public void delete(Long id) {
        medicineMapper.deleteById(id);
    }

    @Override
    public double getDailyConsumption(Long medicineId) {
        LambdaQueryWrapper<MedicationSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationSchedule::getMedicineId, medicineId)
               .eq(MedicationSchedule::getIsActive, true);

        List<MedicationSchedule> schedules = scheduleMapper.selectList(wrapper);

        double totalDaily = 0;
        LocalDate today = LocalDate.now();

        for (MedicationSchedule schedule : schedules) {
            // 检查是否在有效期内
            if (schedule.getStartDate() != null && today.isBefore(schedule.getStartDate())) {
                continue;
            }
            if (schedule.getEndDate() != null && today.isAfter(schedule.getEndDate())) {
                continue;
            }

            double dailyDosage = 0;
            if (schedule.getDosageConfig() != null) {
                dailyDosage = schedule.getDosageConfig().values().stream()
                        .mapToDouble(Double::doubleValue).sum();
            }

            switch (schedule.getScheduleType()) {
                case DAILY:
                case COURSE:
                    totalDaily += dailyDosage;
                    break;
                case INTERVAL:
                    if (schedule.getIntervalDays() != null && schedule.getIntervalDays() > 0) {
                        totalDaily += dailyDosage / schedule.getIntervalDays();
                    }
                    break;
                case WEEKLY:
                    if (schedule.getWeekDays() != null && !schedule.getWeekDays().isEmpty()) {
                        int daysPerWeek = schedule.getWeekDays().split(",").length;
                        totalDaily += dailyDosage * daysPerWeek / 7.0;
                    }
                    break;
            }
        }

        return totalDaily;
    }

    private MedicineDTO convertToDTO(Medicine medicine) {
        MedicineDTO dto = new MedicineDTO();
        BeanUtils.copyProperties(medicine, dto);

        // 计算剩余天数
        double dailyConsumption = getDailyConsumption(medicine.getId());
        if (dailyConsumption > 0 && medicine.getStockQuantity() != null) {
            int remainingDays = (int) (medicine.getStockQuantity() / dailyConsumption);
            dto.setRemainingDays(remainingDays);
            dto.setStockWarning(remainingDays <= 7);
        } else {
            dto.setRemainingDays(null);
            dto.setStockWarning(false);
        }

        return dto;
    }
}
