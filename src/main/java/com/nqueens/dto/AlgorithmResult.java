package com.nqueens.dto;

import java.util.List;

public record AlgorithmResult(
    String algorithm,
    boolean solved,
    long timeMs,
    long constraintChecks,
    List<Integer> queenColumns
) {}
