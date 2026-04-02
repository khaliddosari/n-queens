# N-Queens CSP Solver

Spring Boot REST API that solves the N-Queens problem using three CSP algorithms: **Backtracking (BT)**, **Forward Checking (FC)**, and **Maintaining Arc-Consistency (MAC)**, with MRV, Degree, and LCV heuristics.

## Stack

- Java 17
- Spring Boot 3.2.4
- Maven

## Run

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`.

## API

### `POST /api/solve`

**Request:**
```json
{ "n": 8, "useRandomStart": false }
```

**Response:**
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

`queenColumns[i]` = column of the queen in row `i` (0-indexed). `null` if unsolved.

Returns **HTTP 422** with a structured error body if `n` is outside 1–64.

## Project Structure

```
src/main/java/com/nqueens/
├── NQueensApplication.java
├── controller/
│   ├── NQueensController.java
│   └── GlobalExceptionHandler.java
├── service/
│   └── NQueensService.java
└── dto/
    ├── SolveRequest.java
    ├── AlgorithmResult.java
    └── SolveResponse.java
```
