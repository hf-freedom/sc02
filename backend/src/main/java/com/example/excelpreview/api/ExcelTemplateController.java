package com.example.excelpreview.api;

import com.example.excelpreview.dto.ApiResponse;
import com.example.excelpreview.service.SIExcelTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class ExcelTemplateController {

    private static final Logger log = LoggerFactory.getLogger(ExcelTemplateController.class);

    @Autowired
    private SIExcelTemplateService templateService;

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getTemplateList() {
        log.info("获取可用模板列表");
        List<Map<String, Object>> templates = templateService.getAvailableTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping("/download/{templateType}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable String templateType) {
        log.info("下载模板: {}", templateType);
        try {
            Resource resource = templateService.downloadTemplate(templateType);
            String filename = resource.getFilename();
            if (filename == null) {
                filename = templateType + "_template.xlsx";
            }
            
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("模板不存在: {}", templateType, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("下载模板失败: {}", templateType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/columns/{templateType}")
    public ApiResponse<Map<String, Object>> getTemplateColumns(@PathVariable String templateType) {
        log.info("获取模板列定义: {}", templateType);
        List<String> columns = templateService.getTemplateColumns(templateType);
        Map<String, String> columnMapping = templateService.getTemplateColumnMapping(templateType);
        
        if (columns.isEmpty()) {
            return ApiResponse.error("不存在的模板类型: " + templateType);
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("columns", columns);
        result.put("columnMapping", columnMapping);
        
        return ApiResponse.success(result);
    }
}
