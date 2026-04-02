package com.nqueens.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SolveRequest(
    @Min(1) @Max(64) int n,
    boolean useRandomStart
) {}
