#!/bin/bash
# ==========================================
# TEST NEW CERTIFICATIONS API SUPPORT
# ==========================================
# This script tests backend API support for new certifications:
# - ISTQB_ADV_TM
# - ISTQB_ADV_TTA
# - SCRUM_PSPO_II
# ==========================================

set -e  # Exit on error

API_URL="${API_URL:-http://localhost:8090}"
TOKEN="${TOKEN:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if token is set
if [ -z "$TOKEN" ]; then
    echo -e "${YELLOW}⚠️  TOKEN not set. Getting token...${NC}"
    echo "Please run: export TOKEN=\$(curl -s -X POST $API_URL/authenticate -d '{\"email\":\"hienhv0711@gmail.com\",\"password\":\"12345678\"}' -H 'Content-Type: application/json' | jq -r '.token')"
    exit 1
fi

echo "=========================================="
echo "Testing New Certifications API Support"
echo "API URL: $API_URL"
echo "=========================================="
echo ""

# ==========================================
# Test 1: GET /api/certifications
# ==========================================
echo "=== Test 1: GET /api/certifications ==="
echo "Checking if new certifications appear in list..."

RESPONSE=$(curl -s -X GET "$API_URL/api/certifications" \
  -H "Authorization: Bearer $TOKEN")

# Check ISTQB_ADV_TM
if echo "$RESPONSE" | jq -e '.[] | select(.certificationId == "ISTQB_ADV_TM")' > /dev/null; then
    echo -e "${GREEN}✅ ISTQB_ADV_TM found${NC}"
    echo "$RESPONSE" | jq '.[] | select(.certificationId == "ISTQB_ADV_TM") | {certificationId, name, skillCount, questionCount}'
else
    echo -e "${RED}❌ ISTQB_ADV_TM NOT found${NC}"
fi
echo ""

# Check ISTQB_ADV_TTA
if echo "$RESPONSE" | jq -e '.[] | select(.certificationId == "ISTQB_ADV_TTA")' > /dev/null; then
    echo -e "${GREEN}✅ ISTQB_ADV_TTA found${NC}"
    echo "$RESPONSE" | jq '.[] | select(.certificationId == "ISTQB_ADV_TTA") | {certificationId, name, skillCount, questionCount}'
else
    echo -e "${RED}❌ ISTQB_ADV_TTA NOT found${NC}"
fi
echo ""

# Check SCRUM_PSPO_II
if echo "$RESPONSE" | jq -e '.[] | select(.certificationId == "SCRUM_PSPO_II")' > /dev/null; then
    echo -e "${GREEN}✅ SCRUM_PSPO_II found${NC}"
    echo "$RESPONSE" | jq '.[] | select(.certificationId == "SCRUM_PSPO_II") | {certificationId, name, skillCount, questionCount}'
else
    echo -e "${RED}❌ SCRUM_PSPO_II NOT found${NC}"
fi
echo ""

# ==========================================
# Test 2: GET /api/certifications/{id}
# ==========================================
echo "=== Test 2: GET /api/certifications/{certificationId} ==="

for CERT_ID in "ISTQB_ADV_TM" "ISTQB_ADV_TTA" "SCRUM_PSPO_II"; do
    echo "Testing certification: $CERT_ID"

    CERT_RESPONSE=$(curl -s -X GET "$API_URL/api/certifications/$CERT_ID" \
      -H "Authorization: Bearer $TOKEN")

    if echo "$CERT_RESPONSE" | jq -e '.certificationId' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ API response OK${NC}"
        echo "$CERT_RESPONSE" | jq '{certificationId, name, skillCount, questionCount, level}'
    else
        echo -e "${RED}❌ API error or certification not found${NC}"
        echo "$CERT_RESPONSE" | jq '.'
    fi
    echo ""
done

# ==========================================
# Test 3: GET /api/certifications/{id}/skills
# ==========================================
echo "=== Test 3: GET /api/certifications/{certificationId}/skills ==="

