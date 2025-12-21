#!/bin/bash
set -e

# This script should be run from the scripts/ directory
# Navigate to project root
cd "$(dirname "$0")/.."

echo "Compiling Stress Tests..."
javac -d out -cp src src/graphs/*.java src/utils/*.java src/tests/StressTests.java

echo "Running StressTests with -Xss32m..."
java -Xss32m -Xmx2G -cp out tests.StressTests
