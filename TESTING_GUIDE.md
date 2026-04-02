# Testing Guide - Collecting Experimental Results

This guide will help you collect actual experimental results for your report.

## Method 1: Automated Testing (Recommended)

### For Windows:
1. Double-click `run_tests.bat` OR
2. Open Command Prompt in the project folder and run:
   ```
   javac AiProject.java
   java -cp . AiProject --test > test_results.txt
   ```

### For Linux/Mac:
1. Make the script executable:
   ```
   chmod +x run_tests.sh
   ./run_tests.sh
   ```
   OR
2. Run manually:
   ```
   javac AiProject.java
   java -cp . AiProject --test > test_results.txt
   ```

The automated test will:
- Run all three algorithms (BT, FC, MAC) on N = 4, 8, 16, 22, 32
- Output results in a formatted table
- Save results to `test_results.txt`

**Note:** For N=32 and larger, tests may take several minutes. Be patient!

## Method 2: Manual Testing

If you want to test specific values or see solutions:

1. Compile the program:
   ```
   javac AiProject.java
   ```

2. Run the program:
   ```
   java AiProject
   ```

3. When prompted:
   - Enter the number of queens (N): e.g., `4`, `8`, `16`, `22`, `32`
   - Choose random start state: `n` (recommended for consistent results)

4. Record the results from the output:
   - Time (ms)
   - Constraint Checks
   - Status (SOLVED/FAILED)

5. Repeat for different N values

## Collecting Results

### Results Table Format

Create a table like this in your report:

| N  | Algorithm | Time (ms) | Constraint Checks | Status |
|----|-----------|-----------|-------------------|--------|
| 4  | BT        | [value]   | [value]           | SOLVED |
| 4  | FC        | [value]   | [value]           | SOLVED |
| 4  | MAC       | [value]   | [value]           | SOLVED |
| 8  | BT        | [value]   | [value]           | SOLVED |
| ... | ...      | ...       | ...               | ...    |

### Tips for Accurate Results

1. **Run multiple times**: For each N value, run 3-5 times and take the average
2. **Close other programs**: Ensure your computer isn't running heavy processes
3. **Use consistent settings**: Always use `n` for random start state (or always `y`) for fair comparison
4. **Note your system**: Include your system specs (CPU, RAM) in the report

### Expected Results Range

Based on typical performance:

- **N = 4**: All algorithms should complete in < 1ms
- **N = 8**: BT: 1-10ms, FC: < 1ms, MAC: < 1ms
- **N = 16**: BT: 50-500ms, FC: 10-100ms, MAC: 5-50ms
- **N = 22**: BT: 500-5000ms, FC: 100-1000ms, MAC: 50-500ms
- **N = 32**: BT: 5000-60000ms, FC: 1000-10000ms, MAC: 500-5000ms

*Actual results will vary based on your system performance.*

## Creating Graphs

### Option 1: Excel/Google Sheets
1. Copy your results table
2. Create a line chart with:
   - X-axis: N (problem size)
   - Y-axis: Time (ms) or Constraint Checks (log scale recommended)
   - Series: One line for each algorithm (BT, FC, MAC)

### Option 2: Python (if you have it)
```python
import matplotlib.pyplot as plt
import numpy as np

# Your data
n_values = [4, 8, 16, 22, 32]
bt_times = [your_bt_times]
fc_times = [your_fc_times]
mac_times = [your_mac_times]

plt.figure(figsize=(10, 6))
plt.plot(n_values, bt_times, 'o-', label='Backtracking')
plt.plot(n_values, fc_times, 's-', label='Forward Checking')
plt.plot(n_values, mac_times, '^-', label='MAC')
plt.xlabel('N (Problem Size)')
plt.ylabel('Time (ms)')
plt.yscale('log')  # Log scale for better visualization
plt.title('Algorithm Performance Comparison')
plt.legend()
plt.grid(True)
plt.savefig('performance_graph.png')
```

## Updating the Report

Once you have your results:

1. Open `N-Queens_CSP_Report.md`
2. Go to Section 4.2 (Results Table)
3. Replace the placeholder values with your actual results
4. Add your graphs to Section 4.3
5. Update the analysis in Section 5 with your specific findings

## Troubleshooting

**Problem:** Program takes too long or times out
- **Solution:** Start with smaller N values (4, 8, 16) first
- For N=32+, the program may take 10+ minutes

**Problem:** Out of memory error
- **Solution:** Close other programs, or test smaller N values only

**Problem:** Results vary significantly between runs
- **Solution:** This is normal, especially with random start states. Take averages of multiple runs.

**Problem:** Can't compile
- **Solution:** Make sure you have Java JDK installed. Check with `javac -version`

## Quick Test Command

For a quick single test:
```
java AiProject
```
Then enter: `8` and `n` when prompted.

Good luck with your testing!

