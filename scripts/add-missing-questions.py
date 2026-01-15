#!/usr/bin/env python3
"""
Script to add questions for skills that have no questions mapped
"""
import mysql.connector

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Quiz ID for PSM1 Mock Test 01
PSM1_QUIZ_ID = 8

# New questions for missing skills
# Format: (skill_code, question_text, answers_with_correct, explanation)
NEW_QUESTIONS = [
    # PSM_SPRINT_SCOPE
    (
        "PSM_SPRINT_SCOPE",
        "During the Sprint, when can the scope of the Sprint Backlog be clarified or renegotiated?",
        [
            ("Never, the Sprint Backlog is fixed once Sprint Planning is complete", False),
            ("Only during the Daily Scrum", False),
            ("As more is learned, the scope can be clarified and renegotiated with the Product Owner", True),
            ("Only if the Scrum Master approves the change", False),
        ],
        "According to the Scrum Guide 2020, scope may be clarified and renegotiated with the Product Owner as more is learned. The Sprint Backlog is a living plan, not a fixed contract."
    ),
    (
        "PSM_SPRINT_SCOPE",
        "What happens if the Developers realize during the Sprint that they have selected more work than they can complete?",
        [
            ("The Sprint must be cancelled and restarted", False),
            ("The Developers must work overtime to complete all items", False),
            ("The scope is renegotiated with the Product Owner without affecting the Sprint Goal", True),
            ("The Scrum Master removes items from the Sprint Backlog", False),
        ],
        "When work turns out to be different than expected, the Developers collaborate with the Product Owner to negotiate the scope of the Sprint Backlog within the Sprint. The Sprint Goal should not be compromised."
    ),

    # PSM_PO_TRANSPARENCY
    (
        "PSM_PO_TRANSPARENCY",
        "How does the Product Owner ensure transparency of the Product Backlog?",
        [
            ("By keeping it private and only sharing with the Development Team", False),
            ("By making it visible, understood, and accessible to all stakeholders", True),
            ("By documenting it in a separate requirements document", False),
            ("By presenting it only during Sprint Reviews", False),
        ],
        "The Product Owner is responsible for ensuring the Product Backlog is transparent, visible, and understood. This is a key accountability of the Product Owner role."
    ),
    (
        "PSM_PO_TRANSPARENCY",
        "Why is Product Backlog transparency important in Scrum?",
        [
            ("It helps the organization track developer productivity", False),
            ("It enables inspection and adaptation based on accurate information", True),
            ("It satisfies project management requirements", False),
            ("It reduces the need for Sprint Reviews", False),
        ],
        "Transparency enables inspection. Without transparency, the empirical pillars of Scrum cannot function properly, making it impossible to make informed decisions."
    ),

    # PSM_REVIEW_ADAPT
    (
        "PSM_REVIEW_ADAPT",
        "What typically happens to the Product Backlog as a result of the Sprint Review?",
        [
            ("It remains unchanged until the next Sprint Planning", False),
            ("It may be adjusted to reflect new opportunities and learning", True),
            ("It is completely rewritten by the Product Owner", False),
            ("It is frozen until the product is released", False),
        ],
        "During Sprint Review, the entire group collaborates on what to do next. The Product Backlog may be adjusted to meet new opportunities, and this feeds into Sprint Planning."
    ),
    (
        "PSM_REVIEW_ADAPT",
        "During the Sprint Review, who can suggest adjustments to the Product Backlog?",
        [
            ("Only the Product Owner", False),
            ("Only the Scrum Team", False),
            ("The Scrum Team and key stakeholders collaborate on adjustments", True),
            ("Only the Scrum Master", False),
        ],
        "The Sprint Review is a collaborative working session. The Scrum Team and stakeholders review what was accomplished and what has changed, then collaborate on what to do next."
    ),

    # PSM_DEV_DAILY_ADAPT
    (
        "PSM_DEV_DAILY_ADAPT",
        "How often should Developers adapt their plan toward the Sprint Goal?",
        [
            ("Only during the Daily Scrum", False),
            ("Daily, and whenever necessary", True),
            ("Only when the Scrum Master asks them to", False),
            ("At the end of each Sprint", False),
        ],
        "Developers adapt their plan each day toward the Sprint Goal. The Daily Scrum is one opportunity for this, but adaptation should happen throughout the day as needed."
    ),
    (
        "PSM_DEV_DAILY_ADAPT",
        "What is the purpose of the Developers adapting their plan daily?",
        [
            ("To report progress to the Product Owner", False),
            ("To optimize the probability of achieving the Sprint Goal", True),
            ("To ensure the Scrum Master knows what everyone is doing", False),
            ("To update the Sprint Burndown chart", False),
        ],
        "The purpose of daily adaptation is to optimize the probability of achieving the Sprint Goal. This is the core reason for frequent inspection and adaptation."
    ),

    # PSM_DAILY_PLAN
    (
        "PSM_DAILY_PLAN",
        "What should the Developers produce during or after the Daily Scrum?",
        [
            ("A detailed status report for management", False),
            ("An actionable plan for the next day of work", True),
            ("Updated estimates for all remaining work", False),
            ("A list of completed tasks for the Sprint Burndown", False),
        ],
        "The Daily Scrum produces an actionable plan for the next day of work. This is how the Developers coordinate their work and optimize their chances of achieving the Sprint Goal."
    ),
    (
        "PSM_DAILY_PLAN",
        "Who is responsible for creating the plan for the day's work during the Daily Scrum?",
        [
            ("The Scrum Master", False),
            ("The Product Owner", False),
            ("The Developers", True),
            ("The team lead or senior developer", False),
        ],
        "The Developers are responsible for creating their plan. Self-management means the Developers decide how to accomplish their work, including daily planning."
    ),

    # PSM_PLANNING_WHY
    (
        "PSM_PLANNING_WHY",
        "What is discussed during the 'Why is this Sprint valuable?' topic in Sprint Planning?",
        [
            ("Technical implementation details", False),
            ("How the Sprint could increase the product's value and utility", True),
            ("Which developers will work on which items", False),
            ("Detailed task breakdown for each Product Backlog item", False),
        ],
        "The Product Owner proposes how the product could increase its value and utility in the current Sprint. The whole Scrum Team then collaborates to define a Sprint Goal that communicates why the Sprint is valuable."
    ),
    (
        "PSM_PLANNING_WHY",
        "Who proposes how the Sprint could increase the product's value?",
        [
            ("The Scrum Master", False),
            ("The Product Owner", True),
            ("The Developers", False),
            ("The stakeholders", False),
        ],
        "The Product Owner proposes how the product could increase its value and utility in the current Sprint. This is the starting point for defining why the Sprint is valuable."
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

    return f'a:{len(answers)}:{{{";".join(parts)}}}'

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

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
        sort_order = get_next_sort_order(cursor, PSM1_QUIZ_ID)
        questions_added = 0
        mappings_added = 0

        for skill_code, question_text, answers, explanation in NEW_QUESTIONS:
            # Get skill ID
            skill_id = get_skill_id(cursor, skill_code)
            if not skill_id:
                print(f"Warning: Skill {skill_code} not found, skipping...")
                continue

            # Create title
            title = f"PSM1_NEW_{skill_code}_{questions_added + 1}"

            # Create answer data
            answer_data = create_answer_data(answers)

            # Format question with HTML
            formatted_question = f"<p>{question_text}</p>"

            # Insert question
            question_id = insert_question(
                cursor, PSM1_QUIZ_ID, title, formatted_question,
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
