package com.example.excelpreview.service;

import java.util.Map;

public interface SIValidationService {
    
    Map<String, String> validateRow(String templateType, Map<String, Object> data);
    
    boolean validatePhone(String phone);
    
    boolean validateEmail(String email);
    
    boolean validateOrgCode(String orgCode);
    
    boolean validateRequired(String value);
}
