package com.med.service;

import com.med.dto.TakeRecordDTO;
import com.med.dto.TodayMedicationDTO;
import java.time.LocalDate;
import java.util.List;

public interface RecordService {
    List<TodayMedicationDTO> getTodayMedications(LocalDate date);
    void takeMedication(TakeRecordDTO dto);
    void cancelTakeMedication(TakeRecordDTO dto);
    void supplementRecord(TakeRecordDTO dto);
    void generateDailyRecords(LocalDate date);
}
