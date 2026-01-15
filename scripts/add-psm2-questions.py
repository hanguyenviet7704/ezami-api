#!/usr/bin/env python3
"""
Script to add questions for PSM_II skills that have no questions mapped
"""
import mysql.connector

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Quiz ID for PSM2 quiz (will be determined dynamically)
PSM2_QUIZ_ID = None

# New questions for missing PSM_II skills
NEW_QUESTIONS = [
    # PSM2_TEAM_COACH
    (
        "PSM2_TEAM_COACH",
        "What is the primary focus of a Scrum Master when coaching an entire Scrum Team?",
        [
            ("Ensuring individual team members complete their tasks on time", False),
            ("Helping the team improve their collective effectiveness and self-management", True),
            ("Making decisions for the team when they cannot agree", False),
            ("Reporting team progress to senior management", False),
        ],
        "A Scrum Master coaches the entire team to improve their collective effectiveness, self-management, and cross-functionality. This is different from coaching individuals and focuses on team dynamics and performance."
    ),
    (
        "PSM2_TEAM_COACH",
        "When coaching a Scrum Team, a Scrum Master notices the team always defers to the most senior developer for decisions. What coaching approach would be most effective?",
        [
            ("Tell the team they must make decisions democratically", False),
            ("Remove the senior developer from discussions", False),
            ("Facilitate exercises that help the team practice shared decision-making", True),
            ("Assign decision-making authority to different team members each Sprint", False),
        ],
        "Effective team coaching involves facilitating activities that help the team develop new behaviors and skills, rather than imposing rules or restructuring artificially."
    ),

    # PSM2_SERVANT_LEADER
    (
        "PSM2_SERVANT_LEADER",
        "Which behavior best exemplifies servant leadership for a Scrum Master?",
        [
            ("Making decisions quickly so the team can focus on development", False),
            ("Protecting the team from all external interruptions", False),
            ("Helping team members grow and perform at their highest potential", True),
            ("Taking responsibility for the team's commitments", False),
        ],
        "Servant leadership is about helping others grow and succeed. A servant leader prioritizes the needs and development of team members, empowering them rather than directing them."
    ),
    (
        "PSM2_SERVANT_LEADER",
        "How does a Scrum Master demonstrate servant leadership when the team is struggling with a technical problem?",
        [
            ("Solve the problem for the team to maintain velocity", False),
            ("Hire external consultants to fix the issue", False),
            ("Help the team identify what support they need and facilitate obtaining it", True),
            ("Extend the Sprint to give the team more time", False),
        ],
        "A servant leader supports the team by helping them identify their needs and facilitating access to resources, rather than solving problems for them or making unilateral decisions."
    ),
]

def create_answer_data(answers):
    """Create serialized PHP answer data format"""
    parts = []
    for i, (answer_text, is_correct) in enumerate(answers):
        correct_val = "b:1" if is_correct else "b:0"
        answer_escaped = answer_text.replace('"', '\\"')
        part = f'i:{i};O:27:"WpProQuiz_Model_AnswerTypes":10:{{s:10:"\\0*\\0_answer";s:{len(answer_text)}:"{answer_escaped}";s:8:"\\0*\\0_html";b:0;s:10:"\\0*\\0_points";i:0;s:11:"\\0*\\0_correct";{correct_val};s:14:"\\0*\\0_sortString";s:0:"";s:18:"\\0*\\0_sortStringHtml";b:0;s:10:"\\0*\\0_graded";s:1:"1";s:22:"\\0*\\0_gradingProgression";s:15:"not-graded-none";s:14:"\\0*\\0_gradedType";s:4:"text";s:10:"\\0*\\0_mapper";N;}}'
        parts.append(part)

    return f'a:{len(answers)}:{{{"".join(parts)}}}'

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def get_psm2_quiz_id(cursor):
    """Get quiz ID for PSM2"""
    cursor.execute("""
        SELECT id FROM wp_learndash_pro_quiz_master
        WHERE name LIKE '%PSM%II%' OR name LIKE '%PSM2%' OR name LIKE '%PSM-II%'
        LIMIT 1
    """)
    result = cursor.fetchone()
    if result:
        return result[0]

    # If no PSM2 quiz, use PSM1 quiz
    cursor.execute("""
        SELECT id FROM wp_learndash_pro_quiz_master
        WHERE name LIKE '%PSM%' OR name LIKE '%Scrum Master%'
        LIMIT 1
    """)
    result = cursor.fetchone()
    return result[0] if result else 8  # Fallback to ID 8

def get_next_sort_order(cursor, quiz_id):
    """Get next sort order for questions in quiz"""
    cursor.execute("SELECT MAX(sort) FROM wp_learndash_pro_quiz_question WHERE quiz_id = %s", (quiz_id,))
    result = cursor.fetchone()
    return (result[0] or 0) + 1

def get_skill_id(cursor, skill_code):
    """Get skill ID by code"""
    cursor.execute("SELECT id FROM wp_ez_skills WHERE code = %s", (skill_code,))
    result = cursor.fetchone()
    return result[0] if result else None

def insert_question(cursor, quiz_id, title, question_text, answer_data, correct_msg, sort_order):
    """Insert a new question"""
    sql = """
    INSERT INTO wp_learndash_pro_quiz_question
    (quiz_id, online, previous_id, sort, title, points, question, correct_msg, incorrect_msg,
     correct_same_text, tip_enabled, tip_msg, answer_type, show_points_in_box,
     answer_points_activated, answer_data, category_id, answer_points_diff_modus_activated,
     disable_correct, matrix_sort_answer_criteria_width)
    VALUES (%s, 1, 0, %s, %s, 1, %s, %s, '', 0, 0, '', 'single', 0, 0, %s, 0, 0, 0, 0)
    """
    cursor.execute(sql, (quiz_id, sort_order, title, question_text, correct_msg, answer_data))
    return cursor.lastrowid

def insert_mapping(cursor, question_id, skill_id):
    """Insert question-skill mapping"""
    sql = """
    INSERT INTO wp_ez_question_skills
    (question_id, skill_id, weight, confidence, mapped_by, mapped_at)
    VALUES (%s, %s, 1.0, 'high', 0, NOW())
    """
    cursor.execute(sql, (question_id, skill_id))

def main():
    conn = get_connection()
    cursor = conn.cursor()

    try:
        # Get quiz ID
        quiz_id = get_psm2_quiz_id(cursor)
        print(f"Using quiz ID: {quiz_id}")

        sort_order = get_next_sort_order(cursor, quiz_id)
        questions_added = 0
        mappings_added = 0

        for skill_code, question_text, answers, explanation in NEW_QUESTIONS:
            # Get skill ID
            skill_id = get_skill_id(cursor, skill_code)
            if not skill_id:
                print(f"Warning: Skill {skill_code} not found, skipping...")
                continue

            # Create title
            title = f"PSM2_NEW_{skill_code}_{questions_added + 1}"

            # Create answer data
            answer_data = create_answer_data(answers)

            # Format question with HTML
            formatted_question = f"<p>{question_text}</p>"

            # Insert question
            question_id = insert_question(
                cursor, quiz_id, title, formatted_question,
                answer_data, explanation, sort_order
            )
            questions_added += 1
            sort_order += 1

            # Insert mapping
            insert_mapping(cursor, question_id, skill_id)
            mappings_added += 1

            print(f"Added question for {skill_code}: {title}")

        conn.commit()
        print(f"\n=== Added {questions_added} questions and {mappings_added} mappings ===")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
