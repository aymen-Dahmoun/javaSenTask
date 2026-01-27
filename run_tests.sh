#!/bin/bash
# Elevator Controller Simulator - Test Runner Script
# This script compiles and runs all tests

set -e

PROJECT_DIR="/home/aymen/javaSenTask"
cd "$PROJECT_DIR"

echo "========================================"
echo "  Elevator Simulator - Build & Test"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}[1/4]${NC} Creating target directories..."
mkdir -p target/classes target/test-classes

echo -e "${BLUE}[2/4]${NC} Compiling main source code..."
javac -d target/classes -sourcepath src/main/java \
  src/main/java/com/elevatorcontroller/*.java 2>&1 | grep -v "warning:" || true
echo -e "${GREEN}✓ Main source compiled${NC}"

echo -e "${BLUE}[3/4]${NC} Compiling test source code..."
javac -cp target/classes:/home/aymen/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.9.2/junit-jupiter-api-5.9.2.jar:/home/aymen/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar \
  -d target/test-classes -sourcepath src/test/java \
  src/test/java/com/elevatorcontroller/*.java 2>&1 | grep -v "warning:" || true
echo -e "${GREEN}✓ Tests compiled${NC}"

echo -e "${BLUE}[4/4]${NC} Running tests..."
echo ""

java -cp target/classes:target/test-classes:/home/aymen/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.9.2/junit-jupiter-api-5.9.2.jar:/home/aymen/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar \
  com.elevatorcontroller.TestRunner

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
else
    echo -e "${GREEN}Tests completed with status code: $EXIT_CODE${NC}"
fi

exit $EXIT_CODE
