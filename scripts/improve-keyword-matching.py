#!/usr/bin/env python3
"""
Script to improve keyword matching for certifications with low coverage
Focus: PSM_II, ISTQB_AGILE
"""
import mysql.connector
from collections import defaultdict

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Enhanced keyword mappings for low-coverage certifications
ENHANCED_KEYWORDS = {
    'PSM_II': {
        # Facilitation
        'PSM2_FACILITATION': ['facilitation', 'facilitate', 'facilitator', 'facilitating'],
        'PSM2_CONFLICT_RES': ['conflict', 'disagreement', 'dispute', 'tension', 'resolve conflict', 'mediate'],
        'PSM2_DECISION_MAKING': ['decision', 'decide', 'choosing', 'consensus', 'agreement', 'vote'],
        'PSM2_MEETING_FACIL': ['meeting', 'workshop', 'session', 'productive meeting', 'time-box'],

        # Coaching
        'PSM2_COACHING': ['coaching', 'coach', 'mentor', 'mentoring', 'guide'],
        'PSM2_INDIVIDUAL_COACH': ['individual', 'one-on-one', '1:1', 'personal development', 'personal growth'],
        'PSM2_TEAM_COACH': ['team development', 'team dynamics', 'team performance', 'high-performing'],
        'PSM2_ORG_COACH': ['organization', 'organizational', 'enterprise', 'stakeholder', 'management'],

        # Leadership
        'PSM2_LEADERSHIP': ['leadership', 'leader', 'leading', 'lead the team'],
        'PSM2_SERVANT_LEADER': ['servant', 'servant leader', 'serve', 'serving others', 'empower'],
        'PSM2_INFLUENCE': ['influence', 'persuade', 'convince', 'without authority', 'soft skills'],
        'PSM2_CHANGE_AGENT': ['change agent', 'transformation', 'organizational change', 'culture change', 'agile transformation'],

        # Organization Design
        'PSM2_ORG_DESIGN': ['organization design', 'structure', 'team structure', 'department'],
        'PSM2_SCALING': ['scaling', 'scale', 'multiple teams', 'large scale', 'nexus', 'less', 'safe'],
        'PSM2_IMPEDIMENT_ORG': ['organizational impediment', 'systemic', 'bureaucracy', 'policy'],
        'PSM2_CULTURE': ['culture', 'mindset', 'agile culture', 'values', 'behavior'],
    },

    'ISTQB_AGILE': {
        # Agile Fundamentals
        'ISTQB_AGILE_FUND': ['agile fundamental', 'agile software', 'agile development'],
        'ISTQB_AGILE_VALUES': ['agile manifesto', 'agile values', 'agile principles', 'individuals and interactions', 'working software', 'customer collaboration', 'responding to change'],
        'ISTQB_AGILE_APPROACHES': ['scrum', 'kanban', 'xp', 'extreme programming', 'lean', 'agile methodology', 'agile framework'],
        'ISTQB_WHOLE_TEAM': ['whole team', 'cross-functional', 'collaborative', 'team responsibility', 'shared ownership'],

        # Agile Testing
        'ISTQB_AGILE_TESTING': ['agile testing', 'testing in agile', 'agile test'],
        'ISTQB_AGILE_DIFF': ['traditional', 'waterfall', 'difference', 'compared to', 'unlike traditional'],
        'ISTQB_AGILE_STATUS': ['test status', 'test progress', 'burn', 'velocity', 'sprint progress', 'dashboard'],
        'ISTQB_REGRESSION_AGILE': ['regression', 'regression testing', 'regression risk', 'automated regression'],

        # Agile Testing Techniques
        'ISTQB_AGILE_TECHNIQUES': ['agile technique', 'testing technique'],
        'ISTQB_TDD': ['tdd', 'test-driven', 'test driven', 'red green refactor', 'unit test first'],
        'ISTQB_ATDD': ['atdd', 'acceptance test-driven', 'acceptance test driven', 'specification by example'],
        'ISTQB_BDD': ['bdd', 'behavior-driven', 'behavior driven', 'given when then', 'cucumber', 'gherkin'],

        # Tools in Agile
        'ISTQB_AGILE_TOOLS': ['agile tool', 'tool support', 'automation tool'],
        'ISTQB_AUTOMATION_AGILE': ['automation', 'automated testing', 'test automation', 'continuous testing'],
        'ISTQB_CI_CD': ['ci', 'cd', 'continuous integration', 'continuous delivery', 'continuous deployment', 'pipeline', 'jenkins', 'gitlab'],
    }
}

