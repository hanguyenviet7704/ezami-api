#!/usr/bin/env python3
"""
Script to map all remaining unmapped questions
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

# Complete keyword mappings for all certifications
KEYWORD_MAPPINGS = {
    'PSM_I': {
        'PSM_EMPIRICISM': ['empiricism', 'empirical', 'inspect and adapt', 'transparency inspection adaptation'],
        'PSM_TRANSPARENCY': ['transparency', 'transparent', 'visible', 'visibility', 'openness about work'],
        'PSM_INSPECTION': ['inspection', 'inspect', 'inspecting', 'examine', 'review progress'],
        'PSM_ADAPTATION': ['adaptation', 'adapt', 'adapting', 'adjust', 'change based on'],
        'PSM_LEAN_THINKING': ['lean', 'waste', 'minimize waste', 'eliminate waste'],
        'PSM_COMMITMENT': ['commitment', 'committed', 'commit to', 'dedication'],
        'PSM_FOCUS': ['focus', 'focused', 'concentrate', 'sprint work'],
        'PSM_OPENNESS': ['openness', 'open about', 'honest', 'candid'],
        'PSM_RESPECT': ['respect', 'respectful', 'mutual respect', 'trust'],
        'PSM_COURAGE': ['courage', 'courageous', 'brave', 'tough problems'],
        'PSM_DEVELOPERS': ['developer', 'developers', 'development team', 'dev team'],
        'PSM_DEV_SPRINT_BACKLOG': ['sprint backlog', 'create sprint backlog', 'plan for sprint'],
        'PSM_DEV_QUALITY': ['definition of done', 'dod', 'quality', 'done criteria', 'quality standards'],
        'PSM_DEV_DAILY_ADAPT': ['daily adaptation', 'adapt daily', 'adjust plan each day'],
        'PSM_DEV_ACCOUNTABILITY': ['accountability', 'accountable', 'responsible', 'ownership'],
        'PSM_PRODUCT_OWNER': ['product owner', 'po ', ' po,', 'po role', 'product ownership'],
        'PSM_PO_GOAL': ['product goal', 'vision', 'long-term objective', 'product direction'],
        'PSM_PO_PBI': ['product backlog item', 'pbi', 'backlog item', 'user story', 'feature'],
        'PSM_PO_ORDERING': ['ordering', 'order backlog', 'prioritize backlog', 'priority', 'sequence'],
        'PSM_PO_TRANSPARENCY': ['backlog transparency', 'visible backlog', 'understood by all'],
        'PSM_SCRUM_MASTER': ['scrum master', 'sm role', 'scrum master accountability'],
        'PSM_SM_COACHING': ['coaching', 'coach the team', 'facilitate', 'mentor', 'guide'],
        'PSM_SM_FOCUS': ['focus on increment', 'high-value', 'maximize value'],
        'PSM_SM_IMPEDIMENTS': ['impediment', 'remove impediment', 'blocker', 'obstacle', 'remove obstacle'],
        'PSM_SM_EVENTS': ['scrum event', 'facilitate event', 'timeboxed event', 'run event'],
        'PSM_SM_SERVANT': ['servant leader', 'servant leadership', 'serve the team', 'service'],
        'PSM_SELF_MANAGING': ['self-managing', 'self-organized', 'self-organization', 'autonomous', 'self-manage'],
        'PSM_CROSS_FUNCTIONAL': ['cross-functional', 'all skills needed', 'multi-skilled', 'complete work'],
        'PSM_SPRINT': ['sprint', 'iteration', 'timebox', 'time-box'],
        'PSM_SPRINT_LENGTH': ['sprint length', 'sprint duration', 'one month', 'two weeks', 'four weeks'],
        'PSM_SPRINT_GOAL': ['sprint goal', 'objective of sprint', 'single objective', 'goal for sprint'],
        'PSM_SPRINT_SCOPE': ['sprint scope', 'scope clarification', 'scope negotiation', 'renegotiate scope'],
        'PSM_SPRINT_PLANNING': ['sprint planning', 'planning meeting', 'plan the sprint'],
        'PSM_PLANNING_WHY': ['why this sprint', 'value of sprint', 'why is this sprint valuable'],
        'PSM_PLANNING_WHAT': ['what can be done', 'select items', 'forecast', 'selected pbi'],
        'PSM_PLANNING_HOW': ['how to do', 'decompose', 'break down', 'smaller tasks'],
        'PSM_DAILY_SCRUM': ['daily scrum', 'daily standup', 'daily meeting', '15 minutes', '15-minute'],
        'PSM_DAILY_PURPOSE': ['daily purpose', 'inspect progress', 'progress toward goal'],
        'PSM_DAILY_PLAN': ['daily plan', 'plan for day', 'next 24 hours', 'upcoming work'],
        'PSM_SPRINT_REVIEW': ['sprint review', 'review meeting', 'demo', 'demonstrate increment'],
        'PSM_REVIEW_INSPECT': ['inspect increment', 'demonstrate', 'show work', 'present increment'],
        'PSM_REVIEW_ADAPT': ['adapt backlog', 'adjust backlog after review', 'update backlog'],
        'PSM_SPRINT_RETRO': ['retrospective', 'retro', 'sprint retrospective', 'reflect'],
        'PSM_RETRO_INSPECT': ['inspect sprint', 'what went well', 'what went wrong', 'lessons learned'],
        'PSM_RETRO_IMPROVE': ['improvement', 'improve', 'action items', 'actionable improvement'],
        'PSM_PRODUCT_BACKLOG': ['product backlog', 'backlog'],
        'PSM_PB_REFINEMENT': ['refinement', 'grooming', 'backlog refinement', 'refine'],
        'PSM_PB_PRODUCT_GOAL': ['product goal', 'long-term objective', 'future state'],
        'PSM_SPRINT_BACKLOG': ['sprint backlog', 'selected items', 'sprint plan'],
        'PSM_SB_GOAL': ['sprint goal', 'single objective'],
        'PSM_SB_PLAN': ['sprint plan', 'plan for sprint', 'delivery plan'],
        'PSM_INCREMENT': ['increment', 'potentially releasable', 'done increment', 'working software'],
        'PSM_INC_USABLE': ['usable increment', 'meets dod', 'potentially shippable'],
        'PSM_INC_DOD': ['definition of done', 'done criteria', 'dod'],
    },

    'PSPO_I': {
        'PSPO_AGILE_PO': ['agile product', 'product management', 'po in agile'],
        'PSPO_VALUE_DRIVEN': ['value-driven', 'maximize value', 'deliver value', 'business value'],
        'PSPO_STAKEHOLDER': ['stakeholder', 'stakeholder management', 'stakeholder engagement'],
        'PSPO_MARKET_SENSE': ['market', 'customer', 'user need', 'market research'],
        'PSPO_BACKLOG_MGMT': ['backlog management', 'manage backlog', 'product backlog'],
        'PSPO_PBI_CREATION': ['pbi creation', 'create backlog item', 'write user story'],
        'PSPO_ORDERING': ['ordering', 'order backlog', 'prioritize', 'sequence backlog'],
        'PSPO_REFINEMENT': ['refinement', 'refine', 'groom', 'detail backlog'],
        'PSPO_PRODUCT_VISION': ['product vision', 'vision', 'product direction'],
        'PSPO_VISION': ['vision', 'compelling vision', 'product vision statement'],
        'PSPO_GOAL': ['product goal', 'measurable goal', 'product objective'],
        'PSPO_ROADMAP': ['roadmap', 'product roadmap', 'release plan'],
        'PSPO_SCRUM_EVENTS': ['po in events', 'po role in', 'product owner in'],
        'PSPO_PLANNING': ['sprint planning', 'po in planning', 'clarify requirements'],
        'PSPO_REVIEW': ['sprint review', 'po in review', 'demonstrate value'],
        'PSPO_RELEASE': ['release', 'release management', 'deploy', 'go live'],
    },

    'PSM_II': {
        'PSM2_FACILITATION': ['facilitation', 'facilitate', 'facilitator'],
        'PSM2_CONFLICT_RES': ['conflict', 'disagreement', 'dispute', 'tension', 'mediate'],
        'PSM2_DECISION_MAKING': ['decision', 'decide', 'consensus', 'agreement'],
        'PSM2_MEETING_FACIL': ['meeting', 'workshop', 'session', 'productive meeting'],
        'PSM2_COACHING': ['coaching', 'coach', 'mentor', 'mentoring'],
        'PSM2_INDIVIDUAL_COACH': ['individual', 'one-on-one', 'personal development'],
        'PSM2_TEAM_COACH': ['team development', 'team dynamics', 'high-performing'],
        'PSM2_ORG_COACH': ['organization', 'organizational', 'enterprise', 'management'],
        'PSM2_LEADERSHIP': ['leadership', 'leader', 'leading'],
        'PSM2_SERVANT_LEADER': ['servant', 'serve', 'empower'],
        'PSM2_INFLUENCE': ['influence', 'persuade', 'without authority'],
        'PSM2_CHANGE_AGENT': ['change agent', 'transformation', 'culture change'],
        'PSM2_ORG_DESIGN': ['organization design', 'structure', 'department'],
        'PSM2_SCALING': ['scaling', 'scale', 'multiple teams', 'nexus', 'less', 'safe'],
        'PSM2_IMPEDIMENT_ORG': ['organizational impediment', 'systemic', 'bureaucracy'],
        'PSM2_CULTURE': ['culture', 'mindset', 'agile culture'],
    },

    'ISTQB_FOUNDATION': {
        'ISTQB_WHAT_TESTING': ['what is testing', 'testing definition', 'purpose of testing', 'testing objective'],
        'ISTQB_WHY_TESTING': ['why testing', 'importance of testing', 'testing necessary', 'need for testing'],
        'ISTQB_PRINCIPLES': ['testing principle', 'seven principles', 'exhaustive testing impossible', 'principle'],
        'ISTQB_ACTIVITIES': ['test activities', 'test process', 'planning and control', 'test workflow'],
        'ISTQB_TESTWARE': ['testware', 'test artifacts', 'test documentation', 'test deliverable'],
        'ISTQB_PSYCHOLOGY': ['psychology', 'confirmation bias', 'tester mindset', 'developer mindset'],
        'ISTQB_SDLC_MODELS': ['sdlc', 'waterfall', 'v-model', 'agile model', 'iterative', 'incremental'],
        'ISTQB_TEST_LEVELS': ['test level', 'unit test', 'component test', 'integration test', 'system test', 'acceptance test'],
        'ISTQB_TEST_TYPES': ['test type', 'functional test', 'non-functional', 'regression', 'confirmation test'],
        'ISTQB_MAINTENANCE': ['maintenance testing', 'impact analysis', 'regression analysis'],
        'ISTQB_STATIC_BASICS': ['static testing', 'static analysis', 'review', 'static technique'],
        'ISTQB_REVIEW_PROCESS': ['review process', 'formal review', 'review roles', 'review phases'],
        'ISTQB_REVIEW_TYPES': ['walkthrough', 'technical review', 'inspection', 'informal review', 'peer review'],
        'ISTQB_BB_TECHNIQUES': ['black-box', 'equivalence', 'boundary value', 'decision table', 'state transition'],
        'ISTQB_WB_TECHNIQUES': ['white-box', 'statement coverage', 'branch coverage', 'code coverage', 'structural'],
        'ISTQB_EXPERIENCE': ['experience-based', 'error guessing', 'exploratory', 'checklist-based'],
        'ISTQB_PLANNING': ['test plan', 'test planning', 'test estimation', 'test schedule'],
        'ISTQB_MONITORING': ['test monitoring', 'test control', 'test progress', 'test metrics'],
        'ISTQB_CONFIG_MGMT': ['configuration management', 'version control', 'baseline', 'config management'],
        'ISTQB_DEFECT_MGMT': ['defect management', 'bug report', 'defect lifecycle', 'defect tracking'],
        'ISTQB_TOOL_TYPES': ['test tool', 'automation tool', 'test management tool', 'tool category'],
        'ISTQB_TOOL_BENEFITS': ['tool benefit', 'tool risk', 'tool selection', 'automation benefit'],
    },

    'ISTQB_AGILE': {
        'ISTQB_AGILE_VALUES': ['agile manifesto', 'agile values', 'agile principles'],
        'ISTQB_AGILE_APPROACHES': ['scrum', 'kanban', 'xp', 'extreme programming'],
        'ISTQB_WHOLE_TEAM': ['whole team', 'cross-functional', 'collaborative testing'],
        'ISTQB_AGILE_DIFF': ['traditional', 'waterfall', 'difference from'],
        'ISTQB_AGILE_STATUS': ['test status', 'burn', 'velocity', 'dashboard'],
        'ISTQB_REGRESSION_AGILE': ['regression', 'automated regression', 'regression risk'],
        'ISTQB_TDD': ['tdd', 'test-driven', 'test driven', 'red green refactor'],
        'ISTQB_ATDD': ['atdd', 'acceptance test-driven', 'specification by example'],
        'ISTQB_BDD': ['bdd', 'behavior-driven', 'given when then', 'cucumber', 'gherkin'],
        'ISTQB_AUTOMATION_AGILE': ['automation', 'automated testing', 'continuous testing'],
        'ISTQB_CI_CD': ['ci', 'cd', 'continuous integration', 'continuous delivery', 'pipeline'],
    },

    'CBAP': {
        'CBAP_APPROACH': ['ba approach', 'business analysis approach', 'ba planning'],
        'CBAP_STAKEHOLDER_PLAN': ['stakeholder engagement', 'stakeholder plan', 'stakeholder analysis'],
        'CBAP_GOVERNANCE': ['ba governance', 'governance approach', 'decision authority'],
        'CBAP_INFO_MGMT': ['information management', 'requirements repository', 'requirement storage'],
        'CBAP_PREPARE_ELICIT': ['prepare elicitation', 'elicitation preparation', 'elicitation planning'],
        'CBAP_CONDUCT_ELICIT': ['conduct elicitation', 'interview', 'workshop', 'brainstorming', 'focus group'],
        'CBAP_CONFIRM_ELICIT': ['confirm elicitation', 'verify understanding', 'elicitation confirmation'],
        'CBAP_COMMUNICATE': ['communicate ba', 'present findings', 'communication'],
        'CBAP_TRACE': ['trace requirement', 'traceability', 'requirements trace', 'requirement relationship'],
        'CBAP_MAINTAIN': ['maintain requirement', 'requirements maintenance', 'requirement change'],
        'CBAP_PRIORITIZE': ['prioritize', 'priority', 'moscow', 'ranking', 'prioritization'],
        'CBAP_ASSESS_CHANGES': ['assess change', 'impact analysis', 'change request', 'change impact'],
        'CBAP_APPROVE': ['approve requirement', 'requirements approval', 'sign-off', 'signoff'],
        'CBAP_SPECIFY': ['specify requirement', 'document requirement', 'use case', 'user story'],
        'CBAP_VERIFY': ['verify requirement', 'requirements verification', 'review requirement'],
        'CBAP_VALIDATE': ['validate requirement', 'requirements validation', 'acceptance criteria'],
        'CBAP_DEFINE_ARCH': ['requirements architecture', 'decomposition', 'requirements structure'],
        'CBAP_CURRENT_STATE': ['current state', 'as-is', 'current situation', 'baseline'],
        'CBAP_FUTURE_STATE': ['future state', 'to-be', 'target state', 'desired state'],
        'CBAP_ASSESS_RISK': ['risk assessment', 'identify risk', 'risk analysis', 'risk mitigation'],
        'CBAP_CHANGE_STRATEGY': ['change strategy', 'transition', 'implementation approach'],
        'CBAP_MEASURE_PERF': ['measure performance', 'solution performance', 'kpi', 'metrics'],
        'CBAP_ANALYZE_VALUE': ['analyze value', 'value realization', 'benefit analysis', 'roi'],
        'CBAP_ASSESS_LIMITS': ['solution limitation', 'constraint', 'gap analysis', 'assumption'],
        'CBAP_RECOMMEND': ['recommend action', 'recommendation', 'improvement action'],
    },

    'CCBA': {
        'CCBA_ELICITATION': ['elicitation', 'elicit', 'gather requirement', 'interview'],
        'CCBA_ANALYSIS': ['analysis', 'analyze', 'requirement analysis'],
        'CCBA_DOCUMENTATION': ['documentation', 'document', 'specification'],
        'CCBA_VALIDATION': ['validation', 'validate', 'verify'],
        'CCBA_STAKEHOLDER': ['stakeholder', 'stakeholder management'],
        'CCBA_SOLUTION': ['solution', 'solution assessment'],
        'CCBA_PLANNING': ['planning', 'ba planning', 'requirements planning'],
    },

    'ECBA': {
        'ECBA_FUNDAMENTALS': ['ba fundamentals', 'business analysis basics'],
        'ECBA_TECHNIQUES': ['ba technique', 'analysis technique'],
        'ECBA_COMPETENCIES': ['competency', 'skill', 'knowledge area'],
        'ECBA_PERSPECTIVE': ['perspective', 'viewpoint', 'stakeholder view'],
    }
}

# Question prefix to certification mapping
PREFIX_TO_CERT = {
    'PSM1': 'PSM_I', 'PSMI': 'PSM_I', 'PSM_I': 'PSM_I', 'PSM-I': 'PSM_I',
    'PSM2': 'PSM_II', 'PSMII': 'PSM_II', 'PSM_II': 'PSM_II', 'PSM-II': 'PSM_II',
    'PSPO1': 'PSPO_I', 'PSPOI': 'PSPO_I', 'PSPO_I': 'PSPO_I',
    'ISTQB': 'ISTQB_FOUNDATION', 'CTFL': 'ISTQB_FOUNDATION', 'CTAL': 'ISTQB_FOUNDATION',
    'AGILE': 'ISTQB_AGILE', 'ISTQB-AT': 'ISTQB_AGILE',
    'CBAP': 'CBAP', 'CCBA': 'CCBA', 'ECBA': 'ECBA',
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def detect_certification(title, question_text):
    """Detect certification from question content"""
    combined = f"{title} {question_text}".upper()

    for prefix, cert in PREFIX_TO_CERT.items():
        if prefix.upper() in combined:
            return cert

    # Content-based detection
    if any(kw in combined for kw in ['SCRUM MASTER', 'SPRINT', 'PRODUCT OWNER', 'SCRUM TEAM']):
        return 'PSM_I'
    if any(kw in combined for kw in ['TEST', 'DEFECT', 'BUG', 'QA', 'QUALITY']):
        return 'ISTQB_FOUNDATION'
    if any(kw in combined for kw in ['REQUIREMENT', 'STAKEHOLDER', 'ELICIT', 'BABOK']):
        return 'CBAP'

    return None

def get_skills_for_certification(cursor, cert_id):
    """Get all skills for a certification"""
    cursor.execute("""
        SELECT id, code, name
        FROM wp_ez_skills
        WHERE certification_id = %s AND status = 'active' AND level > 0
    """, (cert_id,))
    return cursor.fetchall()

def match_to_skills(combined_text, cert_id, skills):
    """Match question to skills"""
    if cert_id not in KEYWORD_MAPPINGS:
        return []

    keywords = KEYWORD_MAPPINGS[cert_id]
    text_lower = combined_text.lower()

    matches = []
    for skill in skills:
        skill_id, skill_code, skill_name = skill
        if skill_code in keywords:
            for kw in keywords[skill_code]:
                if kw.lower() in text_lower:
                    matches.append((skill_id, skill_code, 1.0))
                    break

    # Fallback: skill name matching
    if not matches:
        for skill in skills:
            skill_id, skill_code, skill_name = skill
            words = skill_name.lower().split()
            for word in words:
                if len(word) > 4 and word in text_lower:
                    matches.append((skill_id, skill_code, 0.5))
                    break

    return matches[:3]

def insert_mapping(cursor, question_id, skill_id, weight=1.0, confidence='high'):
    """Insert mapping if not exists"""
    cursor.execute("""
        SELECT id FROM wp_ez_question_skills
        WHERE question_id = %s AND skill_id = %s
    """, (question_id, skill_id))

    if cursor.fetchone():
        return False

    cursor.execute("""
        INSERT INTO wp_ez_question_skills
        (question_id, skill_id, weight, confidence, mapped_by, mapped_at)
        VALUES (%s, %s, %s, %s, 0, NOW())
    """, (question_id, skill_id, weight, confidence))
    return True

def main():
    conn = get_connection()
    cursor = conn.cursor()

    # Get unmapped questions
    cursor.execute("""
        SELECT q.id, q.title, q.question
        FROM wp_learndash_pro_quiz_question q
        LEFT JOIN wp_ez_question_skills qs ON q.id = qs.question_id
        WHERE q.online = 1 AND qs.id IS NULL
        ORDER BY q.id
    """)
    questions = cursor.fetchall()

    print(f"Found {len(questions)} unmapped questions")

    # Cache skills
    skills_cache = {}
    for cert_id in KEYWORD_MAPPINGS.keys():
        skills_cache[cert_id] = get_skills_for_certification(cursor, cert_id)

    stats = defaultdict(int)
    mappings_added = 0
    mapped_questions = 0

    try:
        for q_id, title, question_text in questions:
            # Detect certification
            cert_id = detect_certification(title or '', question_text or '')

            if not cert_id or cert_id not in skills_cache:
                stats['no_cert'] += 1
                continue

            skills = skills_cache[cert_id]
            if not skills:
                stats['no_skills'] += 1
                continue

            # Match to skills
            combined = f"{title} {question_text}"
            matches = match_to_skills(combined, cert_id, skills)

            if matches:
                for skill_id, skill_code, confidence in matches:
                    if insert_mapping(cursor, q_id, skill_id,
                                    weight=confidence,
                                    confidence='high' if confidence >= 0.8 else 'medium'):
                        mappings_added += 1
                mapped_questions += 1
                stats[cert_id] += 1
            else:
                # Map to root skill as fallback
                root = [s for s in skills if '_ROOT' in s[1]]
                if root:
                    if insert_mapping(cursor, q_id, root[0][0], weight=0.3, confidence='low'):
                        mappings_added += 1
                        mapped_questions += 1
                        stats[f'{cert_id}_fallback'] += 1

        conn.commit()

        print(f"\n=== Mapping Complete ===")
        print(f"Questions mapped: {mapped_questions}")
        print(f"Total new mappings: {mappings_added}")
        print(f"\nBy certification:")
        for key, count in sorted(stats.items()):
            print(f"  {key}: {count}")

        # Final stats
        cursor.execute('SELECT COUNT(*) FROM wp_learndash_pro_quiz_question WHERE online = 1')
        total = cursor.fetchone()[0]
        cursor.execute('SELECT COUNT(DISTINCT question_id) FROM wp_ez_question_skills')
        mapped = cursor.fetchone()[0]

        print(f"\n=== Final Stats ===")
        print(f"Total questions: {total}")
        print(f"Mapped questions: {mapped} ({mapped*100/total:.1f}%)")
        print(f"Unmapped: {total - mapped}")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
