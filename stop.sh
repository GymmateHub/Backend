#!/bin/bash

# Stop script for GymMate Backend
# Finds and stops running GymMate application instances

set -e

echo "Stopping GymMate Backend..."

# Find Java processes running GymMateApplication (exclude IntelliJ build processes)
PIDS=$(ps -ef | grep -E "com\.gymmate\.GymMateApplication|gymmate-backend.*\.jar" | grep -v grep | grep -v "jps-launcher" | grep -v "BuildMain" | awk '{print $2}')

if [ -z "$PIDS" ]; then
    echo "No GymMate Backend processes found running."
    exit 0
fi

echo "Found GymMate Backend processes:"
ps -ef | grep -E "com\.gymmate\.GymMateApplication|gymmate-backend.*\.jar" | grep -v grep | grep -v "jps-launcher" | grep -v "BuildMain" | while read line; do
    echo "  $line"
done

echo ""
echo "Stopping processes: $PIDS"

# Stop each process gracefully first (SIGTERM)
for PID in $PIDS; do
    if ps -p $PID > /dev/null 2>&1; then
        echo "Stopping process $PID..."
        kill $PID
    fi
done

# Wait up to 10 seconds for graceful shutdown
echo "Waiting for graceful shutdown..."
WAIT_COUNT=0
while [ $WAIT_COUNT -lt 10 ]; do
    REMAINING_PIDS=""
    for PID in $PIDS; do
        if ps -p $PID > /dev/null 2>&1; then
            REMAINING_PIDS="$REMAINING_PIDS $PID"
        fi
    done
    
    if [ -z "$REMAINING_PIDS" ]; then
        echo "All processes stopped successfully."
        exit 0
    fi
    
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))
done

# Force kill any remaining processes (SIGKILL)
if [ -n "$REMAINING_PIDS" ]; then
    echo "Force killing remaining processes: $REMAINING_PIDS"
    for PID in $REMAINING_PIDS; do
        if ps -p $PID > /dev/null 2>&1; then
            echo "Force killing process $PID..."
            kill -9 $PID
        fi
    done
fi

echo "GymMate Backend stopped."