#!/bin/bash
set -e

# This script should be run from the project root directory
# If run from src/tests/, it will change directory to root first

# Detect if we're in src/tests/ and navigate to root
if [[ $(basename "$PWD") == "tests" && $(basename "$(dirname "$PWD")") == "src" ]]; then
    cd ../..
fi

echo "Compiling..."
javac -d out -cp src src/graphs/*.java src/tests/RegressionTests.java

echo "Running RegressionTests..."
java -cp out tests.RegressionTests
