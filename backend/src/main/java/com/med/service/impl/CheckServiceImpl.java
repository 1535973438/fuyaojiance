package com.med.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.med.dto.CheckRecordDTO;
import com.med.entity.CheckItem;
import com.med.entity.CheckRecord;
import com.med.mapper.CheckItemMapper;
import com.med.mapper.CheckRecordMapper;
import com.med.service.CheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckServiceImpl implements CheckService {

    private final CheckItemMapper checkItemMapper;
    private final CheckRecordMapper checkRecordMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public List<CheckItem> listItems() {
        LambdaQueryWrapper<CheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CheckItem::getSortOrder);
        return checkItemMapper.selectList(wrapper);
    }

    @Override
    public List<CheckRecordDTO> listRecords(Long checkItemId) {
        LambdaQueryWrapper<CheckRecord> wrapper = new LambdaQueryWrapper<>();
        if (checkItemId != null) {
            wrapper.eq(CheckRecord::getCheckItemId, checkItemId);
        }
        wrapper.orderByDesc(CheckRecord::getCheckDate);

        List<CheckRecord> records = checkRecordMapper.selectList(wrapper);

        // 获取检查项信息
        List<CheckItem> items = checkItemMapper.selectList(null);
        Map<Long, CheckItem> itemMap = new HashMap<>();
        items.forEach(item -> itemMap.put(item.getId(), item));

        return records.stream().map(record -> {
            CheckRecordDTO dto = new CheckRecordDTO();
            BeanUtils.copyProperties(record, dto);

            CheckItem item = itemMap.get(record.getCheckItemId());
            if (item != null) {
                dto.setItemName(item.getName());
                dto.setUnit(item.getUnit());
                dto.setReferenceMin(item.getReferenceMin());
                dto.setReferenceMax(item.getReferenceMax());

                // 判断是否正常
                boolean isNormal = true;
                if (item.getReferenceMin() != null && record.getValue().compareTo(item.getReferenceMin()) < 0) {
                    isNormal = false;
                }
                if (item.getReferenceMax() != null && record.getValue().compareTo(item.getReferenceMax()) > 0) {
                    isNormal = false;
                }
                dto.setIsNormal(isNormal);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CheckRecord createRecord(CheckRecord record) {
        checkRecordMapper.insert(record);
        return record;
    }

    @Override
    public CheckRecord updateRecord(Long id, CheckRecord record) {
        CheckRecord existingRecord = checkRecordMapper.selectById(id);
        if (existingRecord == null) {
            throw new IllegalArgumentException("检查记录不存在");
        }

        record.setId(id);
        checkRecordMapper.updateById(record);
        return checkRecordMapper.selectById(id);
    }

    @Override
    public void deleteRecord(Long id) {
        checkRecordMapper.deleteById(id);
    }

    @Override
    public String uploadImage(byte[] fileData, String originalFilename) {
        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.write(filePath, fileData);

            return "/uploads/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
