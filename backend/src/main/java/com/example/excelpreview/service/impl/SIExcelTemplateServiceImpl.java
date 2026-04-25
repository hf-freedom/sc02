package com.example.excelpreview.service.impl;

import com.example.excelpreview.service.SIExcelTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class SIExcelTemplateServiceImpl implements SIExcelTemplateService {

    private static final Logger log = LoggerFactory.getLogger(SIExcelTemplateServiceImpl.class);

    private final Map<String, TemplateInfo> templateMap = new HashMap<>();

    @PostConstruct
    public void init() {
        templateMap.put("user", new TemplateInfo(
                "USER_001",
                "用户模板",
                "user_template.xlsx",
                Arrays.asList("用户名称", "手机号", "邮箱", "部门", "职位"),
                createUserColumnMapping()
        ));
        
        templateMap.put("organization", new TemplateInfo(
                "ORG_001",
                "机构模板",
                "organization_template.xlsx",
                Arrays.asList("机构名称", "机构代码", "联系人", "联系电话", "地址"),
                createOrgColumnMapping()
        ));
    }

    private Map<String, String> createUserColumnMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("用户名称", "userName");
        mapping.put("手机号", "phone");
        mapping.put("邮箱", "email");
        mapping.put("部门", "department");
        mapping.put("职位", "position");
        return mapping;
    }

    private Map<String, String> createOrgColumnMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("机构名称", "orgName");
        mapping.put("机构代码", "orgCode");
        mapping.put("联系人", "contactPerson");
        mapping.put("联系电话", "contactPhone");
        mapping.put("地址", "address");
        return mapping;
    }

    @Override
    public List<Map<String, Object>> getAvailableTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        for (Map.Entry<String, TemplateInfo> entry : templateMap.entrySet()) {
            Map<String, Object> template = new HashMap<>();
            template.put("type", entry.getKey());
            template.put("name", entry.getValue().getName());
            template.put("templateId", entry.getValue().getTemplateId());
            templates.add(template);
        }
        return templates;
    }

    @Override
    public Resource downloadTemplate(String templateType) {
        TemplateInfo templateInfo = templateMap.get(templateType);
        if (templateInfo == null) {
            throw new IllegalArgumentException("不存在的模板类型: " + templateType);
        }
        return new ClassPathResource("templates/" + templateInfo.getFileName());
    }

    @Override
    public String getTemplateIdByType(String templateType) {
        TemplateInfo templateInfo = templateMap.get(templateType);
        return templateInfo != null ? templateInfo.getTemplateId() : null;
    }

    @Override
    public String getTemplateTypeById(String templateId) {
        for (Map.Entry<String, TemplateInfo> entry : templateMap.entrySet()) {
            if (entry.getValue().getTemplateId().equals(templateId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public List<String> getTemplateColumns(String templateType) {
        TemplateInfo templateInfo = templateMap.get(templateType);
        return templateInfo != null ? templateInfo.getColumns() : Collections.emptyList();
    }

    @Override
    public Map<String, String> getTemplateColumnMapping(String templateType) {
        TemplateInfo templateInfo = templateMap.get(templateType);
        return templateInfo != null ? templateInfo.getColumnMapping() : Collections.emptyMap();
    }

    private static class TemplateInfo {
        private final String templateId;
        private final String name;
        private final String fileName;
        private final List<String> columns;
        private final Map<String, String> columnMapping;

        public TemplateInfo(String templateId, String name, String fileName, List<String> columns, Map<String, String> columnMapping) {
            this.templateId = templateId;
            this.name = name;
            this.fileName = fileName;
            this.columns = columns;
            this.columnMapping = columnMapping;
        }

        public String getTemplateId() {
            return templateId;
        }

        public String getName() {
            return name;
        }

        public String getFileName() {
            return fileName;
        }

        public List<String> getColumns() {
            return columns;
        }

        public Map<String, String> getColumnMapping() {
            return columnMapping;
        }
    }
}
