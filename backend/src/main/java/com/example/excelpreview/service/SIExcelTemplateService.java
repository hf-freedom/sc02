package com.example.excelpreview.service;

import org.springframework.core.io.Resource;
import java.util.List;
import java.util.Map;

public interface SIExcelTemplateService {
    
    List<Map<String, Object>> getAvailableTemplates();
    
    Resource downloadTemplate(String templateType);
    
    String getTemplateIdByType(String templateType);
    
    String getTemplateTypeById(String templateId);
    
    List<String> getTemplateColumns(String templateType);
    
    Map<String, String> getTemplateColumnMapping(String templateType);
}
