package com.nqueens.controller;

import com.nqueens.dto.SolveRequest;
import com.nqueens.dto.SolveResponse;
import com.nqueens.service.NQueensService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NQueensController {

    private final NQueensService nQueensService;

    public NQueensController(NQueensService nQueensService) {
        this.nQueensService = nQueensService;
    }

    @PostMapping("/solve")
    public ResponseEntity<SolveResponse> solve(@Valid @RequestBody SolveRequest request) {
        return ResponseEntity.ok(nQueensService.solve(request));
    }
}
