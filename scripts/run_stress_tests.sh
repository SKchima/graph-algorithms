#!/bin/bash
set -e

# Navigate to project root
cd "$(dirname "$0")/.."

echo "Compiling Stress Tests (javac)..."
mkdir -p out
javac -d out -cp "src/main/java:src/test/java" \
    src/main/java/graphs/*.java \
    src/main/java/utils/*.java \
    src/test/java/tests/StressTests.java

echo "Running StressTests with -Xss32m..."
java -Xss32m -Xmx2G -cp out tests.StressTests
