package com.nqueens.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SolveRequestValidator implements ConstraintValidator<ValidSolveRequest, SolveRequest> {

    @Override
    public boolean isValid(SolveRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;
        int max = req.useRandomStart() ? 32 : 20;
        if (req.n() > max) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                "N must be at most " + max + (req.useRandomStart() ? "" : " when Random Start is off")
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
