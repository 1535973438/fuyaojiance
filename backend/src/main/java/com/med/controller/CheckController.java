package com.med.controller;

import com.med.common.Result;
import com.med.dto.CheckRecordDTO;
import com.med.entity.CheckItem;
import com.med.entity.CheckRecord;
import com.med.service.CheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @GetMapping("/check-items")
    public Result<List<CheckItem>> listItems() {
        return Result.success(checkService.listItems());
    }

    @GetMapping("/check-records")
    public Result<List<CheckRecordDTO>> listRecords(
            @RequestParam(required = false) Long checkItemId) {
        return Result.success(checkService.listRecords(checkItemId));
    }

    @PostMapping("/check-records")
    public Result<CheckRecord> createRecord(@RequestBody CheckRecord record) {
        return Result.success(checkService.createRecord(record));
    }

    @PutMapping("/check-records/{id}")
    public Result<CheckRecord> updateRecord(@PathVariable Long id, @RequestBody CheckRecord record) {
        return Result.success(checkService.updateRecord(id, record));
    }

    @DeleteMapping("/check-records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        checkService.deleteRecord(id);
        return Result.success();
    }

    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = checkService.uploadImage(file.getBytes(), file.getOriginalFilename());
        return Result.success(url);
    }
}
