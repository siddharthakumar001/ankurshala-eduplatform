package com.ankurshala.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IndianMobileNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IndianMobileNumber {
    String message() default "Invalid Indian mobile number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
