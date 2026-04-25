package com.example.excelpreview.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
public class TemplateInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TemplateInitializer.class);

    @Value("${excel.templates.user:user_template.xlsx}")
    private String userTemplateName;

    @Value("${excel.templates.organization:organization_template.xlsx}")
    private String orgTemplateName;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化Excel模板文件...");
        
        try {
            ClassPathResource resource = new ClassPathResource("templates");
            File templatesDir;
            
            if (resource.exists()) {
                templatesDir = resource.getFile();
            } else {
                Path path = Paths.get("src", "main", "resources", "templates");
                Files.createDirectories(path);
                templatesDir = path.toFile();
            }
            
            createUserTemplate(templatesDir);
            createOrgTemplate(templatesDir);
            
            log.info("Excel模板文件初始化完成");
        } catch (Exception e) {
            log.error("初始化Excel模板失败", e);
        }
    }

    private void createUserTemplate(File templatesDir) throws IOException {
        File templateFile = new File(templatesDir, userTemplateName);
        
        if (templateFile.exists()) {
            log.info("用户模板已存在，跳过创建: {}", templateFile.getAbsolutePath());
            return;
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户数据");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row templateIdRow = sheet.createRow(0);
            Cell templateIdCell = templateIdRow.createCell(0);
            templateIdCell.setCellValue("USER_001");
            templateIdCell.setCellStyle(headerStyle);
            
            Row headerRow = sheet.createRow(1);
            List<String> headers = Arrays.asList("用户名称", "手机号", "邮箱", "部门", "职位");
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            Row exampleRow1 = sheet.createRow(2);
            String[] example1 = {"张三", "13800138001", "zhangsan@example.com", "技术部", "开发工程师"};
            for (int i = 0; i < example1.length; i++) {
                Cell cell = exampleRow1.createCell(i);
                cell.setCellValue(example1[i]);
                cell.setCellStyle(dataStyle);
            }
            
            Row exampleRow2 = sheet.createRow(3);
            String[] example2 = {"李四", "13800138002", "lisi@example.com", "市场部", "产品经理"};
            for (int i = 0; i < example2.length; i++) {
                Cell cell = exampleRow2.createCell(i);
                cell.setCellValue(example2[i]);
                cell.setCellStyle(dataStyle);
            }
            
            try (FileOutputStream fos = new FileOutputStream(templateFile)) {
                workbook.write(fos);
            }
            
            log.info("用户模板创建成功: {}", templateFile.getAbsolutePath());
        }
    }

    private void createOrgTemplate(File templatesDir) throws IOException {
        File templateFile = new File(templatesDir, orgTemplateName);
        
        if (templateFile.exists()) {
            log.info("机构模板已存在，跳过创建: {}", templateFile.getAbsolutePath());
            return;
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("机构数据");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row templateIdRow = sheet.createRow(0);
            Cell templateIdCell = templateIdRow.createCell(0);
            templateIdCell.setCellValue("ORG_001");
            templateIdCell.setCellStyle(headerStyle);
            
            Row headerRow = sheet.createRow(1);
            List<String> headers = Arrays.asList("机构名称", "机构代码", "联系人", "联系电话", "地址");
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4500);
            }
            
            Row exampleRow1 = sheet.createRow(2);
            String[] example1 = {"某某科技有限公司", "ABC12345-1", "王五", "13900139001", "北京市朝阳区某某路123号"};
            for (int i = 0; i < example1.length; i++) {
                Cell cell = exampleRow1.createCell(i);
                cell.setCellValue(example1[i]);
                cell.setCellStyle(dataStyle);
            }
            
            Row exampleRow2 = sheet.createRow(3);
            String[] example2 = {"某某咨询服务中心", "DEF67890-2", "赵六", "13900139002", "上海市浦东新区某某大厦8楼"};
            for (int i = 0; i < example2.length; i++) {
                Cell cell = exampleRow2.createCell(i);
                cell.setCellValue(example2[i]);
                cell.setCellStyle(dataStyle);
            }
            
            try (FileOutputStream fos = new FileOutputStream(templateFile)) {
                workbook.write(fos);
            }
            
            log.info("机构模板创建成功: {}", templateFile.getAbsolutePath());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}
