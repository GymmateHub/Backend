#!/bin/bash

# Run script for GymMate Backend
# Ensures the correct Java version (21) is used for running

set -e

# Set Java 21 as the active Java version for this run
export JAVA_HOME=/Users/godswilldavid/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home

echo "Using Java version:"
java -version

echo ""
echo "Starting GymMate Backend..."
mvn spring-boot:run