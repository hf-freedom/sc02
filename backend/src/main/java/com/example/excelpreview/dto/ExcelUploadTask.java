package com.example.excelpreview.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ExcelUploadTask {
    private String taskId;
    private String templateId;
    private String templateType;
    private List<ExcelDataRow> dataRows;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public List<ExcelDataRow> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<ExcelDataRow> dataRows) {
        this.dataRows = dataRows;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
