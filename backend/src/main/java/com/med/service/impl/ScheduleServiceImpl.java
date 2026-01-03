package com.med.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.med.dto.ScheduleDTO;
import com.med.dto.ScheduleDTO.ScheduleMedicineDTO;
import com.med.entity.MedicationSchedule;
import com.med.entity.ScheduleMedicine;
import com.med.mapper.MedicationScheduleMapper;
import com.med.mapper.ScheduleMedicineMapper;
import com.med.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final MedicationScheduleMapper scheduleMapper;
    private final ScheduleMedicineMapper scheduleMedicineMapper;

    @Override
    public List<ScheduleDTO> listAll() {
        LambdaQueryWrapper<MedicationSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MedicationSchedule::getScheduleTime);

        List<MedicationSchedule> schedules = scheduleMapper.selectList(wrapper);

        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ScheduleDTO getById(Long id) {
        MedicationSchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new IllegalArgumentException("服药计划不存在");
        }
        return convertToDTO(schedule);
    }

    @Override
    @Transactional
    public ScheduleDTO create(ScheduleDTO dto) {
        MedicationSchedule schedule = new MedicationSchedule();
        BeanUtils.copyProperties(dto, schedule);
        if (schedule.getIsActive() == null) {
            schedule.setIsActive(true);
        }
        if (schedule.getScheduleType() == null) {
            schedule.setScheduleType(MedicationSchedule.ScheduleType.DAILY);
        }
        scheduleMapper.insert(schedule);

        // 保存药品关联
        if (dto.getMedicines() != null) {
            for (ScheduleMedicineDTO medDto : dto.getMedicines()) {
                ScheduleMedicine sm = new ScheduleMedicine();
                sm.setScheduleId(schedule.getId());
                sm.setMedicineId(medDto.getMedicineId());
                sm.setDosage(medDto.getDosage() != null ? medDto.getDosage() : BigDecimal.ONE);
                scheduleMedicineMapper.insert(sm);
            }
        }

        return getById(schedule.getId());
    }

    @Override
    @Transactional
    public ScheduleDTO update(Long id, ScheduleDTO dto) {
        MedicationSchedule existing = scheduleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("服药计划不存在");
        }

        MedicationSchedule schedule = new MedicationSchedule();
        BeanUtils.copyProperties(dto, schedule);
        schedule.setId(id);
        scheduleMapper.updateById(schedule);

        // 删除旧的药品关联，重新添加
        LambdaQueryWrapper<ScheduleMedicine> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ScheduleMedicine::getScheduleId, id);
        scheduleMedicineMapper.delete(deleteWrapper);

        if (dto.getMedicines() != null) {
            for (ScheduleMedicineDTO medDto : dto.getMedicines()) {
                ScheduleMedicine sm = new ScheduleMedicine();
                sm.setScheduleId(id);
                sm.setMedicineId(medDto.getMedicineId());
                sm.setDosage(medDto.getDosage() != null ? medDto.getDosage() : BigDecimal.ONE);
                scheduleMedicineMapper.insert(sm);
            }
        }

        return getById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        scheduleMapper.deleteById(id);
    }

    private ScheduleDTO convertToDTO(MedicationSchedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        BeanUtils.copyProperties(schedule, dto);

        // 获取关联的药品
        List<Map<String, Object>> medicines = scheduleMedicineMapper.getWithMedicineInfo(schedule.getId());
        List<ScheduleMedicineDTO> medicineDTOs = new ArrayList<>();

        for (Map<String, Object> m : medicines) {
            ScheduleMedicineDTO medDto = new ScheduleMedicineDTO();
            medDto.setId(((Number) m.get("id")).longValue());
            medDto.setMedicineId(((Number) m.get("medicine_id")).longValue());
            medDto.setMedicineName((String) m.get("medicine_name"));
            medDto.setSpecification((String) m.get("specification"));
            medDto.setDosage((BigDecimal) m.get("dosage"));
            medicineDTOs.add(medDto);
        }

        dto.setMedicines(medicineDTOs);
        return dto;
    }
}