for CERT_ID in "ISTQB_ADV_TM" "ISTQB_ADV_TTA" "SCRUM_PSPO_II"; do
    echo "Testing skills for: $CERT_ID"

    SKILLS_RESPONSE=$(curl -s -X GET "$API_URL/api/certifications/$CERT_ID/skills" \
      -H "Authorization: Bearer $TOKEN")

    SKILL_COUNT=$(echo "$SKILLS_RESPONSE" | jq 'length')

    if [ "$SKILL_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✅ Found $SKILL_COUNT skills${NC}"
        echo "Sample skills:"
        echo "$SKILLS_RESPONSE" | jq '.[0:3] | .[] | {skillCode, skillName, questionCount}'
    else
        echo -e "${RED}❌ No skills found (this certification needs skills added!)${NC}"
    fi
    echo ""
done

# ==========================================
# Test 4: GET /api/certifications/{id}/skills/tree
# ==========================================
echo "=== Test 4: GET /api/certifications/{certificationId}/skills/tree ==="

for CERT_ID in "ISTQB_ADV_TM" "ISTQB_ADV_TTA" "SCRUM_PSPO_II"; do
    echo "Testing skill tree for: $CERT_ID"

    TREE_RESPONSE=$(curl -s -X GET "$API_URL/api/certifications/$CERT_ID/skills/tree" \
      -H "Authorization: Bearer $TOKEN")

    if echo "$TREE_RESPONSE" | jq -e '.certificationId' > /dev/null 2>&1; then
        TOTAL_SKILLS=$(echo "$TREE_RESPONSE" | jq '.totalSkills')
        echo -e "${GREEN}✅ Skill tree built successfully${NC}"
        echo "Total skills: $TOTAL_SKILLS"
        echo "$TREE_RESPONSE" | jq '{certificationId, totalSkills, rootSkillCount: (.skills | length)}'
    else
        echo -e "${RED}❌ Failed to build skill tree${NC}"
    fi
    echo ""
done

# ==========================================
# Test 5: POST /api/eil/diagnostic/start
# ==========================================
echo "=== Test 5: POST /api/eil/diagnostic/start ==="

for CERT_ID in "ISTQB_ADV_TM" "ISTQB_ADV_TTA" "SCRUM_PSPO_II"; do
    echo "Testing diagnostic start for: $CERT_ID"

    START_RESPONSE=$(curl -s -X POST "$API_URL/api/eil/diagnostic/start" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"mode\": \"CERTIFICATION_PRACTICE\",
        \"certificationCode\": \"$CERT_ID\",
        \"questionCount\": 10
      }")

    if echo "$START_RESPONSE" | jq -e '.sessionId' > /dev/null 2>&1; then
        SESSION_ID=$(echo "$START_RESPONSE" | jq -r '.sessionId')
        TOTAL_QUESTIONS=$(echo "$START_RESPONSE" | jq '.totalQuestions')
        echo -e "${GREEN}✅ Diagnostic session started${NC}"
        echo "Session ID: $SESSION_ID"
        echo "Total questions: $TOTAL_QUESTIONS"

        # Abandon session to clean up
        curl -s -X POST "$API_URL/api/eil/diagnostic/abandon/$SESSION_ID" \
          -H "Authorization: Bearer $TOKEN" > /dev/null
        echo "Session abandoned (cleanup)"
    else
        echo -e "${YELLOW}⚠️  Could not start diagnostic (may need questions mapped)${NC}"
        echo "$START_RESPONSE" | jq '.message // .error // .'
    fi
    echo ""
done

# ==========================================
# Summary
# ==========================================
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo ""
echo "If any certification shows errors:"
echo "1. Check if certification exists in database"
echo "2. Ensure skills are added to wp_ez_skills"
echo "3. Ensure questions are mapped to skills"
echo ""
echo "To add missing data:"
echo "  mysql -u root -p wordpress < scripts/add_new_certifications.sql"
echo "  mysql -u root -p wordpress < scripts/add_[certification]_skills.sql"
echo ""
echo "=========================================="
