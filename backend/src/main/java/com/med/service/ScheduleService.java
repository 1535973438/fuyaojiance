package com.med.service;

import com.med.dto.ScheduleDTO;
import java.util.List;

public interface ScheduleService {
    List<ScheduleDTO> listAll();
    ScheduleDTO getById(Long id);
    ScheduleDTO create(ScheduleDTO dto);
    ScheduleDTO update(Long id, ScheduleDTO dto);
    void delete(Long id);
}
