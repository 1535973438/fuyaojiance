package com.med.service;

import com.med.dto.MedicineDTO;
import com.med.entity.Medicine;
import java.util.List;

public interface MedicineService {
    List<MedicineDTO> listAll();
    MedicineDTO getById(Long id);
    Medicine create(Medicine medicine);
    Medicine update(Long id, Medicine medicine);
    void delete(Long id);
    double getDailyConsumption(Long medicineId);
    List<MedicineDTO> getLowStockMedicines();
}
