package com.ankurshala.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IndianMobileNumberValidator implements ConstraintValidator<IndianMobileNumber, String> {
    
    private static final String INDIAN_MOBILE_PATTERN = "^[6-9]\\d{9}$";
    
    @Override
    public void initialize(IndianMobileNumber constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String mobileNumber, ConstraintValidatorContext context) {
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove any spaces or special characters
        String cleanNumber = mobileNumber.replaceAll("[\\s\\-()]", "");
        
        // Check if it's a 10-digit number starting with 6, 7, 8, or 9
        return cleanNumber.matches(INDIAN_MOBILE_PATTERN);
    }
}
