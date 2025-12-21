#!/bin/bash
set -e

# This script should be run from the scripts/ directory
# Navigate to project root
cd "$(dirname "$0")/.."

echo "Compiling..."
javac -d out -cp src src/graphs/*.java src/tests/RegressionTests.java

echo "Running RegressionTests..."
java -cp out tests.RegressionTests
