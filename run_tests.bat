@echo off
echo Compiling AiProject.java...
javac AiProject.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running automated tests...
echo.
java -cp . AiProject --test > test_results.txt

echo.
echo Tests complete! Results saved to test_results.txt
echo.
pause

