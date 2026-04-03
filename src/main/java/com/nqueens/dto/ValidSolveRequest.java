package com.nqueens.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SolveRequestValidator.class)
public @interface ValidSolveRequest {
    String message() default "Invalid request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
