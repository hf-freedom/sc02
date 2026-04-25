package com.example.excelpreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ExcelPreviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelPreviewApplication.class, args);
    }

}
