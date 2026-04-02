#!/bin/bash

echo "Compiling AiProject.java..."
javac AiProject.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Running automated tests..."
echo ""
java -cp . AiProject --test > test_results.txt

echo ""
echo "Tests complete! Results saved to test_results.txt"
echo ""

