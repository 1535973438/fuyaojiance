package com.med.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.med.entity.ScheduleMedicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScheduleMedicineMapper extends BaseMapper<ScheduleMedicine> {

    @Select("SELECT sm.*, m.name as medicine_name, m.specification " +
            "FROM schedule_medicine sm " +
            "LEFT JOIN medicine m ON sm.medicine_id = m.id " +
            "WHERE sm.schedule_id = #{scheduleId}")
    List<Map<String, Object>> getWithMedicineInfo(@Param("scheduleId") Long scheduleId);
}
