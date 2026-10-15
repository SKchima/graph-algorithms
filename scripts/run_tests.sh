#!/bin/bash
set -e

# Navigate to project root
cd "$(dirname "$0")/.."

if command -v mvn &> /dev/null && [ "$1" == "--mvn" ]; then
    echo "Running tests with Maven..."
    mvn test
else
    echo "Compiling (javac)..."
    mkdir -p out
    javac -d out -cp "src/main/java:src/test/java" \
        src/main/java/graphs/*.java \
        src/main/java/utils/*.java \
        src/test/java/tests/RegressionTests.java

    echo "Running RegressionTests..."
    java -cp out tests.RegressionTests
fi
