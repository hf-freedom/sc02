package com.example.excelpreview.service.impl;

import com.example.excelpreview.service.SIValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SIValidationServiceImpl implements SIValidationService {

    private static final Logger log = LoggerFactory.getLogger(SIValidationServiceImpl.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern ORG_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{8}-[A-Z0-9]$");

    @Override
    public Map<String, String> validateRow(String templateType, Map<String, Object> data) {
        Map<String, String> errors = new HashMap<>();

        if ("user".equals(templateType)) {
            validateUserFields(data, errors);
        } else if ("organization".equals(templateType)) {
            validateOrganizationFields(data, errors);
        }

        return errors;
    }

    private void validateUserFields(Map<String, Object> data, Map<String, String> errors) {
        String userName = getStringValue(data.get("userName"));
        String phone = getStringValue(data.get("phone"));
        String email = getStringValue(data.get("email"));

        if (!validateRequired(userName)) {
            errors.put("userName", "用户名称不能为空");
        }

        if (!validateRequired(phone)) {
            errors.put("phone", "手机号不能为空");
        } else if (!validatePhone(phone)) {
            errors.put("phone", "手机号格式不正确，请输入11位有效手机号");
        }

        if (email != null && !email.isEmpty() && !validateEmail(email)) {
            errors.put("email", "邮箱格式不正确");
        }
    }

    private void validateOrganizationFields(Map<String, Object> data, Map<String, String> errors) {
        String orgName = getStringValue(data.get("orgName"));
        String orgCode = getStringValue(data.get("orgCode"));
        String contactPhone = getStringValue(data.get("contactPhone"));

        if (!validateRequired(orgName)) {
            errors.put("orgName", "机构名称不能为空");
        }

        if (!validateRequired(orgCode)) {
            errors.put("orgCode", "机构代码不能为空");
        } else if (!validateOrgCode(orgCode)) {
            errors.put("orgCode", "机构代码格式不正确，应为8位字符-1位校验位");
        }

        if (!validateRequired(contactPhone)) {
            errors.put("contactPhone", "联系电话不能为空");
        } else if (!validatePhone(contactPhone)) {
            errors.put("contactPhone", "联系电话格式不正确，请输入11位有效手机号");
        }
    }

    private String getStringValue(Object value) {
        return value == null ? null : value.toString().trim();
    }

    @Override
    public boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    @Override
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @Override
    public boolean validateOrgCode(String orgCode) {
        if (orgCode == null || orgCode.trim().isEmpty()) {
            return false;
        }
        return ORG_CODE_PATTERN.matcher(orgCode.trim()).matches();
    }

    @Override
    public boolean validateRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