# Question prefix patterns to detect certification type
QUESTION_PREFIX_PATTERNS = {
    'PSM2': 'PSM_II',
    'PSMII': 'PSM_II',
    'PSM_II': 'PSM_II',
    'PSM-II': 'PSM_II',
    'PSM II': 'PSM_II',
    'AGILE': 'ISTQB_AGILE',
    'ISTQB_AGILE': 'ISTQB_AGILE',
    'ISTQB-AT': 'ISTQB_AGILE',
    'CTFL-AT': 'ISTQB_AGILE',
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def get_skills_for_certification(cursor, cert_id):
    """Get all skills for a certification"""
    sql = """
    SELECT id, code, name
    FROM wp_ez_skills
    WHERE certification_id = %s AND status = 'active' AND level > 0
    """
    cursor.execute(sql, (cert_id,))
    return cursor.fetchall()

def detect_certification_from_content(title, question_text):
    """Detect certification type from question content"""
    combined = f"{title} {question_text}".upper()

    # Check for PSM II indicators
    psm2_indicators = [
        'PSM II', 'PSM-II', 'PSM2', 'ADVANCED SCRUM MASTER',
        'SCALING', 'ORGANIZATIONAL', 'SERVANT LEADER',
        'CHANGE AGENT', 'FACILITATION', 'COACHING',
        'LARGE-SCALE', 'NEXUS', 'LESS', 'SAFE'
    ]
    for indicator in psm2_indicators:
        if indicator in combined:
            return 'PSM_II'

    # Check for ISTQB Agile indicators
    agile_indicators = [
        'AGILE TESTER', 'AGILE TESTING', 'TDD', 'BDD', 'ATDD',
        'CONTINUOUS INTEGRATION', 'CI/CD', 'AGILE MANIFESTO',
        'WHOLE TEAM', 'SPRINT', 'KANBAN', 'XP', 'EXTREME PROGRAMMING',
        'SCRUM TESTING', 'REGRESSION IN AGILE'
    ]
    for indicator in agile_indicators:
        if indicator in combined:
            return 'ISTQB_AGILE'

    return None

def match_question_to_skills(question_text, title, cert_id, skills):
    """Match question to skills using enhanced keyword matching"""
    if cert_id not in ENHANCED_KEYWORDS:
        return []

    keywords = ENHANCED_KEYWORDS[cert_id]
    combined_lower = f"{title} {question_text}".lower()

    matches = []
    for skill in skills:
        skill_id, skill_code, skill_name = skill
        if skill_code in keywords:
            for kw in keywords[skill_code]:
                if kw.lower() in combined_lower:
                    matches.append((skill_id, skill_code, 1.0))
                    break

    # If no keyword match, try skill name matching
    if not matches:
        for skill in skills:
            skill_id, skill_code, skill_name = skill
            skill_name_lower = skill_name.lower()
            words = skill_name_lower.split()
            for word in words:
                if len(word) > 4 and word in combined_lower:
                    matches.append((skill_id, skill_code, 0.5))
                    break

    return matches[:3]

def insert_mapping(cursor, question_id, skill_id, weight=1.0, confidence='high'):
    """Insert question-skill mapping if not exists"""
    # Check if mapping exists
    cursor.execute("""
        SELECT id FROM wp_ez_question_skills
        WHERE question_id = %s AND skill_id = %s
    """, (question_id, skill_id))

    if cursor.fetchone():
        return False

    sql = """
    INSERT INTO wp_ez_question_skills
    (question_id, skill_id, weight, confidence, mapped_by, mapped_at)
    VALUES (%s, %s, %s, %s, 0, NOW())
    """
    cursor.execute(sql, (question_id, skill_id, weight, confidence))
    return True

def main():
    conn = get_connection()
    cursor = conn.cursor()

    # Get all questions that might be PSM_II or ISTQB_AGILE
    cursor.execute("""
        SELECT q.id, q.title, q.question
        FROM wp_learndash_pro_quiz_question q
        WHERE q.online = 1
        ORDER BY q.id
    """)
    questions = cursor.fetchall()

    print(f"Analyzing {len(questions)} questions for PSM_II and ISTQB_AGILE content...")

    # Cache skills
    skills_cache = {}
    for cert_id in ENHANCED_KEYWORDS.keys():
        skills_cache[cert_id] = get_skills_for_certification(cursor, cert_id)
        print(f"Loaded {len(skills_cache[cert_id])} skills for {cert_id}")

    stats = defaultdict(int)
    mappings_added = 0

    try:
        for q_id, title, question_text in questions:
            # Detect certification
            cert_id = detect_certification_from_content(title, question_text or '')

            if not cert_id or cert_id not in skills_cache:
                continue

            skills = skills_cache[cert_id]
            if not skills:
                continue

            # Match question to skills
            matches = match_question_to_skills(question_text or '', title, cert_id, skills)

            for skill_id, skill_code, confidence in matches:
                if insert_mapping(cursor, q_id, skill_id,
                                weight=confidence,
                                confidence='high' if confidence >= 0.8 else 'medium'):
                    mappings_added += 1
                    stats[cert_id] += 1

        conn.commit()

        print(f"\n=== Enhanced Mapping Complete ===")
        print(f"Total new mappings: {mappings_added}")
        print(f"\nBy certification:")
        for cert, count in sorted(stats.items()):
            print(f"  {cert}: {count}")

        # Show final coverage
        print("\n=== Final Coverage ===")
        for cert_id in ENHANCED_KEYWORDS.keys():
            cursor.execute("""
                SELECT
                    COUNT(DISTINCT s.id) as total_skills,
                    COUNT(DISTINCT CASE WHEN qs.question_id IS NOT NULL THEN s.id END) as skills_with_questions,
                    COUNT(DISTINCT qs.question_id) as total_questions
                FROM wp_ez_skills s
                LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
                WHERE s.certification_id = %s AND s.level > 0
            """, (cert_id,))
            total, with_q, questions = cursor.fetchone()
            pct = with_q * 100 / total if total > 0 else 0
            print(f"{cert_id}: {with_q}/{total} skills ({pct:.1f}%), {questions} questions")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
