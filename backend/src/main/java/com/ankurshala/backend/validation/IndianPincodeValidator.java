package com.ankurshala.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IndianPincodeValidator implements ConstraintValidator<IndianPincode, String> {
    
    private static final String INDIAN_PINCODE_PATTERN = "^[1-9]{1}[0-9]{5}$";
    
    @Override
    public void initialize(IndianPincode constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String pincode, ConstraintValidatorContext context) {
        if (pincode == null || pincode.trim().isEmpty()) {
            return false;
        }
        
        // Remove any spaces
        String cleanPincode = pincode.trim();
        
        // Check if it's a 6-digit number starting with 1-9
        return cleanPincode.matches(INDIAN_PINCODE_PATTERN);
    }
}
