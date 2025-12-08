package com.ankurshala.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FutureDateTimeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDateTime {
    String message() default "Date must be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int bufferMinutes() default 0; // Minimum buffer in minutes
}
