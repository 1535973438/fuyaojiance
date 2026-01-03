package com.med.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.med.entity.CheckRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckRecordMapper extends BaseMapper<CheckRecord> {

    @Select("SELECT cr.*, ci.name as item_name, ci.unit, ci.reference_min, ci.reference_max " +
            "FROM check_record cr " +
            "LEFT JOIN check_item ci ON cr.check_item_id = ci.id " +
            "WHERE cr.check_item_id = #{itemId} " +
            "AND cr.check_date BETWEEN #{start} AND #{end} " +
            "ORDER BY cr.check_date ASC")
    List<Map<String, Object>> getTrendData(@Param("itemId") Long itemId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Select("SELECT cr.*, ci.name as item_name, ci.unit " +
            "FROM check_record cr " +
            "LEFT JOIN check_item ci ON cr.check_item_id = ci.id " +
            "WHERE ci.is_preset = 1 " +
            "AND cr.id IN (SELECT MAX(id) FROM check_record GROUP BY check_item_id) " +
            "ORDER BY cr.check_date DESC")
    List<Map<String, Object>> getLatestPresetChecks();
}
