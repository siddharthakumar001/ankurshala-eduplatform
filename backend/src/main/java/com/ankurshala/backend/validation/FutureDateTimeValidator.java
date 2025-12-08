package com.ankurshala.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FutureDateTimeValidator implements ConstraintValidator<FutureDateTime, LocalDateTime> {
    
    private int bufferMinutes;
    
    @Override
    public void initialize(FutureDateTime constraintAnnotation) {
        this.bufferMinutes = constraintAnnotation.bufferMinutes();
    }
    
    @Override
    public boolean isValid(LocalDateTime dateTime, ConstraintValidatorContext context) {
        if (dateTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime minDateTime = now.plusMinutes(bufferMinutes);
        
        return dateTime.isAfter(minDateTime);
    }
}
