# N-Queens CSP Solver

Solves the N-Queens problem using three algorithms: **Backtracking (BT)**, **Forward Checking (FC)**, and **Maintaining Arc-Consistency (MAC)**, with MRV, Degree, and LCV heuristics.

## Run (current CLI version)

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

---

## Planned: Spring Boot + Maven Backend

### Project Structure

```
n-queens/
├── pom.xml
└── src/main/
    ├── java/com/nqueens/
    │   ├── NQueensApplication.java       ← Spring Boot entry point
    │   ├── controller/
    │   │   └── NQueensController.java    ← REST endpoint
    │   ├── service/
    │   │   └── NQueensService.java       ← all solver logic
    │   └── dto/
    │       ├── SolveRequest.java
    │       ├── AlgorithmResult.java
    │       └── SolveResponse.java
    └── resources/
        └── application.properties
```

### API

**`POST /api/solve`**

Request:
```json
{ "n": 8, "useRandomStart": false }
```

Response:
```json
{
  "n": 8,
  "results": [
    {
      "algorithm": "BACKTRACKING",
      "solved": true,
      "timeMs": 3,
      "constraintChecks": 876,
      "queenColumns": [0, 4, 7, 5, 2, 6, 1, 3]
    },
    {
      "algorithm": "FORWARD_CHECKING",
      "solved": true,
      "timeMs": 1,
      "constraintChecks": 112,
      "queenColumns": [0, 4, 7, 5, 2, 6, 1, 3]
    },
    {
      "algorithm": "MAC",
      "solved": true,
      "timeMs": 2,
      "constraintChecks": 89,
      "queenColumns": [0, 4, 7, 5, 2, 6, 1, 3]
    }
  ]
}
```

`queenColumns[i]` = column (0-indexed) of the queen in row `i`. Returns `null` if unsolved. HTTP 400 if `n` is outside 1–64.

`queenColumns` is `null` if unsolved. HTTP 422 with structured error body if `n` is outside 1–64:
```json
{ "error": "n must be between 1 and 64", "field": "n", "rejected": 0 }
```

### Stack
- Java 17
- Spring Boot 3.2.x
- Maven
- `spring-boot-starter-web` (Jackson + Tomcat)
- `spring-boot-starter-validation` (Bean Validation for `SolveRequest`)
- CORS enabled for local frontend dev
