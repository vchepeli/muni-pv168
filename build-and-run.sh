#!/bin/bash

# Build and Run Script for Car Rental Management System
# Uses Maven for proper dependency management and building

function db-cleanup() {

  # Stop and remove the database with old schema
  docker-compose down -v

  # Start fresh with corrected schema
  docker-compose up -d

  # Wait for PostgreSQL to initialize
  sleep 10
}

set -e

#db-cleanup

echo "Building Car Rental Management System with Maven..."
echo ""

# Check if Maven is installed
if ! command -v mvn &>/dev/null; then
  echo "Error: Maven is not installed. Please install Maven 3.6.0 or higher."
  echo "Visit: https://maven.apache.org/download.cgi"
  exit 1
fi

# Step 1: Clean and build the project
echo "Step 1: Cleaning previous builds..."
mvn clean -q

echo "Step 2: Compiling and packaging with Maven..."
mvn package -q -DskipTests

echo ""
echo "Step 3: Running the application..."
echo ""

# Run the built JAR file
JAR_FILE="gui/target/car-rental-gui-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
  echo "Error: JAR file not found at $JAR_FILE"
  echo "Build may have failed. Check the build output above."
  exit 1
fi

# Run the JAR with all dependencies included
java -jar "$JAR_FILE"

echo ""
echo "Application closed."

# Rebuild and run the application
./build-and-run.sh
