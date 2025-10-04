#!/bin/bash

# Test script for GymMate Backend API
# Run this script to test the user and gym onboarding endpoints

export JAVA_HOME=/Users/godswilldavid/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home

echo "Starting GymMate Backend for testing..."

# Start the application in background
java -jar target/gymmate-backend-0.0.1-SNAPSHOT.jar --server.port=8081 &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 10

# Function to make API calls
make_api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo "Testing: $description"
    echo "Request: $method $url"
    if [ -n "$data" ]; then
        echo "Data: $data"
        response=$(curl -s -X $method \
            -H "Content-Type: application/json" \
            -d "$data" \
            "http://localhost:8081$url" || echo "ERROR")
    else
        response=$(curl -s -X $method "http://localhost:8081$url" || echo "ERROR")
    fi
    
    echo "Response: $response"
    echo "---"
}

# Test user registration
USER_DATA='{
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "SecurePass123",
    "phoneNumber": "+1234567890",
    "role": "GYM_OWNER"
}'

make_api_call "POST" "/api/users/register/gym-owner" "$USER_DATA" "Register new gym owner"

# Test gym registration
GYM_DATA='{
    "ownerId": 1,
    "name": "John's Fitness Center",
    "description": "A modern fitness center with state-of-the-art equipment",
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "contactEmail": "info@johnsfitness.com",
    "contactPhone": "+1234567891"
}'

make_api_call "POST" "/api/gyms/register" "$GYM_DATA" "Register new gym"

# Test getting user by ID
make_api_call "GET" "/api/users/1" "" "Get user by ID"

# Test getting gym by ID
make_api_call "GET" "/api/gyms/1" "" "Get gym by ID"

# Test getting all active gyms
make_api_call "GET" "/api/gyms/active" "" "Get all active gyms"

# Stop the application
echo "Stopping application..."
kill $APP_PID
wait $APP_PID 2>/dev/null

echo "Test completed!"