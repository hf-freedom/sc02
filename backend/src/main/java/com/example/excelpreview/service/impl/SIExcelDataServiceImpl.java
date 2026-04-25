package com.example.excelpreview.service.impl;

import com.example.excelpreview.dto.ExcelDataRow;
import com.example.excelpreview.dto.ExcelUploadTask;
import com.example.excelpreview.service.SIExcelDataService;
import com.example.excelpreview.service.SIExcelTemplateService;
import com.example.excelpreview.service.SIValidationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SIExcelDataServiceImpl implements SIExcelDataService {

    private static final Logger log = LoggerFactory.getLogger(SIExcelDataServiceImpl.class);
    private static final String CACHE_NAME = "excelTasks";
    
    @Autowired
    private SIExcelTemplateService templateService;
    
    @Autowired
    private SIValidationService validationService;
    
    @Autowired
    private CacheManager cacheManager;

    @Override
    public ExcelUploadTask uploadExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            Row templateIdRow = sheet.getRow(0);
            String templateId = getCellValue(templateIdRow.getCell(0));
            log.info("读取到模板ID: {}", templateId);
            
            String templateType = templateService.getTemplateTypeById(templateId);
            if (templateType == null) {
                throw new IllegalArgumentException("无法识别的模板ID: " + templateId);
            }
            
            List<ExcelDataRow> dataRows = parseExcelData(sheet, templateType);
            
            String taskId = generateTaskId();
            ExcelUploadTask task = new ExcelUploadTask();
            task.setTaskId(taskId);
            task.setTemplateId(templateId);
            task.setTemplateType(templateType);
            task.setDataRows(dataRows);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            
            validateAndCacheTask(task);
            
            return task;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        }
    }

    private List<ExcelDataRow> parseExcelData(Sheet sheet, String templateType) {
        List<ExcelDataRow> dataRows = new ArrayList<>();
        Map<String, String> columnMapping = templateService.getTemplateColumnMapping(templateType);
        List<String> headerColumns = new ArrayList<>(columnMapping.keySet());
        
        Row headerRow = sheet.getRow(1);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel格式错误：缺少表头行");
        }
        
        Map<Integer, String> columnIndexToField = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String headerName = getCellValue(headerRow.getCell(i));
            if (columnMapping.containsKey(headerName)) {
                columnIndexToField.put(i, columnMapping.get(headerName));
            }
        }
        
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }
            
            ExcelDataRow dataRow = new ExcelDataRow();
            dataRow.setRowIndex(i - 2);
            Map<String, Object> data = new LinkedHashMap<>();
            
            for (Map.Entry<Integer, String> entry : columnIndexToField.entrySet()) {
                Cell cell = row.getCell(entry.getKey());
                data.put(entry.getValue(), getCellValue(cell));
            }
            
            dataRow.setData(data);
            dataRow.setErrors(new HashMap<>());
            dataRow.setValid(true);
            
            dataRows.add(dataRow);
        }
        
        return dataRows;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void validateAndCacheTask(ExcelUploadTask task) {
        List<ExcelDataRow> validatedRows = new ArrayList<>();
        for (ExcelDataRow row : task.getDataRows()) {
            Map<String, String> errors = validationService.validateRow(
                    task.getTemplateType(), row.getData());
            row.setErrors(errors);
            row.setValid(errors.isEmpty());
            validatedRows.add(row);
        }
        task.setDataRows(validatedRows);
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(task.getTaskId(), task);
        }
    }

    private String generateTaskId() {
        return "TASK_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public ExcelUploadTask getTask(String taskId) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(taskId);
        return wrapper != null ? (ExcelUploadTask) wrapper.get() : null;
    }

    @Override
    public ExcelUploadTask updateRow(String taskId, int rowIndex, ExcelDataRow updatedRow) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        if (rowIndex < 0 || rowIndex >= task.getDataRows().size()) {
            throw new IllegalArgumentException("行索引超出范围: " + rowIndex);
        }
        
        updatedRow.setRowIndex(rowIndex);
        Map<String, String> errors = validationService.validateRow(
                task.getTemplateType(), updatedRow.getData());
        updatedRow.setErrors(errors);
        updatedRow.setValid(errors.isEmpty());
        
        task.getDataRows().set(rowIndex, updatedRow);
        task.setUpdateTime(LocalDateTime.now());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(taskId, task);
        }
        
        return task;
    }

    @Override
    public ExcelUploadTask deleteRow(String taskId, int rowIndex) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        if (rowIndex < 0 || rowIndex >= task.getDataRows().size()) {
            throw new IllegalArgumentException("行索引超出范围: " + rowIndex);
        }
        
        task.getDataRows().remove(rowIndex);
        
        for (int i = rowIndex; i < task.getDataRows().size(); i++) {
            task.getDataRows().get(i).setRowIndex(i);
        }
        
        task.setUpdateTime(LocalDateTime.now());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(taskId, task);
        }
        
        return task;
    }

    @Override
    public ExcelUploadTask addRow(String taskId, ExcelDataRow newRow) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        int newIndex = task.getDataRows().size();
        newRow.setRowIndex(newIndex);
        
        Map<String, String> errors = validationService.validateRow(
                task.getTemplateType(), newRow.getData());
        newRow.setErrors(errors);
        newRow.setValid(errors.isEmpty());
        
        task.getDataRows().add(newRow);
        task.setUpdateTime(LocalDateTime.now());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(taskId, task);
        }
        
        return task;
    }

    @Override
    public boolean saveTask(String taskId) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        for (ExcelDataRow row : task.getDataRows()) {
            if (!row.isValid()) {
                throw new IllegalStateException("数据存在验证错误，请先修正后再保存");
            }
        }
        
        log.info("保存任务数据，任务ID: {}, 数据行数: {}", taskId, task.getDataRows().size());
        
        return true;
    }

    @Override
    public List<ExcelDataRow> validateTaskData(String taskId) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        List<ExcelDataRow> validatedRows = new ArrayList<>();
        for (ExcelDataRow row : task.getDataRows()) {
            Map<String, String> errors = validationService.validateRow(
                    task.getTemplateType(), row.getData());
            row.setErrors(errors);
            row.setValid(errors.isEmpty());
            validatedRows.add(row);
        }
        
        return validatedRows;
    }

    @Override
    public ExcelUploadTask revalidateTask(String taskId) {
        ExcelUploadTask task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        List<ExcelDataRow> validatedRows = new ArrayList<>();
        for (ExcelDataRow row : task.getDataRows()) {
            Map<String, String> errors = validationService.validateRow(
                    task.getTemplateType(), row.getData());
            row.setErrors(errors);
            row.setValid(errors.isEmpty());
            validatedRows.add(row);
        }
        
        task.setDataRows(validatedRows);
        task.setUpdateTime(LocalDateTime.now());
        
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(taskId, task);
        }
        
        return task;
    }
}
