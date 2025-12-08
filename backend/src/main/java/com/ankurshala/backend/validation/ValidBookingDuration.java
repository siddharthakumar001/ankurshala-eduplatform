package com.ankurshala.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidBookingDurationValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingDuration {
    String message() default "Invalid booking duration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int minMinutes() default 30;
    int maxMinutes() default 180;
}
