#!/bin/bash

# Test Authentication Flow
# Run this against production API

API_URL="${1:-https://api-v2.ezami.io}"
TEST_EMAIL="hienhv0711@gmail.com"
TEST_PASS="12345678"

echo "=========================================="
echo "Testing Authentication Flow"
echo "API: $API_URL"
echo "=========================================="
echo ""

# Test 1: Health Check
echo "=== Test 1: Health Check ==="
curl -s "$API_URL/actuator/health" | jq .
echo ""

# Test 2: Login with Email/Password
echo "=== Test 2: POST /authenticate ==="
RESPONSE=$(curl -s -X POST "$API_URL/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASS\"}" \
  -w "\nHTTP_CODE:%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_CODE")

echo "Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_CODE" = "200" ]; then
  TOKEN=$(echo "$BODY" | jq -r '.token' 2>/dev/null)
  if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "✅ Login SUCCESS"
    echo "Token: ${TOKEN:0:50}..."
    echo ""

    # Test 3: Use token to call protected endpoint
    echo "=== Test 3: GET /api/user/me (with token) ==="
    curl -s -X GET "$API_URL/api/user/me" \
      -H "Authorization: Bearer $TOKEN" | jq .
    echo ""
  else
    echo "❌ Login failed: No token in response"
  fi
else
  echo "❌ Login failed with HTTP $HTTP_CODE"
  echo ""

  # Parse error details
  ERROR_CODE=$(echo "$BODY" | jq -r '.code' 2>/dev/null)
  ERROR_MSG=$(echo "$BODY" | jq -r '.message' 2>/dev/null)

  echo "Error Code: $ERROR_CODE"
  echo "Error Message: $ERROR_MSG"
  echo ""

  # Common issues
  echo "=== Possible Issues ==="
  if [ "$ERROR_CODE" = "1001" ]; then
    echo "- Username or password is blank"
  elif [ "$ERROR_CODE" = "1002" ]; then
    echo "- User not found or password incorrect"
  elif [ "$ERROR_CODE" = "1003" ]; then
    echo "- User account is disabled"
  else
    echo "- Unknown error: Check API logs"
  fi
fi

echo ""
echo "=== Test 4: Google OAuth Login URL ==="
curl -s "$API_URL/auth/google/login?platform=web" | jq .
echo ""

echo "=========================================="
echo "Test Complete"
echo "=========================================="
