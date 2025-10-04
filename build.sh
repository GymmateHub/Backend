#!/bin/bash

# Build script for GymMate Backend
# Ensures the correct Java version (21) is used for building

set -e

# Set Java 21 as the active Java version for this build
export JAVA_HOME=/Users/godswilldavid/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home

echo "Using Java version:"
java -version

echo ""
echo "Building GymMate Backend..."
mvn clean package

echo ""
echo "Build completed successfully!"
echo "JAR file location: $(ls -1 target/*.jar | head -1)"