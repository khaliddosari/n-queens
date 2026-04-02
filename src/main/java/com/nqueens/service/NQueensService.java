package com.nqueens.service;

import com.nqueens.dto.AlgorithmResult;
import com.nqueens.dto.SolveRequest;
import com.nqueens.dto.SolveResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NQueensService {

    public SolveResponse solve(SolveRequest request) {
        int n = request.n();
        boolean useRandom = request.useRandomStart();
        Random random = useRandom ? new Random() : null;

        List<AlgorithmResult> results = new ArrayList<>();

        CSP btCsp = new CSP(n);
        if (useRandom) btCsp.initializeRandomState(random);
        PerformanceMetrics btMetrics = new PerformanceMetrics();
        btMetrics.start();
        boolean btSolved = backtracking(btCsp, btMetrics);
        btMetrics.stop();
        results.add(new AlgorithmResult("BACKTRACKING", btSolved,
                btMetrics.getTimeMs(), btMetrics.constraintChecks,
                btSolved ? toList(btCsp.assignment) : null));

        CSP fcCsp = new CSP(n);
        if (useRandom) fcCsp.initializeRandomState(random);
        PerformanceMetrics fcMetrics = new PerformanceMetrics();
        fcMetrics.start();
        boolean fcSolved = forwardChecking(fcCsp, fcMetrics);
        fcMetrics.stop();
        results.add(new AlgorithmResult("FORWARD_CHECKING", fcSolved,
                fcMetrics.getTimeMs(), fcMetrics.constraintChecks,
                fcSolved ? toList(fcCsp.assignment) : null));

        CSP macCsp = new CSP(n);
        if (useRandom) macCsp.initializeRandomState(random);
        PerformanceMetrics macMetrics = new PerformanceMetrics();
        macMetrics.start();
        boolean macSolved = mac(macCsp, macMetrics);
        macMetrics.stop();
        results.add(new AlgorithmResult("MAC", macSolved,
                macMetrics.getTimeMs(), macMetrics.constraintChecks,
                macSolved ? toList(macCsp.assignment) : null));

        return new SolveResponse(n, results);
    }

    private static List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>(arr.length);
        for (int v : arr) list.add(v);
        return list;
    }

    // -------------------------------------------------------------------------
    // Performance Metrics
    // -------------------------------------------------------------------------

    private static class PerformanceMetrics {
        long constraintChecks = 0;
        long startTime;
        long endTime;

        void start() { startTime = System.nanoTime(); }
        void stop()  { endTime   = System.nanoTime(); }
        long getTimeMs() { return (endTime - startTime) / 1_000_000; }
        void incrementChecks() { constraintChecks++; }
    }

    // -------------------------------------------------------------------------
    // CSP State
    // -------------------------------------------------------------------------

    private static class CSP {
        int n;
        int[] assignment;
        List<Set<Integer>> domains;
        int assignedCount;

        CSP(int n) {
            this.n = n;
            this.assignment = new int[n];
            Arrays.fill(assignment, -1);
            this.assignedCount = 0;
            this.domains = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Set<Integer> domain = new HashSet<>();
                for (int j = 0; j < n; j++) domain.add(j);
                domains.add(domain);
            }
        }

        CSP(CSP other) {
            this.n = other.n;
            this.assignment = other.assignment.clone();
            this.assignedCount = other.assignedCount;
            this.domains = new ArrayList<>();
            for (Set<Integer> domain : other.domains)
                this.domains.add(new HashSet<>(domain));
        }

        void initializeRandomState(Random random) {
            Arrays.fill(assignment, -1);
            assignedCount = 0;

            int toPlace = Math.max(1, n / 4);
            int attempts = 0;

            while (assignedCount < toPlace && attempts < n * 10) {
                int row = random.nextInt(n);
                int col = random.nextInt(n);
                if (assignment[row] == -1) {
                    boolean valid = true;
                    for (int r = 0; r < n; r++) {
                        if (assignment[r] != -1 && r != row) {
                            int c = assignment[r];
                            if (c == col || Math.abs(r - row) == Math.abs(c - col)) {
                                valid = false;
                                break;
                            }
                        }
                    }
                    if (valid) assign(row, col);
                }
                attempts++;
            }

            for (int row = 0; row < n; row++) {
                Set<Integer> domain = domains.get(row);
                domain.clear();
                if (assignment[row] != -1) {
                    domain.add(assignment[row]);
                } else {
                    for (int col = 0; col < n; col++) {
                        boolean valid = true;
                        for (int r = 0; r < n; r++) {
                            if (assignment[r] != -1 && r != row) {
                                int c = assignment[r];
                                if (c == col || Math.abs(r - row) == Math.abs(c - col)) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                        if (valid) domain.add(col);
                    }
                }
            }
        }

        boolean isComplete() { return assignedCount == n; }

        boolean isValid(int row, int col, PerformanceMetrics metrics) {
            for (int r = 0; r < n; r++) {
                if (assignment[r] == -1 || r == row) continue;
                metrics.incrementChecks();
                int c = assignment[r];
                if (c == col || Math.abs(r - row) == Math.abs(c - col)) return false;
            }
            return true;
        }

        void assign(int row, int col) {
            if (assignment[row] == -1) assignedCount++;
            assignment[row] = col;
        }

        void unassign(int row) {
            if (assignment[row] != -1) assignedCount--;
            assignment[row] = -1;
        }
    }

    // -------------------------------------------------------------------------
    // Algorithms
    // -------------------------------------------------------------------------

    private static boolean backtracking(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) return true;
        int row = selectUnassignedVariableMRV(csp);
        for (int col : orderDomainValues(csp, row)) {
            if (csp.isValid(row, col, metrics)) {
                csp.assign(row, col);
                if (backtracking(csp, metrics)) return true;
                csp.unassign(row);
            }
        }
        return false;
    }

    private static boolean forwardChecking(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) return true;
        int row = selectUnassignedVariableMRV(csp);
        for (int col : orderDomainValues(csp, row)) {
            if (csp.isValid(row, col, metrics)) {
                CSP copy = new CSP(csp);
                copy.assign(row, col);
                if (forwardCheck(copy, row, col, metrics) && forwardChecking(copy, metrics)) {
                    csp.assignment = copy.assignment;
                    csp.domains    = copy.domains;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean forwardCheck(CSP csp, int assignedRow, int assignedCol, PerformanceMetrics metrics) {
        for (int r = 0; r < csp.n; r++) {
            if (csp.assignment[r] != -1 || r == assignedRow) continue;
            Set<Integer> domain = csp.domains.get(r);
            int rowDist = Math.abs(r - assignedRow);
            Iterator<Integer> it = domain.iterator();
            while (it.hasNext()) {
                int c = it.next();
                metrics.incrementChecks();
                if (c == assignedCol || rowDist == Math.abs(c - assignedCol)) it.remove();
            }
            if (domain.isEmpty()) return false;
        }
        return true;
    }

    private static boolean mac(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) return true;
        int row = selectUnassignedVariableMRV(csp);
        for (int col : orderDomainValues(csp, row)) {
            if (csp.isValid(row, col, metrics)) {
                CSP copy = new CSP(csp);
                copy.assign(row, col);
                Queue<int[]> queue = new LinkedList<>();
                for (int r = 0; r < copy.n; r++) {
                    if (copy.assignment[r] == -1 && r != row)
                        queue.add(new int[]{r, row});
                }
                if (ac3(copy, queue, metrics) && mac(copy, metrics)) {
                    csp.assignment = copy.assignment;
                    csp.domains    = copy.domains;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean ac3(CSP csp, Queue<int[]> queue, PerformanceMetrics metrics) {
        while (!queue.isEmpty()) {
            int[] arc = queue.poll();
            if (revise(csp, arc[0], arc[1], metrics)) {
                if (csp.domains.get(arc[0]).isEmpty()) return false;
                for (int r = 0; r < csp.n; r++) {
                    if (csp.assignment[r] == -1 && r != arc[0] && r != arc[1])
                        queue.add(new int[]{r, arc[0]});
                }
            }
        }
        return true;
    }

    private static boolean revise(CSP csp, int row1, int row2, PerformanceMetrics metrics) {
        boolean revised = false;
        int rowDist = Math.abs(row1 - row2);
        Iterator<Integer> it = csp.domains.get(row1).iterator();
        while (it.hasNext()) {
            int col1 = it.next();
            boolean hasSupport = false;
            for (int col2 : csp.domains.get(row2)) {
                metrics.incrementChecks();
                if (col1 != col2 && rowDist != Math.abs(col1 - col2)) {
                    hasSupport = true;
                    break;
                }
            }
            if (!hasSupport) { it.remove(); revised = true; }
        }
        return revised;
    }

    // -------------------------------------------------------------------------
    // Heuristics
    // -------------------------------------------------------------------------

    private static int selectUnassignedVariableMRV(CSP csp) {
        int minSize = Integer.MAX_VALUE;
        int selected = -1;
        for (int r = 0; r < csp.n; r++) {
            if (csp.assignment[r] == -1) {
                int size = csp.domains.get(r).size();
                if (size < minSize ||
                    (size == minSize && getDegree(csp, r) > getDegree(csp, selected))) {
                    minSize  = size;
                    selected = r;
                }
            }
        }
        if (selected == -1)
            throw new IllegalStateException("selectUnassignedVariableMRV called on a complete assignment");
        return selected;
    }

    private static int getDegree(CSP csp, int row) {
        int unassigned = csp.n - csp.assignedCount;
        return (csp.assignment[row] == -1) ? unassigned - 1 : unassigned;
    }

    private static List<Integer> orderDomainValues(CSP csp, int row) {
        List<Integer> domain = new ArrayList<>(csp.domains.get(row));
        domain.sort((a, b) -> Integer.compare(countEliminations(csp, row, a), countEliminations(csp, row, b)));
        return domain;
    }

    private static int countEliminations(CSP csp, int row, int col) {
        int count = 0;
        for (int r = 0; r < csp.n; r++) {
            if (csp.assignment[r] == -1 && r != row) {
                int rowDist = Math.abs(r - row);
                for (int c : csp.domains.get(r)) {
                    if (c == col || rowDist == Math.abs(c - col)) count++;
                }
            }
        }
        return count;
    }
}
