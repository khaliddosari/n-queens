import java.util.*;

public class AiProject {

    private static final long TIME_LIMIT_MS = 20L * 60L * 1000L;
    
    static class PerformanceMetrics {
        long constraintChecks = 0;
        long startTime;
        long endTime;
        
        void start() {
            startTime = System.nanoTime();
        }
        
        void stop() {
            endTime = System.nanoTime();
        }
        
        long getTimeMs() {
            return (endTime - startTime) / 1_000_000;
        }
        
        void incrementChecks() {
            constraintChecks++;
        }
    }
    
    static class CSP {
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
            for (int rowIndex = 0; rowIndex < n; rowIndex++) {
                Set<Integer> domain = new HashSet<>();
                for (int columnIndex = 0; columnIndex < n; columnIndex++) {
                    domain.add(columnIndex);
                }
                domains.add(domain);
            }
        }
        
        CSP(CSP other) {
            this.n = other.n;
            this.assignment = other.assignment.clone();
            this.assignedCount = other.assignedCount;
            this.domains = new ArrayList<>();
            for (Set<Integer> domain : other.domains) {
                this.domains.add(new HashSet<>(domain));
            }
        }
        
        void initializeRandomState(Random random) {
            Arrays.fill(assignment, -1);
            assignedCount = 0;
            
            int numberOfQueensToPlace = Math.max(1, n / 4);
            int attempts = 0;
            int maxAttempts = n * 10;
            
            while (assignedCount < numberOfQueensToPlace && attempts < maxAttempts) {
                int randomRow = random.nextInt(n);
                int randomCol = random.nextInt(n);
                
                if (assignment[randomRow] == -1) {
                    boolean isValidPosition = true;
                    for (int otherRow = 0; otherRow < n; otherRow++) {
                        if (assignment[otherRow] != -1 && otherRow != randomRow) {
                            int otherCol = assignment[otherRow];
                            if (otherCol == randomCol) {
                                isValidPosition = false;
                                break;
                            }
                            int rowDistance = Math.abs(otherRow - randomRow);
                            int colDistance = Math.abs(otherCol - randomCol);
                            if (rowDistance == colDistance) {
                                isValidPosition = false;
                                break;
                            }
                        }
                    }
                    
                    if (isValidPosition) {
                        assign(randomRow, randomCol);
                    }
                }
                attempts++;
            }
            
            for (int rowIndex = 0; rowIndex < n; rowIndex++) {
                if (assignment[rowIndex] != -1) {
                    Set<Integer> domain = domains.get(rowIndex);
                    domain.clear();
                    domain.add(assignment[rowIndex]);
                } else {
                    Set<Integer> domain = domains.get(rowIndex);
                    domain.clear();
                    for (int colIndex = 0; colIndex < n; colIndex++) {
                        boolean isValidCol = true;
                        for (int otherRow = 0; otherRow < n; otherRow++) {
                            if (assignment[otherRow] != -1 && otherRow != rowIndex) {
                                int otherCol = assignment[otherRow];
                                if (otherCol == colIndex) {
                                    isValidCol = false;
                                    break;
                                }
                                int rowDistance = Math.abs(otherRow - rowIndex);
                                int colDistance = Math.abs(otherCol - colIndex);
                                if (rowDistance == colDistance) {
                                    isValidCol = false;
                                    break;
                                }
                            }
                        }
                        if (isValidCol) {
                            domain.add(colIndex);
                        }
                    }
                }
            }
        }
        
        boolean isComplete() {
            return assignedCount == n;
        }
        
        boolean isValid(int row, int col, PerformanceMetrics metrics) {
            for (int otherRow = 0; otherRow < n; otherRow++) {
                if (assignment[otherRow] == -1 || otherRow == row) {
                    continue;
                }
                
                metrics.incrementChecks();
                int existingQueenColumn = assignment[otherRow];
                
                if (existingQueenColumn == col) {
                    return false;
                }
                
                int rowDistance = Math.abs(otherRow - row);
                int columnDistance = Math.abs(existingQueenColumn - col);
                if (rowDistance == columnDistance) {
                    return false;
                }
            }
            return true;
        }
        
        void assign(int row, int col) {
            if (assignment[row] == -1) {
                assignedCount++;
            }
            assignment[row] = col;
        }
        
