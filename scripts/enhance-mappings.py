#!/usr/bin/env python3
"""
Script to enhance mappings for skills with few or no questions
"""
import mysql.connector
import re

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Enhanced keywords for missing skills
ENHANCED_KEYWORDS = {
    'PSM_I': {
        # Level 1 category skills - map to all questions that don't have specific mappings
        'PSM_SCRUM_TEAM': ['team', 'role', 'member', 'developer', 'product owner', 'scrum master'],
        'PSM_SCRUM_EVENTS': ['event', 'meeting', 'sprint', 'daily', 'review', 'retrospective', 'planning'],
        'PSM_SCRUM_ARTIFACTS': ['artifact', 'backlog', 'increment', 'done'],

        # Level 2 values
        'PSM_OPENNESS': ['open', 'honest', 'share', 'sharing', 'disclose'],
        'PSM_RESPECT': ['respect', 'trust', 'appreciate', 'value each other'],
        'PSM_COURAGE': ['courage', 'brave', 'difficult decision', 'tough', 'challenging'],

        # Level 3 detailed
        'PSM_SM_COACHING': ['coach', 'mentor', 'guide', 'help team', 'teach'],
        'PSM_SM_SERVANT': ['servant', 'serve', 'support', 'enable'],
        'PSM_SM_FOCUS': ['focus on value', 'high-value', 'valuable increment'],
        'PSM_RETRO_INSPECT': ['what went', 'went well', 'went wrong', 'improve', 'lesson learned'],
        'PSM_INC_USABLE': ['usable', 'releasable', 'working software', 'done increment'],
        'PSM_PLANNING_WHY': ['why valuable', 'business value', 'sprint valuable'],
        'PSM_PLANNING_WHAT': ['what to do', 'select items', 'forecast', 'capacity'],
        'PSM_PLANNING_HOW': ['how to', 'decompose', 'task', 'break down work'],
        'PSM_REVIEW_ADAPT': ['adapt backlog', 'update backlog', 'change priority'],
        'PSM_DAILY_PLAN': ['plan for day', 'today plan', 'work plan'],
        'PSM_SPRINT_SCOPE': ['scope change', 'scope clarification', 'negotiate scope'],
        'PSM_DEV_DAILY_ADAPT': ['adapt plan', 'adjust plan', 're-plan'],
        'PSM_PO_TRANSPARENCY': ['transparent backlog', 'visible backlog', 'clear backlog'],
    },
    'PSPO_I': {
        'PSPO_AGILE_PO': ['product owner', 'po ', 'agile product'],
        'PSPO_VALUE_DRIVEN': ['value', 'roi', 'business value', 'maximize value'],
        'PSPO_STAKEHOLDER': ['stakeholder', 'customer', 'user', 'sponsor'],
        'PSPO_MARKET_SENSE': ['market', 'competition', 'customer need'],
        'PSPO_BACKLOG_MGMT': ['backlog', 'pbi', 'product backlog'],
        'PSPO_PBI_CREATION': ['user story', 'requirement', 'feature'],
        'PSPO_ORDERING': ['order', 'prioritize', 'priority', 'sequence'],
        'PSPO_REFINEMENT': ['refine', 'groom', 'detail', 'elaborate'],
        'PSPO_PRODUCT_VISION': ['vision', 'goal', 'roadmap'],
        'PSPO_VISION': ['product vision', 'long-term', 'direction'],
        'PSPO_GOAL': ['product goal', 'objective', 'outcome'],
        'PSPO_ROADMAP': ['roadmap', 'release plan', 'timeline'],
        'PSPO_SCRUM_EVENTS': ['event', 'ceremony', 'meeting'],
        'PSPO_PLANNING': ['planning', 'sprint planning'],
        'PSPO_REVIEW': ['review', 'demo', 'inspect'],
        'PSPO_RELEASE': ['release', 'deploy', 'launch'],
    },
    'PSM_II': {
        'PSM2_FACILITATION': ['facilitate', 'facilitating', 'facilitation'],
        'PSM2_CONFLICT_RES': ['conflict', 'disagreement', 'resolve'],
        'PSM2_DECISION_MAKING': ['decision', 'consensus', 'agreement'],
        'PSM2_MEETING_FACIL': ['meeting', 'workshop', 'session'],
        'PSM2_COACHING': ['coach', 'coaching', 'mentor'],
        'PSM2_INDIVIDUAL_COACH': ['individual', 'person', 'team member'],
        'PSM2_TEAM_COACH': ['team coaching', 'team development'],
        'PSM2_ORG_COACH': ['organization', 'stakeholder', 'management'],
        'PSM2_LEADERSHIP': ['leader', 'leadership', 'lead'],
        'PSM2_SERVANT_LEADER': ['servant', 'serve', 'support'],
        'PSM2_INFLUENCE': ['influence', 'persuade', 'convince'],
        'PSM2_CHANGE_AGENT': ['change', 'transformation', 'improve'],
        'PSM2_ORG_DESIGN': ['organization', 'structure', 'design'],
        'PSM2_SCALING': ['scale', 'multiple team', 'large'],
        'PSM2_IMPEDIMENT_ORG': ['impediment', 'obstacle', 'blocker'],
        'PSM2_CULTURE': ['culture', 'mindset', 'agile culture'],
    },
    'ISTQB_AGILE': {
        'ISTQB_AGILE_FUND': ['agile', 'scrum', 'kanban'],
        'ISTQB_AGILE_VALUES': ['agile value', 'manifesto', 'principle'],
        'ISTQB_AGILE_APPROACHES': ['scrum', 'kanban', 'xp', 'extreme programming'],
        'ISTQB_WHOLE_TEAM': ['whole team', 'collaborative', 'together'],
        'ISTQB_AGILE_TESTING': ['agile testing', 'test in agile'],
        'ISTQB_AGILE_DIFF': ['differ', 'traditional', 'waterfall'],
        'ISTQB_AGILE_STATUS': ['test status', 'progress', 'burndown'],
        'ISTQB_REGRESSION_AGILE': ['regression', 'automated test', 'safety net'],
        'ISTQB_AGILE_TECHNIQUES': ['technique', 'practice', 'approach'],
        'ISTQB_TDD': ['tdd', 'test-driven', 'test first'],
        'ISTQB_ATDD': ['atdd', 'acceptance test', 'acceptance-driven'],
        'ISTQB_BDD': ['bdd', 'behavior-driven', 'given when then'],
        'ISTQB_AGILE_TOOLS': ['tool', 'automation', 'ci/cd'],
        'ISTQB_AUTOMATION_AGILE': ['automate', 'automated', 'automation'],
        'ISTQB_CI_CD': ['ci', 'cd', 'continuous', 'pipeline'],
    },
    'CCBA': {
        'CCBA_PLANNING': ['plan', 'approach', 'business analysis'],
        'CCBA_APPROACH': ['approach', 'methodology', 'framework'],
        'CCBA_STAKEHOLDER': ['stakeholder', 'sponsor', 'user'],
        'CCBA_ELICITATION': ['elicit', 'gather', 'collect'],
        'CCBA_TECHNIQUES': ['technique', 'interview', 'workshop', 'brainstorm'],
        'CCBA_COLLABORATION': ['collaborate', 'work together', 'communicate'],
        'CCBA_ANALYSIS': ['analyze', 'analysis', 'requirement'],
        'CCBA_SPECIFY': ['specify', 'document', 'describe'],
        'CCBA_MODEL': ['model', 'diagram', 'visual'],
        'CCBA_LIFECYCLE': ['lifecycle', 'manage', 'maintain'],
        'CCBA_TRACE': ['trace', 'traceability', 'link'],
        'CCBA_PRIORITIZE': ['prioritize', 'priority', 'rank'],
    },
    'ECBA': {
        'ECBA_FUNDAMENTALS': ['business analysis', 'ba ', 'analyst'],
        'ECBA_WHAT_BA': ['what is', 'definition', 'concept'],
        'ECBA_BA_ROLE': ['role', 'responsibility', 'task'],
        'ECBA_TECHNIQUES': ['technique', 'method', 'approach'],
        'ECBA_ELICIT': ['elicit', 'gather', 'interview'],
        'ECBA_DOCUMENT': ['document', 'write', 'specification'],
        'ECBA_STAKEHOLDER': ['stakeholder', 'sponsor', 'customer'],
        'ECBA_IDENTIFY': ['identify', 'find', 'discover'],
        'ECBA_COMMUNICATE': ['communicate', 'present', 'report'],
    },
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def get_skills_without_questions(cursor, cert_id):
    """Get skills that have no questions mapped"""
    sql = """
    SELECT s.id, s.code, s.name
    FROM wp_ez_skills s
    LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
    WHERE s.certification_id = %s AND qs.id IS NULL
    """
    cursor.execute(sql, (cert_id,))
    return cursor.fetchall()

def get_unmapped_questions(cursor, prefix_pattern):
    """Get questions that don't have mappings yet"""
    sql = """
    SELECT q.id, q.title, q.question
    FROM wp_learndash_pro_quiz_question q
    LEFT JOIN wp_ez_question_skills qs ON q.id = qs.question_id
    WHERE q.title LIKE %s AND qs.id IS NULL
    """
    cursor.execute(sql, (prefix_pattern,))
    return cursor.fetchall()

def get_questions_with_few_mappings(cursor, prefix_pattern, max_mappings=1):
    """Get questions that have only root skill mapping"""
    sql = """
    SELECT q.id, q.title, q.question, COUNT(qs.id) as mapping_count
    FROM wp_learndash_pro_quiz_question q
    LEFT JOIN wp_ez_question_skills qs ON q.id = qs.question_id
    WHERE q.title LIKE %s
    GROUP BY q.id, q.title, q.question
    HAVING mapping_count <= %s
    """
    cursor.execute(sql, (prefix_pattern, max_mappings))
    return cursor.fetchall()

def match_enhanced_keywords(text, keywords_dict):
    """Match text against enhanced keywords"""
    text_lower = text.lower()
    matches = []

    for skill_code, keywords in keywords_dict.items():
        for kw in keywords:
            if kw.lower() in text_lower:
                matches.append(skill_code)
                break

    return list(set(matches))

def get_skill_id(cursor, cert_id, skill_code):
    """Get skill ID by code"""
    sql = "SELECT id FROM wp_ez_skills WHERE certification_id = %s AND code = %s"
    cursor.execute(sql, (cert_id, skill_code))
    result = cursor.fetchone()
    return result[0] if result else None

def insert_mapping(cursor, question_id, skill_id, weight=0.8, confidence='medium'):
    """Insert question-skill mapping"""
    # Check if mapping already exists
    cursor.execute(
        "SELECT id FROM wp_ez_question_skills WHERE question_id = %s AND skill_id = %s",
        (question_id, skill_id)
    )
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

    # Certification to prefix mapping
    cert_prefixes = {
        'PSM_I': 'PSM%',
        'PSPO_I': 'PSPO%',
        'PSM_II': 'PSM2%',
        'ISTQB_AGILE': 'ISTQB%Agile%',
        'CCBA': 'CCBA%',
        'ECBA': 'ECBA%',
    }

    total_new_mappings = 0

    try:
        for cert_id, prefix in cert_prefixes.items():
            if cert_id not in ENHANCED_KEYWORDS:
                continue

            print(f"\nProcessing {cert_id}...")

            # Get questions with few mappings
            questions = get_questions_with_few_mappings(cursor, prefix, 1)
            print(f"  Found {len(questions)} questions with <=1 mappings")

            new_mappings = 0
            for q_id, title, question, mapping_count in questions:
                combined_text = f"{title} {question}"
                matched_codes = match_enhanced_keywords(combined_text, ENHANCED_KEYWORDS[cert_id])

                for skill_code in matched_codes[:3]:  # Max 3 skills per question
                    skill_id = get_skill_id(cursor, cert_id, skill_code)
                    if skill_id:
                        if insert_mapping(cursor, q_id, skill_id, 0.7, 'medium'):
                            new_mappings += 1

            print(f"  Added {new_mappings} new mappings for {cert_id}")
            total_new_mappings += new_mappings
            conn.commit()

        print(f"\n=== Total new mappings: {total_new_mappings} ===")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
