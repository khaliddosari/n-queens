# N-Queens CSP Solver

Spring Boot web app that solves the N-Queens problem using three CSP algorithms — Backtracking, Forward Checking, and MAC — enhanced with MRV, Degree, and LCV heuristics. Includes a built-in interactive frontend for visualizing and comparing results.

## Stack

- Java 17, Spring Boot 3.2.4, Maven
- Vanilla HTML/CSS/JS frontend (served as static resources)
- Deployed to Railway via GitHub Actions

## Run Locally

```bash
mvn spring-boot:run
```

Opens at `http://localhost:8080`.

## API

**`POST /api/solve`**

```json
{ "n": 8, "useRandomStart": false }
```

Returns each algorithm's solution, time, and constraint checks. `queenColumns[i]` = column of the queen in row `i`. Returns HTTP 422 if `n` is outside 1-64.

## Project Structure

```
src/main/java/com/nqueens/
  NQueensApplication.java
  controller/   NQueensController, GlobalExceptionHandler
  service/      NQueensService (BT, FC, MAC + heuristics)
  dto/          SolveRequest, SolveResponse, AlgorithmResult
  filter/       RateLimitFilter
src/main/resources/static/
  index.html, style.css, script.js
.github/workflows/
  deploy.yml
```
