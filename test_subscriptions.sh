#!/bin/bash

# GymMate Subscription & Rate Limiting Test Script
# This script demonstrates the subscription and rate limiting features

echo "============================================"
echo "GymMate Subscription & Rate Limiting Tests"
echo "============================================"
echo ""

# Base URL
BASE_URL="http://localhost:8080"
AUTH_TOKEN="YOUR_JWT_TOKEN_HERE"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}1. Get All Subscription Tiers${NC}"
echo "GET $BASE_URL/api/subscriptions/tiers"
curl -s -X GET "$BASE_URL/api/subscriptions/tiers" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}2. Create a New Subscription (Starter Plan with Trial)${NC}"
echo "POST $BASE_URL/api/subscriptions"
curl -s -X POST "$BASE_URL/api/subscriptions" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tierName": "starter",
    "startTrial": true
  }' | jq '.'
echo ""
echo ""

echo -e "${BLUE}3. Get Current Subscription${NC}"
echo "GET $BASE_URL/api/subscriptions/current"
curl -s -X GET "$BASE_URL/api/subscriptions/current" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}4. Check Rate Limit Status${NC}"
echo "GET $BASE_URL/api/subscriptions/rate-limit/status"
curl -s -X GET "$BASE_URL/api/subscriptions/rate-limit/status" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}5. Get Current Usage${NC}"
echo "GET $BASE_URL/api/subscriptions/usage/current"
curl -s -X GET "$BASE_URL/api/subscriptions/usage/current" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}6. Test Rate Limiting (Make 120 rapid requests)${NC}"
echo "This will exceed the burst limit of 100 requests/minute for starter plan"
echo "You should see HTTP 429 responses after 100 requests"
echo ""
for i in {1..120}
do
  response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/gyms" \
    -H "Authorization: Bearer $AUTH_TOKEN")

  if [ "$response" == "429" ]; then
    echo -e "${RED}Request $i: Rate Limited (HTTP 429)${NC}"
  else
    echo -e "${GREEN}Request $i: OK (HTTP $response)${NC}"
  fi

  # Small delay to avoid overwhelming the server
  sleep 0.01
done
echo ""
echo ""

echo -e "${BLUE}7. Check Rate Limit Status After Test${NC}"
curl -s -X GET "$BASE_URL/api/subscriptions/rate-limit/status" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}8. Get Rate Limit Statistics (Last 7 Days)${NC}"
echo "GET $BASE_URL/api/subscriptions/rate-limit/statistics?days=7"
curl -s -X GET "$BASE_URL/api/subscriptions/rate-limit/statistics?days=7" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${BLUE}9. Upgrade Subscription to Professional${NC}"
echo "POST $BASE_URL/api/subscriptions/upgrade"
curl -s -X POST "$BASE_URL/api/subscriptions/upgrade" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "newTierName": "professional"
  }' | jq '.'
echo ""
echo ""

echo -e "${BLUE}10. Verify New Rate Limits${NC}"
curl -s -X GET "$BASE_URL/api/subscriptions/rate-limit/status" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}Tests Complete!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo "Summary:"
echo "- Created subscription with starter plan"
echo "- Tested rate limiting (100 requests/minute burst limit)"
echo "- Upgraded to professional plan (500 requests/minute)"
echo "- New higher rate limits are now in effect"
echo ""
echo "Check the database tables:"
echo "  - subscription_tiers (4 default tiers)"
echo "  - gym_subscriptions (your subscription)"
echo "  - subscription_usage (current billing period usage)"
echo "  - api_rate_limits (rate limit windows)"
echo ""

