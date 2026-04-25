package com.example.excelpreview.api;

import com.example.excelpreview.dto.ApiResponse;
import com.example.excelpreview.dto.ExcelDataRow;
import com.example.excelpreview.dto.ExcelUploadTask;
import com.example.excelpreview.service.SIExcelDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class ExcelDataController {

    private static final Logger log = LoggerFactory.getLogger(ExcelDataController.class);

    @Autowired
    private SIExcelDataService excelDataService;

    @PostMapping("/upload")
    public ApiResponse<ExcelUploadTask> uploadExcel(@RequestParam("file") MultipartFile file) {
        log.info("接收Excel上传请求，文件名: {}", file.getOriginalFilename());
        try {
            ExcelUploadTask task = excelDataService.uploadExcel(file);
            return ApiResponse.success("上传成功", task);
        } catch (IllegalArgumentException e) {
            log.error("Excel格式错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("上传Excel失败", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<ExcelUploadTask> getTask(@PathVariable String taskId) {
        log.info("获取任务详情: {}", taskId);
        ExcelUploadTask task = excelDataService.getTask(taskId);
        if (task == null) {
            return ApiResponse.error(404, "任务不存在或已过期");
        }
        return ApiResponse.success(task);
    }

    @PutMapping("/task/{taskId}/row/{rowIndex}")
    public ApiResponse<ExcelUploadTask> updateRow(
            @PathVariable String taskId,
            @PathVariable int rowIndex,
            @RequestBody ExcelDataRow updatedRow) {
        log.info("更新任务 {} 的第 {} 行数据", taskId, rowIndex);
        try {
            ExcelUploadTask task = excelDataService.updateRow(taskId, rowIndex, updatedRow);
            return ApiResponse.success("更新成功", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/task/{taskId}/row/{rowIndex}")
    public ApiResponse<ExcelUploadTask> deleteRow(
            @PathVariable String taskId,
            @PathVariable int rowIndex) {
        log.info("删除任务 {} 的第 {} 行数据", taskId, rowIndex);
        try {
            ExcelUploadTask task = excelDataService.deleteRow(taskId, rowIndex);
            return ApiResponse.success("删除成功", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/row")
    public ApiResponse<ExcelUploadTask> addRow(
            @PathVariable String taskId,
            @RequestBody ExcelDataRow newRow) {
        log.info("为任务 {} 添加新行数据", taskId);
        try {
            ExcelUploadTask task = excelDataService.addRow(taskId, newRow);
            return ApiResponse.success("添加成功", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/save")
    public ApiResponse<Boolean> saveTask(@PathVariable String taskId) {
        log.info("保存任务数据: {}", taskId);
        try {
            boolean success = excelDataService.saveTask(taskId);
            return ApiResponse.success("保存成功", success);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/revalidate")
    public ApiResponse<ExcelUploadTask> revalidateTask(@PathVariable String taskId) {
        log.info("重新验证任务数据: {}", taskId);
        try {
            ExcelUploadTask task = excelDataService.revalidateTask(taskId);
            return ApiResponse.success("验证完成", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}/validation")
    public ApiResponse<List<ExcelDataRow>> getValidationResults(@PathVariable String taskId) {
        log.info("获取任务验证结果: {}", taskId);
        try {
            List<ExcelDataRow> results = excelDataService.validateTaskData(taskId);
            return ApiResponse.success(results);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}
