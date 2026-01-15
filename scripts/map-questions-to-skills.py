#!/usr/bin/env python3
"""
Script to map questions to skills using keyword matching
"""
import mysql.connector
import re
from collections import defaultdict

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Keyword mappings for each certification
KEYWORD_MAPPINGS = {
    'PSM_I': {
        # Scrum Theory
        'PSM_EMPIRICISM': ['empiricism', 'empirical', 'inspect and adapt'],
        'PSM_TRANSPARENCY': ['transparency', 'transparent', 'visible', 'visibility'],
        'PSM_INSPECTION': ['inspection', 'inspect', 'inspecting'],
        'PSM_ADAPTATION': ['adaptation', 'adapt', 'adapting', 'adjust'],
        'PSM_LEAN_THINKING': ['lean', 'waste', 'minimize waste'],

        # Scrum Values
        'PSM_COMMITMENT': ['commitment', 'committed', 'commit to'],
        'PSM_FOCUS': ['focus', 'focused', 'concentrate'],
        'PSM_OPENNESS': ['openness', 'open about', 'transparent'],
        'PSM_RESPECT': ['respect', 'respectful', 'mutual respect'],
        'PSM_COURAGE': ['courage', 'courageous', 'brave'],

        # Developers
        'PSM_DEVELOPERS': ['developer', 'developers', 'development team'],
        'PSM_DEV_SPRINT_BACKLOG': ['sprint backlog', 'create sprint backlog'],
        'PSM_DEV_QUALITY': ['definition of done', 'dod', 'quality', 'done criteria'],
        'PSM_DEV_DAILY_ADAPT': ['daily adaptation', 'adapt daily', 'adjust plan'],
        'PSM_DEV_ACCOUNTABILITY': ['accountability', 'accountable', 'responsible'],

        # Product Owner
        'PSM_PRODUCT_OWNER': ['product owner', 'po role'],
        'PSM_PO_GOAL': ['product goal', 'vision'],
        'PSM_PO_PBI': ['product backlog item', 'pbi', 'backlog item'],
        'PSM_PO_ORDERING': ['ordering', 'order backlog', 'prioritize backlog'],
        'PSM_PO_TRANSPARENCY': ['backlog transparency', 'visible backlog'],

        # Scrum Master
        'PSM_SCRUM_MASTER': ['scrum master', 'sm role'],
        'PSM_SM_COACHING': ['coaching', 'coach the team', 'facilitate'],
        'PSM_SM_FOCUS': ['focus on increment', 'high-value'],
        'PSM_SM_IMPEDIMENTS': ['impediment', 'remove impediment', 'blocker', 'obstacle'],
        'PSM_SM_EVENTS': ['scrum event', 'facilitate event'],
        'PSM_SM_SERVANT': ['servant leader', 'servant leadership', 'serve the team'],

        # Self-Managing & Cross-Functional
        'PSM_SELF_MANAGING': ['self-managing', 'self-organized', 'self-organization', 'autonomous'],
        'PSM_CROSS_FUNCTIONAL': ['cross-functional', 'all skills needed'],

        # Sprint
        'PSM_SPRINT': ['sprint', 'iteration', 'timebox'],
        'PSM_SPRINT_LENGTH': ['sprint length', 'sprint duration', 'one month', 'two weeks'],
        'PSM_SPRINT_GOAL': ['sprint goal', 'objective of sprint'],
        'PSM_SPRINT_SCOPE': ['sprint scope', 'scope clarification'],

        # Sprint Planning
        'PSM_SPRINT_PLANNING': ['sprint planning', 'planning meeting'],
        'PSM_PLANNING_WHY': ['why this sprint', 'value of sprint'],
        'PSM_PLANNING_WHAT': ['what can be done', 'select items'],
        'PSM_PLANNING_HOW': ['how to do', 'decompose', 'break down'],

        # Daily Scrum
        'PSM_DAILY_SCRUM': ['daily scrum', 'daily standup', 'daily meeting', '15 minutes', '15-minute'],
        'PSM_DAILY_PURPOSE': ['daily purpose', 'inspect progress'],
        'PSM_DAILY_PLAN': ['daily plan', 'plan for day'],

        # Sprint Review
        'PSM_SPRINT_REVIEW': ['sprint review', 'review meeting', 'demo'],
        'PSM_REVIEW_INSPECT': ['inspect increment', 'demonstrate'],
        'PSM_REVIEW_ADAPT': ['adapt backlog', 'adjust backlog after review'],

        # Sprint Retrospective
        'PSM_SPRINT_RETRO': ['retrospective', 'retro', 'sprint retrospective'],
        'PSM_RETRO_INSPECT': ['inspect sprint', 'what went well', 'what went wrong'],
        'PSM_RETRO_IMPROVE': ['improvement', 'improve', 'action items'],

        # Artifacts
        'PSM_PRODUCT_BACKLOG': ['product backlog'],
        'PSM_PB_REFINEMENT': ['refinement', 'grooming', 'backlog refinement'],
        'PSM_PB_PRODUCT_GOAL': ['product goal', 'long-term objective'],
        'PSM_SPRINT_BACKLOG': ['sprint backlog'],
        'PSM_SB_GOAL': ['sprint goal'],
        'PSM_SB_PLAN': ['sprint plan', 'plan for sprint'],
        'PSM_INCREMENT': ['increment', 'potentially releasable', 'done increment'],
        'PSM_INC_USABLE': ['usable increment', 'meets dod'],
        'PSM_INC_DOD': ['definition of done', 'done criteria'],
    },

    'ISTQB_FOUNDATION': {
        # Fundamentals
        'ISTQB_WHAT_TESTING': ['what is testing', 'testing definition', 'purpose of testing'],
        'ISTQB_WHY_TESTING': ['why testing', 'importance of testing', 'testing necessary'],
        'ISTQB_PRINCIPLES': ['testing principle', 'seven principles', 'exhaustive testing'],
        'ISTQB_ACTIVITIES': ['test activities', 'test process', 'planning and control'],
        'ISTQB_TESTWARE': ['testware', 'test artifacts', 'test documentation'],
        'ISTQB_PSYCHOLOGY': ['psychology', 'confirmation bias', 'tester mindset'],

        # Lifecycle
        'ISTQB_SDLC_MODELS': ['sdlc', 'waterfall', 'v-model', 'agile model', 'iterative'],
        'ISTQB_TEST_LEVELS': ['test level', 'unit test', 'component test', 'integration test', 'system test', 'acceptance test'],
        'ISTQB_TEST_TYPES': ['test type', 'functional test', 'non-functional', 'regression', 'confirmation'],
        'ISTQB_MAINTENANCE': ['maintenance testing', 'impact analysis'],

        # Static Testing
        'ISTQB_STATIC_BASICS': ['static testing', 'static analysis', 'review'],
        'ISTQB_REVIEW_PROCESS': ['review process', 'formal review', 'review roles'],
        'ISTQB_REVIEW_TYPES': ['walkthrough', 'technical review', 'inspection', 'informal review'],

        # Techniques
        'ISTQB_BB_TECHNIQUES': ['black-box', 'equivalence', 'boundary value', 'decision table', 'state transition'],
        'ISTQB_WB_TECHNIQUES': ['white-box', 'statement coverage', 'branch coverage', 'code coverage'],
        'ISTQB_EXPERIENCE': ['experience-based', 'error guessing', 'exploratory'],

        # Management
        'ISTQB_PLANNING': ['test plan', 'test planning', 'test estimation'],
        'ISTQB_MONITORING': ['test monitoring', 'test control', 'test progress'],
        'ISTQB_CONFIG_MGMT': ['configuration management', 'version control', 'baseline'],
        'ISTQB_DEFECT_MGMT': ['defect management', 'bug report', 'defect lifecycle'],

        # Tools
        'ISTQB_TOOL_TYPES': ['test tool', 'automation tool', 'test management tool'],
        'ISTQB_TOOL_BENEFITS': ['tool benefit', 'tool risk', 'tool selection'],
    },

    'CBAP': {
        # Planning
        'CBAP_APPROACH': ['ba approach', 'business analysis approach'],
        'CBAP_STAKEHOLDER_PLAN': ['stakeholder engagement', 'stakeholder plan'],
        'CBAP_GOVERNANCE': ['ba governance', 'governance approach'],
        'CBAP_INFO_MGMT': ['information management', 'requirements repository'],

        # Elicitation
        'CBAP_PREPARE_ELICIT': ['prepare elicitation', 'elicitation preparation'],
        'CBAP_CONDUCT_ELICIT': ['conduct elicitation', 'interview', 'workshop', 'brainstorming', 'focus group'],
        'CBAP_CONFIRM_ELICIT': ['confirm elicitation', 'verify understanding'],
        'CBAP_COMMUNICATE': ['communicate ba', 'present findings'],

        # Lifecycle
        'CBAP_TRACE': ['trace requirement', 'traceability', 'requirements trace'],
        'CBAP_MAINTAIN': ['maintain requirement', 'requirements maintenance'],
        'CBAP_PRIORITIZE': ['prioritize', 'priority', 'moscow', 'ranking'],
        'CBAP_ASSESS_CHANGES': ['assess change', 'impact analysis', 'change request'],
        'CBAP_APPROVE': ['approve requirement', 'requirements approval', 'sign-off'],

        # Analysis
        'CBAP_SPECIFY': ['specify requirement', 'document requirement', 'use case', 'user story'],
        'CBAP_VERIFY': ['verify requirement', 'requirements verification', 'review requirement'],
        'CBAP_VALIDATE': ['validate requirement', 'requirements validation', 'acceptance criteria'],
        'CBAP_DEFINE_ARCH': ['requirements architecture', 'decomposition', 'requirements structure'],

        # Strategy
        'CBAP_CURRENT_STATE': ['current state', 'as-is', 'current situation'],
        'CBAP_FUTURE_STATE': ['future state', 'to-be', 'target state'],
        'CBAP_ASSESS_RISK': ['risk assessment', 'identify risk', 'risk analysis'],
        'CBAP_CHANGE_STRATEGY': ['change strategy', 'transition', 'implementation approach'],

        # Solution
        'CBAP_MEASURE_PERF': ['measure performance', 'solution performance', 'kpi'],
        'CBAP_ANALYZE_VALUE': ['analyze value', 'value realization', 'benefit analysis'],
        'CBAP_ASSESS_LIMITS': ['solution limitation', 'constraint', 'gap analysis'],
        'CBAP_RECOMMEND': ['recommend action', 'recommendation', 'improvement action'],
    }
}

