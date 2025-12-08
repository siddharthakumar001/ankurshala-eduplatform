package com.ankurshala.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBookingDurationValidator implements ConstraintValidator<ValidBookingDuration, Integer> {
    
    private int minMinutes;
    private int maxMinutes;
    
    @Override
    public void initialize(ValidBookingDuration constraintAnnotation) {
        this.minMinutes = constraintAnnotation.minMinutes();
        this.maxMinutes = constraintAnnotation.maxMinutes();
    }
    
    @Override
    public boolean isValid(Integer durationMinutes, ConstraintValidatorContext context) {
        if (durationMinutes == null) {
            return false;
        }
        
        return durationMinutes >= minMinutes && durationMinutes <= maxMinutes;
    }
}