        void unassign(int row) {
            if (assignment[row] != -1) {
                assignedCount--;
            }
            assignment[row] = -1;
        }
    }
    
    public static boolean backtracking(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) {
            return true;
        }
        
        int row = selectUnassignedVariableMRV(csp);
        List<Integer> values = orderDomainValues(csp, row);
        
        for (int columnPosition : values) {
            if (csp.isValid(row, columnPosition, metrics)) {
                csp.assign(row, columnPosition);
                if (backtracking(csp, metrics)) {
                    return true;
                }
                csp.unassign(row);
            }
        }
        return false;
    }
    
    public static boolean forwardChecking(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) {
            return true;
        }
        
        int row = selectUnassignedVariableMRV(csp);
        List<Integer> possibleColumns = orderDomainValues(csp, row);
        
        for (int columnPosition : possibleColumns) {
            if (csp.isValid(row, columnPosition, metrics)) {
                CSP copiedCsp = new CSP(csp);
                copiedCsp.assign(row, columnPosition);
                
                if (forwardCheck(copiedCsp, row, columnPosition, metrics)) {
                    if (forwardChecking(copiedCsp, metrics)) {
                        csp.assignment = copiedCsp.assignment;
                        csp.domains = copiedCsp.domains;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean forwardCheck(CSP csp, int assignedRow, int assignedColumn, PerformanceMetrics metrics) {
        for (int otherRow = 0; otherRow < csp.n; otherRow++) {
            if (csp.assignment[otherRow] != -1 || otherRow == assignedRow) {
                continue;
            }
            
            Set<Integer> domain = csp.domains.get(otherRow);
            int rowDistance = Math.abs(otherRow - assignedRow);
            
            Iterator<Integer> iterator = domain.iterator();
            while (iterator.hasNext()) {
                int columnValue = iterator.next();
                metrics.incrementChecks();
                
                if (columnValue == assignedColumn || rowDistance == Math.abs(columnValue - assignedColumn)) {
                    iterator.remove();
                }
            }
            
            if (domain.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean mac(CSP csp, PerformanceMetrics metrics) {
        if (csp.isComplete()) {
            return true;
        }
        
        int row = selectUnassignedVariableMRV(csp);
        List<Integer> possibleColumns = orderDomainValues(csp, row);
        
        for (int columnPosition : possibleColumns) {
            if (csp.isValid(row, columnPosition, metrics)) {
                CSP copiedCsp = new CSP(csp);
                copiedCsp.assign(row, columnPosition);
                
                Queue<int[]> arcQueue = new LinkedList<>();
                for (int otherRow = 0; otherRow < copiedCsp.n; otherRow++) {
                    if (copiedCsp.assignment[otherRow] == -1 && otherRow != row) {
                        arcQueue.add(new int[]{otherRow, row});
                    }
                }
                
                if (ac3(copiedCsp, arcQueue, metrics)) {
                    if (mac(copiedCsp, metrics)) {
                        csp.assignment = copiedCsp.assignment;
                        csp.domains = copiedCsp.domains;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean ac3(CSP csp, Queue<int[]> arcQueue, PerformanceMetrics metrics) {
        while (!arcQueue.isEmpty()) {
            int[] arcPair = arcQueue.poll();
            int firstRow = arcPair[0];
            int secondRow = arcPair[1];
            
            if (revise(csp, firstRow, secondRow, metrics)) {
                if (csp.domains.get(firstRow).isEmpty()) {
                    return false;
                }
                
                for (int neighborRow = 0; neighborRow < csp.n; neighborRow++) {
                    if (csp.assignment[neighborRow] == -1 && neighborRow != firstRow && neighborRow != secondRow) {
                        arcQueue.add(new int[]{neighborRow, firstRow});
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean revise(CSP csp, int firstRow, int secondRow, PerformanceMetrics metrics) {
        boolean revised = false;
        Set<Integer> firstRowDomain = csp.domains.get(firstRow);
        Set<Integer> secondRowDomain = csp.domains.get(secondRow);
        int rowDistance = Math.abs(firstRow - secondRow);
        
        Iterator<Integer> iterator = firstRowDomain.iterator();
        while (iterator.hasNext()) {
            int firstRowColumn = iterator.next();
            boolean hasSupport = false;
            
            for (int secondRowColumn : secondRowDomain) {
                metrics.incrementChecks();
                if (firstRowColumn != secondRowColumn && rowDistance != Math.abs(firstRowColumn - secondRowColumn)) {
                    hasSupport = true;
                    break;
                }
            }
            
            if (!hasSupport) {
                iterator.remove();
                revised = true;
            }
        }
        
        return revised;
    }
    
    private static int selectUnassignedVariableMRV(CSP csp) {
        int smallestDomainSize = Integer.MAX_VALUE;
        int selectedRow = -1;
        
        for (int rowIndex = 0; rowIndex < csp.n; rowIndex++) {
            if (csp.assignment[rowIndex] == -1) {
                int numberOfOptions = csp.domains.get(rowIndex).size();
                if (numberOfOptions < smallestDomainSize) {
                    smallestDomainSize = numberOfOptions;
                    selectedRow = rowIndex;
                } else if (numberOfOptions == smallestDomainSize && selectedRow != -1) {
                    int constraintsForCurrentRow = getDegree(csp, rowIndex);
                    int constraintsForSelectedRow = getDegree(csp, selectedRow);
                    if (constraintsForCurrentRow > constraintsForSelectedRow) {
                        selectedRow = rowIndex;
                    }
                }
            }
        }
        if (selectedRow == -1) {
            throw new IllegalStateException("selectUnassignedVariableMRV called on a complete assignment");
        }
        return selectedRow;
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
    
    private static int countEliminations(CSP csp, int row, int column) {
        int conflictCount = 0;
        for (int otherRow = 0; otherRow < csp.n; otherRow++) {
            if (csp.assignment[otherRow] == -1 && otherRow != row) {
                Set<Integer> domain = csp.domains.get(otherRow);
                int rowDistance = Math.abs(otherRow - row);
                
                for (int otherColumn : domain) {
                    if (otherColumn == column || rowDistance == Math.abs(otherColumn - column)) {
                        conflictCount++;
                    }
                }
            }
        }
        return conflictCount;
    }
    
    private static void printSolution(CSP csp) {
        System.out.println("\nSolution:");
        for (int rowIndex = 0; rowIndex < csp.n; rowIndex++) {
            for (int columnIndex = 0; columnIndex < csp.n; columnIndex++) {
                if (csp.assignment[rowIndex] == columnIndex) {
                    System.out.print("Q ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private static void printSummary(boolean solved1, PerformanceMetrics metrics1,
                                     boolean solved2, PerformanceMetrics metrics2,
                                     boolean solved3, PerformanceMetrics metrics3) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Performance Summary:");
        System.out.println("=".repeat(80));
        System.out.printf("%-25s %20s %28s%n", "Algorithm", "Time (ms)", "Constraint Checks");
        System.out.println("-".repeat(80));
        if (solved1) {
            System.out.printf("%-25s %20d %28d%n", "Backtracking (BT)", 
                metrics1.getTimeMs(), metrics1.constraintChecks);
        }
        if (solved2) {
            System.out.printf("%-25s %20d %28d%n", "Forward Checking (FC)", 
                metrics2.getTimeMs(), metrics2.constraintChecks);
        }
        if (solved3) {
            System.out.printf("%-25s %12d %28d%n", "Maintaining Arc-Consistency (MAC)", 
                metrics3.getTimeMs(), metrics3.constraintChecks);
        }
    }
    
    private static void startTimeLimitWatcher() {
        Thread timeLimitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIME_LIMIT_MS);
                    System.out.println("\nTime limit exceeded: 20 minutes. Exiting.");
                    System.out.flush();
                    System.exit(0);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        timeLimitThread.setDaemon(true);
        timeLimitThread.start();
    }
    
    private static void runAutomatedTests() {
        System.out.println("=== Automated Performance Testing ===\n");
        System.out.println("Running tests on multiple problem sizes...\n");
        
        int[] testSizes = {4, 8, 16, 22, 32};
        boolean useRandomStart = false;
        Random random = new Random();
        
        System.out.println("Results Table:");
        System.out.println("=".repeat(100));
        System.out.printf("%-6s %-20s %-15s %-20s %-15s%n", "N", "Algorithm", "Time (ms)", "Constraint Checks", "Status");
        System.out.println("-".repeat(100));
        
        for (int n : testSizes) {
            System.out.println("\nTesting N = " + n + "...");
            
            CSP backtrackingCsp = new CSP(n);
            if (useRandomStart) {
                backtrackingCsp.initializeRandomState(random);
            }
            PerformanceMetrics backtrackingMetrics = new PerformanceMetrics();
            backtrackingMetrics.start();
            boolean backtrackingSolved = backtracking(backtrackingCsp, backtrackingMetrics);
            backtrackingMetrics.stop();

            System.out.printf("%-6d %-20s %-15d %-20d %-15s%n",
                n, "Backtracking (BT)",
                backtrackingMetrics.getTimeMs(),
                backtrackingMetrics.constraintChecks,
                backtrackingSolved ? "SOLVED" : "FAILED");

            CSP forwardCheckingCsp = new CSP(n);
            if (useRandomStart) {
                forwardCheckingCsp.initializeRandomState(random);
            }
            PerformanceMetrics forwardCheckingMetrics = new PerformanceMetrics();
            forwardCheckingMetrics.start();
            boolean forwardCheckingSolved = forwardChecking(forwardCheckingCsp, forwardCheckingMetrics);
            forwardCheckingMetrics.stop();

            System.out.printf("%-6d %-20s %-15d %-20d %-15s%n",
                n, "Forward Checking (FC)",
                forwardCheckingMetrics.getTimeMs(),
                forwardCheckingMetrics.constraintChecks,
                forwardCheckingSolved ? "SOLVED" : "FAILED");

            CSP macCsp = new CSP(n);
            if (useRandomStart) {
                macCsp.initializeRandomState(random);
            }
            PerformanceMetrics macMetrics = new PerformanceMetrics();
            macMetrics.start();
            boolean macSolved = mac(macCsp, macMetrics);
            macMetrics.stop();
            
            System.out.printf("%-6d %-20s %-15d %-20d %-15s%n", 
                n, "MAC", 
                macMetrics.getTimeMs(), 
                macMetrics.constraintChecks,
                macSolved ? "SOLVED" : "FAILED");
        }
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("\nTesting complete!");
    }
    
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            startTimeLimitWatcher();
            runAutomatedTests();
            return;
        }
        
        startTimeLimitWatcher();

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== N-Queens CSP Solver ===");
        int n;
        
        while (true) {
            System.out.print("Enter the number of queens (N, 1-64): ");
            if (scanner.hasNextInt()) {
                n = scanner.nextInt();
                if (n >= 1 && n <= 64) {
                    break;
                } else {
                    System.out.println("Invalid input. N must be between 1 and 64 (inclusive).");
                    System.out.println();
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer.");
                System.out.println();
                scanner.next();
            }
        }
        
        scanner.nextLine();
        
        System.out.println("\nSolving " + n + "-Queens problem...\n");
        
        System.out.print("Use random start state? (y/n): ");
        String useRandom = scanner.nextLine().trim().toLowerCase();
        boolean useRandomStart = useRandom.equals("y") || useRandom.equals("yes");
        scanner.close();
        
        Random random = null;
        if (useRandomStart) {
            random = new Random();
            System.out.println("Using random start state...\n");
        }
        
        int[] comparisonSizes = {4, 8, 16, 32, 64};
        boolean shouldCompareAlgorithms = false;
        
        for (int size : comparisonSizes) {
            if (n == size) {
                shouldCompareAlgorithms = true;
                break;
            }
        }
        
        if (shouldCompareAlgorithms) {
            System.out.println("=".repeat(80));
            System.out.println("Algorithm Comparison for N = " + n);
            System.out.println("=".repeat(80));
            
            CSP backtrackingCsp = new CSP(n);
            if (useRandomStart) {
                backtrackingCsp.initializeRandomState(random);
            }
            PerformanceMetrics backtrackingMetrics = new PerformanceMetrics();
            backtrackingMetrics.start();
            boolean backtrackingSolved = backtracking(backtrackingCsp, backtrackingMetrics);
            backtrackingMetrics.stop();
            
            System.out.println("\n1. Standard Backtracking (BT):");
            if (backtrackingSolved) {
                System.out.println("   Status: SOLVED");
                System.out.println("   Time: " + backtrackingMetrics.getTimeMs() + " ms");
                System.out.println("   Constraint Checks: " + backtrackingMetrics.constraintChecks);
                if (n <= 32) printSolution(backtrackingCsp);
            } else {
                System.out.println("   Status: NO SOLUTION FOUND");
                System.out.println();

            }
            
            CSP forwardCheckingCsp = new CSP(n);
            if (useRandomStart) {
                forwardCheckingCsp.initializeRandomState(random);
            }
            PerformanceMetrics forwardCheckingMetrics = new PerformanceMetrics();
            forwardCheckingMetrics.start();
            boolean forwardCheckingSolved = forwardChecking(forwardCheckingCsp, forwardCheckingMetrics);
            forwardCheckingMetrics.stop();
            
            System.out.println("\n2. Forward Checking (FC):");
            if (forwardCheckingSolved) {
                System.out.println("   Status: SOLVED");
                System.out.println("   Time: " + forwardCheckingMetrics.getTimeMs() + " ms");
                System.out.println("   Constraint Checks: " + forwardCheckingMetrics.constraintChecks);
                if (n <= 32) printSolution(forwardCheckingCsp);
            } else {
                System.out.println("   Status: NO SOLUTION FOUND");
                System.out.println();

            }
            
            CSP macCsp = new CSP(n);
            if (useRandomStart) {
                macCsp.initializeRandomState(random);
            }
            PerformanceMetrics macMetrics = new PerformanceMetrics();
            macMetrics.start();
            boolean macSolved = mac(macCsp, macMetrics);
            macMetrics.stop();
            
            System.out.println("\n3. Maintaining Arc-Consistency (MAC):");
            if (macSolved) {
                System.out.println("   Status: SOLVED");
                System.out.println("   Time: " + macMetrics.getTimeMs() + " ms");
                System.out.println("   Constraint Checks: " + macMetrics.constraintChecks);
                if (n <= 32) printSolution(macCsp);
            } else {
                System.out.println("   Status: NO SOLUTION FOUND");
                System.out.println();

            }
            
            printSummary(backtrackingSolved, backtrackingMetrics, forwardCheckingSolved, forwardCheckingMetrics, macSolved, macMetrics);
            
        } else {
            System.out.println("Running all three algorithms...\n");
            
            CSP backtrackingCsp = new CSP(n);
            if (useRandomStart) {
                backtrackingCsp.initializeRandomState(random);
            }
            PerformanceMetrics backtrackingMetrics = new PerformanceMetrics();
            backtrackingMetrics.start();
            boolean backtrackingSolved = backtracking(backtrackingCsp, backtrackingMetrics);
            backtrackingMetrics.stop();
            
            System.out.println("\nBacktracking (BT):");
            if (backtrackingSolved) {
                System.out.println("  Solved in " + backtrackingMetrics.getTimeMs() + " ms");
                System.out.println("  Constraint checks: " + backtrackingMetrics.constraintChecks);
                if (n <= 32) printSolution(backtrackingCsp);
            } else {
                System.out.println("  No solution found");
                System.out.println();

            }
            
            CSP forwardCheckingCsp = new CSP(n);
            if (useRandomStart) {
                forwardCheckingCsp.initializeRandomState(random);
            }
            PerformanceMetrics forwardCheckingMetrics = new PerformanceMetrics();
            forwardCheckingMetrics.start();
            boolean forwardCheckingSolved = forwardChecking(forwardCheckingCsp, forwardCheckingMetrics);
            forwardCheckingMetrics.stop();
            
            System.out.println("\nForward Checking (FC):");
            if (forwardCheckingSolved) {
                System.out.println("  Solved in " + forwardCheckingMetrics.getTimeMs() + " ms");
                System.out.println("  Constraint checks: " + forwardCheckingMetrics.constraintChecks);
                if (n <= 32) printSolution(forwardCheckingCsp);
            } else {
                System.out.println("  No solution found");
                System.out.println();
            }

            CSP macCsp = new CSP(n);
            if (useRandomStart) {
                macCsp.initializeRandomState(random);
            }
            PerformanceMetrics macMetrics = new PerformanceMetrics();
            macMetrics.start();
            boolean macSolved = mac(macCsp, macMetrics);
            macMetrics.stop();
            
            System.out.println("\nMaintaining Arc-Consistency (MAC):");
            if (macSolved) {
                System.out.println("  Solved in " + macMetrics.getTimeMs() + " ms");
                System.out.println("  Constraint checks: " + macMetrics.constraintChecks);
                if (n <= 32) printSolution(macCsp);
            } else {
                System.out.println("  No solution found");
                System.out.println();
            }
            
            printSummary(backtrackingSolved, backtrackingMetrics, forwardCheckingSolved, forwardCheckingMetrics, macSolved, macMetrics);
        }
    }
}