# Question prefix to certification mapping
QUESTION_PREFIX_MAP = {
    'PSM1_': 'PSM_I',
    'PSMI_': 'PSM_I',
    'PSM2_': 'PSM_II',
    'PSMII_': 'PSM_II',
    'PSPO1_': 'PSPO_I',
    'PSPOI_': 'PSPO_I',
    'PSPO2_': 'PSPO_II',
    'ISTQB_': 'ISTQB_FOUNDATION',
    'CTFL_': 'ISTQB_FOUNDATION',
    'CTLF_': 'ISTQB_FOUNDATION',
    'CTAL_': 'ISTQB_FOUNDATION',
    'CBAP_': 'CBAP',
    'CCBA_': 'CCBA',
    'ECBA_': 'ECBA',
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def get_certification_from_title(title):
    """Determine certification from question title prefix"""
    title_upper = title.upper()
    for prefix, cert in QUESTION_PREFIX_MAP.items():
        if title_upper.startswith(prefix):
            return cert
    return None

def get_skills_for_certification(cursor, cert_id):
    """Get all leaf skills for a certification"""
    sql = """
    SELECT id, code, name
    FROM wp_ez_skills
    WHERE certification_id = %s AND status = 'active'
    """
    cursor.execute(sql, (cert_id,))
    return cursor.fetchall()

def match_question_to_skills(question_text, cert_id, skills):
    """Match question to skills using keyword matching"""
    if cert_id not in KEYWORD_MAPPINGS:
        return []

    keywords = KEYWORD_MAPPINGS[cert_id]
    question_lower = question_text.lower()

    matches = []
    for skill in skills:
        skill_id, skill_code, skill_name = skill
        if skill_code in keywords:
            for kw in keywords[skill_code]:
                if kw.lower() in question_lower:
                    matches.append((skill_id, skill_code, 1.0))  # confidence = 1.0 for exact match
                    break

    # If no keyword match, try to match by skill name
    if not matches:
        for skill in skills:
            skill_id, skill_code, skill_name = skill
            skill_name_lower = skill_name.lower()
            words = skill_name_lower.split()
            # Check if any significant word from skill name is in question
            for word in words:
                if len(word) > 4 and word in question_lower:
                    matches.append((skill_id, skill_code, 0.5))  # lower confidence
                    break

    return matches[:3]  # Return max 3 skills per question

def insert_mapping(cursor, question_id, skill_id, weight=1.0, confidence='high'):
    """Insert question-skill mapping"""
    sql = """
    INSERT INTO wp_ez_question_skills
    (question_id, skill_id, weight, confidence, mapped_by, mapped_at)
    VALUES (%s, %s, %s, %s, 0, NOW())
    """
    cursor.execute(sql, (question_id, skill_id, weight, confidence))

def main():
    conn = get_connection()
    cursor = conn.cursor()

    # Get all questions
    cursor.execute("""
        SELECT q.id, q.title, q.question, m.name as quiz_name
        FROM wp_learndash_pro_quiz_question q
        JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
        ORDER BY q.id
    """)
    questions = cursor.fetchall()

    print(f"Found {len(questions)} questions to process")

    # Cache skills by certification
    skills_cache = {}

    stats = defaultdict(int)
    mapped_count = 0

    try:
        for idx, (q_id, title, question_text, quiz_name) in enumerate(questions):
            # Determine certification from title
            cert_id = get_certification_from_title(title)
            if not cert_id:
                stats['no_cert'] += 1
                continue

            # Get skills for this certification
            if cert_id not in skills_cache:
                skills_cache[cert_id] = get_skills_for_certification(cursor, cert_id)
            skills = skills_cache[cert_id]

            if not skills:
                stats['no_skills'] += 1
                continue

            # Match question to skills
            combined_text = f"{title} {question_text}"
            matches = match_question_to_skills(combined_text, cert_id, skills)

            if matches:
                for skill_id, skill_code, confidence in matches:
                    insert_mapping(cursor, q_id, skill_id,
                                 weight=confidence,
                                 confidence='high' if confidence >= 0.8 else 'medium')
                    mapped_count += 1
                stats[cert_id] += 1
            else:
                # Fallback: map to root skill
                root_skill = [s for s in skills if s[1].endswith('_ROOT')]
                if root_skill:
                    insert_mapping(cursor, q_id, root_skill[0][0], weight=0.3, confidence='low')
                    mapped_count += 1
                    stats[f'{cert_id}_fallback'] += 1

            if (idx + 1) % 500 == 0:
                print(f"Processed {idx + 1}/{len(questions)} questions...")
                conn.commit()

        conn.commit()

        print(f"\n=== Mapping Complete ===")
        print(f"Total mappings created: {mapped_count}")
        print(f"\nBy certification:")
        for key, count in sorted(stats.items()):
            print(f"  {key}: {count}")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
