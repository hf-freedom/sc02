package com.example.excelpreview.service;

import com.example.excelpreview.dto.ExcelDataRow;
import com.example.excelpreview.dto.ExcelUploadTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SIExcelDataService {
    
    ExcelUploadTask uploadExcel(MultipartFile file);
    
    ExcelUploadTask getTask(String taskId);
    
    ExcelUploadTask updateRow(String taskId, int rowIndex, ExcelDataRow updatedRow);
    
    ExcelUploadTask deleteRow(String taskId, int rowIndex);
    
    ExcelUploadTask addRow(String taskId, ExcelDataRow newRow);
    
    boolean saveTask(String taskId);
    
    List<ExcelDataRow> validateTaskData(String taskId);
    
    ExcelUploadTask revalidateTask(String taskId);
}
