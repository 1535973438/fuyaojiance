package com.med.service;

import com.med.dto.CheckRecordDTO;
import com.med.entity.CheckItem;
import com.med.entity.CheckRecord;
import java.util.List;

public interface CheckService {
    List<CheckItem> listItems();
    List<CheckRecordDTO> listRecords(Long checkItemId);
    CheckRecord createRecord(CheckRecord record);
    CheckRecord updateRecord(Long id, CheckRecord record);
    void deleteRecord(Long id);
    String uploadImage(byte[] fileData, String originalFilename);
}
