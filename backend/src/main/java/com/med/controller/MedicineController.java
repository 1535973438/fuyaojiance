package com.med.controller;

import com.med.common.Result;
import com.med.dto.MedicineDTO;
import com.med.entity.Medicine;
import com.med.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public Result<List<MedicineDTO>> list() {
        return Result.success(medicineService.listAll());
    }

    @GetMapping("/{id}")
    public Result<MedicineDTO> get(@PathVariable Long id) {
        return Result.success(medicineService.getById(id));
    }

    @PostMapping
    public Result<Medicine> create(@RequestBody Medicine medicine) {
        return Result.success(medicineService.create(medicine));
    }

    @PutMapping("/{id}")
    public Result<Medicine> update(@PathVariable Long id, @RequestBody Medicine medicine) {
        return Result.success(medicineService.update(id, medicine));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return Result.success();
    }

    @GetMapping("/low-stock")
    public Result<List<MedicineDTO>> getLowStockMedicines() {
        return Result.success(medicineService.getLowStockMedicines());
    }
}
