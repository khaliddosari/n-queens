# N-Queens CSP Solver

Solves the N-Queens problem using three algorithms: **Backtracking (BT)**, **Forward Checking (FC)**, and **Maintaining Arc-Consistency (MAC)**, with MRV, Degree, and LCV heuristics.

## Run

```bash
# Compile
javac "New folder/AiProject.java"

# Interactive
java -cp "New folder" AiProject

# Benchmark (N = 4, 8, 16, 22, 32)
./run_tests.sh        # Linux/macOS
run_tests.bat         # Windows
```

Results saved to `test_results.txt`. Accepts N from 1 to 64.
