package com.nqueens.dto;

import java.util.List;

public record SolveResponse(
    int n,
    List<AlgorithmResult> results
) {}
