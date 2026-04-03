package com.nqueens.dto;

import jakarta.validation.constraints.Min;

@ValidSolveRequest
public record SolveRequest(
    @Min(1) int n,
    boolean useRandomStart
) {}
