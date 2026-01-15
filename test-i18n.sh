#!/bin/bash

# Test script for i18n certification API
# Usage: ./test-i18n.sh

API_URL="http://localhost:8090"

echo "========================================="
echo "Testing Certification API with i18n"
echo "========================================="
echo ""

echo "=== Test 1: Get certifications with Accept-Language: en ==="
curl -s -X GET "$API_URL/api/certifications" \
  -H "Accept-Language: en" | jq '.[0] | {certificationId, name}' || echo "API not running or error"
echo ""
echo ""

echo "=== Test 2: Get certifications with Accept-Language: vi ==="
curl -s -X GET "$API_URL/api/certifications" \
  -H "Accept-Language: vi" | jq '.[0] | {certificationId, name}' || echo "API not running or error"
echo ""
echo ""

echo "=== Test 3: Get certifications with Accept-Language: vi-VN ==="
curl -s -X GET "$API_URL/api/certifications" \
  -H "Accept-Language: vi-VN" | jq '.[0] | {certificationId, name}' || echo "API not running or error"
echo ""
echo ""

echo "=== Test 4: Get certifications with Accept-Language: en-US ==="
curl -s -X GET "$API_URL/api/certifications" \
  -H "Accept-Language: en-US" | jq '.[0] | {certificationId, name}' || echo "API not running or error"
echo ""
echo ""

echo "========================================="
echo "Expected behavior:"
echo "- Test 1 & 4: Should return English name (e.g., 'Professional Scrum Master I')"
echo "- Test 2 & 3: Should return Vietnamese name (e.g., 'Chuyên gia Scrum cấp I')"
echo "- Note: 'nameVi' field should NO LONGER exist in response"
echo "========================================="
