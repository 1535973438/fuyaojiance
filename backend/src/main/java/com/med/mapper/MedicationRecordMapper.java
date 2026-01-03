package com.med.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.med.entity.MedicationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface MedicationRecordMapper extends BaseMapper<MedicationRecord> {

    @Select("SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'TAKEN' THEN 1 ELSE 0 END) as taken, " +
            "SUM(CASE WHEN status = 'MISSED' THEN 1 ELSE 0 END) as missed " +
            "FROM medication_record " +
            "WHERE scheduled_time BETWEEN #{start} AND #{end}")
    Map<String, Object> getCompletionStats(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
}
