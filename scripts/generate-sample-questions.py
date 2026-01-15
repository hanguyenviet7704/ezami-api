#!/usr/bin/env python3
"""
Generate sample questions for new certificate categories.
Questions are generated in LearnDash ProQuiz format.

Usage:
    python3 scripts/generate-sample-questions.py

Requirements:
    pip install mysql-connector-python
"""

import mysql.connector
import json
from datetime import datetime
from typing import List, Dict, Any

DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

# Certificate definitions with sample questions
CERTIFICATE_TEMPLATES = {
    # Developer Group
    'DEV_GOLANG': {
        'name': 'Go/Golang Developer',
        'skills': [
            ('GO_BASICS', 'Go Basics', 'Go programming fundamentals including syntax, types, and control structures'),
            ('GO_CONCURRENCY', 'Concurrency in Go', 'Goroutines, channels, and concurrent programming patterns'),
            ('GO_PACKAGES', 'Go Packages', 'Package management, modules, and dependency handling'),
            ('GO_TESTING', 'Go Testing', 'Testing strategies, benchmarking, and test coverage'),
            ('GO_INTERFACES', 'Go Interfaces', 'Interface implementation and polymorphism in Go'),
        ],
        'sample_questions': [
            {
                'skill_code': 'GO_BASICS',
                'question': 'What is the correct way to declare a variable in Go?',
                'answers': [
                    {'text': 'var x int = 10', 'correct': True},
                    {'text': 'int x = 10', 'correct': False},
                    {'text': 'x := int(10)', 'correct': False},
                    {'text': 'declare x as int = 10', 'correct': False},
                ],
                'explanation': 'In Go, you can declare variables using "var x int = 10" or the short declaration "x := 10".',
            },
            {
                'skill_code': 'GO_CONCURRENCY',
                'question': 'Which keyword is used to start a goroutine in Go?',
                'answers': [
                    {'text': 'go', 'correct': True},
                    {'text': 'async', 'correct': False},
                    {'text': 'concurrent', 'correct': False},
                    {'text': 'thread', 'correct': False},
                ],
                'explanation': 'The "go" keyword is used to start a new goroutine, which is a lightweight thread managed by the Go runtime.',
            },
        ]
    },

    'DEV_SYSTEM_DESIGN': {
        'name': 'System Design',
        'skills': [
            ('SD_SCALABILITY', 'Scalability', 'Horizontal and vertical scaling strategies'),
            ('SD_LOAD_BALANCING', 'Load Balancing', 'Load distribution and traffic management'),
            ('SD_CACHING', 'Caching Strategies', 'Caching patterns and cache invalidation'),
            ('SD_DATABASE', 'Database Design', 'SQL vs NoSQL, sharding, and replication'),
            ('SD_MICROSERVICES', 'Microservices', 'Microservices architecture and communication patterns'),
        ],
        'sample_questions': [
            {
                'skill_code': 'SD_SCALABILITY',
                'question': 'What is the main difference between horizontal and vertical scaling?',
                'answers': [
                    {'text': 'Horizontal scaling adds more machines, vertical scaling adds more power to existing machines', 'correct': True},
                    {'text': 'Horizontal scaling is cheaper than vertical scaling', 'correct': False},
                    {'text': 'Vertical scaling requires load balancers', 'correct': False},
                    {'text': 'Horizontal scaling is limited by hardware constraints', 'correct': False},
                ],
                'explanation': 'Horizontal scaling (scaling out) adds more servers, while vertical scaling (scaling up) increases the resources of existing servers.',
            },
        ]
    },

    # AWS Group
    'AWS_SAA_C03': {
        'name': 'AWS Solutions Architect Associate (SAA-C03)',
        'skills': [
            ('AWS_COMPUTE', 'AWS Compute Services', 'EC2, Lambda, ECS, and compute options'),
            ('AWS_STORAGE', 'AWS Storage Services', 'S3, EBS, EFS, and storage solutions'),
            ('AWS_NETWORKING', 'AWS Networking', 'VPC, Route 53, CloudFront, and networking concepts'),
            ('AWS_SECURITY', 'AWS Security', 'IAM, KMS, security groups, and compliance'),
            ('AWS_DATABASE', 'AWS Database Services', 'RDS, DynamoDB, Aurora, and database solutions'),
        ],
        'sample_questions': [
            {
                'skill_code': 'AWS_COMPUTE',
                'question': 'Which AWS service provides serverless compute capabilities?',
                'answers': [
                    {'text': 'AWS Lambda', 'correct': True},
                    {'text': 'Amazon EC2', 'correct': False},
                    {'text': 'Amazon ECS', 'correct': False},
                    {'text': 'AWS Batch', 'correct': False},
                ],
                'explanation': 'AWS Lambda is a serverless compute service that runs code in response to events without requiring server management.',
            },
        ]
    },

    # Kubernetes Group
    'KUBERNETES_CKA': {
        'name': 'Certified Kubernetes Administrator',
        'skills': [
            ('K8S_ARCHITECTURE', 'Kubernetes Architecture', 'Control plane, worker nodes, and cluster components'),
            ('K8S_WORKLOADS', 'Kubernetes Workloads', 'Deployments, StatefulSets, DaemonSets, and Jobs'),
            ('K8S_NETWORKING', 'Kubernetes Networking', 'Services, Ingress, and network policies'),
            ('K8S_STORAGE', 'Kubernetes Storage', 'Persistent Volumes, Storage Classes, and CSI'),
            ('K8S_SECURITY', 'Kubernetes Security', 'RBAC, security contexts, and network policies'),
        ],
        'sample_questions': [
            {
                'skill_code': 'K8S_ARCHITECTURE',
                'question': 'Which component is responsible for storing cluster state in Kubernetes?',
                'answers': [
                    {'text': 'etcd', 'correct': True},
                    {'text': 'kube-apiserver', 'correct': False},
                    {'text': 'kube-scheduler', 'correct': False},
                    {'text': 'kube-controller-manager', 'correct': False},
                ],
                'explanation': 'etcd is a distributed key-value store that stores all cluster state and configuration data.',
            },
        ]
    },

    # Security Group
    'COMPTIA_SECURITY_PLUS': {
        'name': 'CompTIA Security+',
        'skills': [
            ('SEC_THREATS', 'Threats and Vulnerabilities', 'Malware, social engineering, and attack types'),
            ('SEC_CRYPTOGRAPHY', 'Cryptography', 'Encryption, hashing, and PKI'),
            ('SEC_IAM', 'Identity and Access Management', 'Authentication, authorization, and access control'),
            ('SEC_NETWORK', 'Network Security', 'Firewalls, IDS/IPS, and network segmentation'),
            ('SEC_COMPLIANCE', 'Risk and Compliance', 'Risk assessment, security policies, and compliance frameworks'),
        ],
        'sample_questions': [
            {
                'skill_code': 'SEC_CRYPTOGRAPHY',
                'question': 'Which encryption type uses the same key for encryption and decryption?',
                'answers': [
                    {'text': 'Symmetric encryption', 'correct': True},
                    {'text': 'Asymmetric encryption', 'correct': False},
                    {'text': 'Hashing', 'correct': False},
                    {'text': 'Digital signatures', 'correct': False},
                ],
                'explanation': 'Symmetric encryption uses a single shared key for both encryption and decryption, making it faster but requiring secure key distribution.',
            },
        ]
    },

    # ISTQB Group
    'ISTQB_FOUNDATION_V4_0': {
        'name': 'ISTQB Foundation Level v4.0',
        'skills': [
            ('ISTQB4_FUNDAMENTALS', 'Testing Fundamentals', 'What is testing, why testing is necessary, and testing principles'),
            ('ISTQB4_LIFECYCLE', 'Testing in SDLC', 'Test levels, test types, and testing in different SDLC models'),
            ('ISTQB4_STATIC', 'Static Testing', 'Reviews, static analysis, and review types'),
            ('ISTQB4_TECHNIQUES', 'Test Techniques', 'Black-box, white-box, and experience-based techniques'),
            ('ISTQB4_MANAGEMENT', 'Test Management', 'Test planning, monitoring, and configuration management'),
        ],
        'sample_questions': [
            {
                'skill_code': 'ISTQB4_FUNDAMENTALS',
                'question': 'According to ISTQB v4.0, which is NOT one of the seven testing principles?',
                'answers': [
                    {'text': 'Complete testing is always possible', 'correct': True},
                    {'text': 'Testing shows the presence of defects', 'correct': False},
                    {'text': 'Early testing saves time and money', 'correct': False},
                    {'text': 'Defects cluster together', 'correct': False},
                ],
                'explanation': 'Exhaustive testing is NOT possible according to ISTQB principles. Testing shows presence of defects, not their absence.',
            },
        ]
    },

    # Agile/Scrum Group
    'AGILE_SCRUM_MASTER': {
        'name': 'Agile Scrum Master Certification',
        'skills': [
            ('ASM_AGILE_VALUES', 'Agile Values and Principles', 'Agile Manifesto values and 12 principles'),
            ('ASM_SCRUM_FRAMEWORK', 'Scrum Framework', 'Roles, events, and artifacts in Scrum'),
            ('ASM_FACILITATION', 'Facilitation Skills', 'Meeting facilitation and conflict resolution'),
            ('ASM_COACHING', 'Team Coaching', 'Coaching techniques and servant leadership'),
            ('ASM_SCALING', 'Scaling Agile', 'SAFe, LeSS, and scaling frameworks'),
        ],
        'sample_questions': [
            {
                'skill_code': 'ASM_AGILE_VALUES',
                'question': 'Which statement best represents an Agile Manifesto value?',
                'answers': [
                    {'text': 'Individuals and interactions over processes and tools', 'correct': True},
                    {'text': 'Comprehensive documentation over working software', 'correct': False},
                    {'text': 'Contract negotiation over customer collaboration', 'correct': False},
                    {'text': 'Following a plan over responding to change', 'correct': False},
                ],
                'explanation': 'The Agile Manifesto values individuals and interactions over processes and tools, working software over documentation, customer collaboration over contracts, and responding to change over following a plan.',
            },
        ]
    },

    # PMI PMP
    'PMI_PMP': {
        'name': 'PMI Project Management Professional',
        'skills': [
            ('PMP_PEOPLE', 'People Domain', 'Team building, leadership, and stakeholder management'),
            ('PMP_PROCESS', 'Process Domain', 'Project planning, executing, and monitoring'),
            ('PMP_BUSINESS', 'Business Environment', 'Benefits realization and project alignment'),
            ('PMP_PREDICTIVE', 'Predictive Approaches', 'Waterfall and plan-driven methodologies'),
            ('PMP_AGILE', 'Agile/Hybrid', 'Agile methodologies and hybrid approaches'),
        ],
        'sample_questions': [
            {
                'skill_code': 'PMP_PEOPLE',
                'question': 'What is the primary role of a project manager in building high-performing teams?',
                'answers': [
                    {'text': 'Servant leadership and removing impediments', 'correct': True},
                    {'text': 'Micromanaging team tasks', 'correct': False},
                    {'text': 'Making all technical decisions', 'correct': False},
                    {'text': 'Reporting to stakeholders only', 'correct': False},
                ],
                'explanation': 'Modern project management emphasizes servant leadership, where the PM removes obstacles and empowers the team.',
            },
        ]
    },
}


def get_connection():
    """Get database connection"""
    return mysql.connector.connect(**DB_CONFIG)


def create_quiz_category(cursor, category_name: str) -> int:
    """Create or get quiz category"""
    # Check if exists
    cursor.execute(
        "SELECT category_id FROM wp_learndash_pro_quiz_category WHERE category_name = %s",
        (category_name,)
    )
    result = cursor.fetchone()
    if result:
        return result[0]

    # Create new
    cursor.execute(
        "INSERT INTO wp_learndash_pro_quiz_category (category_name) VALUES (%s)",
        (category_name,)
    )
    return cursor.lastrowid


def create_quiz(cursor, quiz_name: str, category_id: int) -> int:
    """Create a new quiz"""
    cursor.execute("""
        INSERT INTO wp_learndash_pro_quiz_master
        (name, text, result_text, btn_restart_quiz_hidden, btn_view_question_hidden,
         questions_per_page, sort_categories, show_max_question, show_max_question_value,
         show_max_question_percent, toplist_activated, toplist_data, show_average_result,
         prerequisite, toplist_data_add_auto_delete, show_category_score, hide_result_correct_question,
         hide_result_quiz_time, hide_result_points, auto_start, forcing_question_solve,
         hide_question_position_overview, hide_question_numbering, form_activated, form_show_position,
         start_only_registered_user, questions_per_page_enabled, statistic_on, hide_answer_message_box,
         disabled_answer_mark, show_max_question_value_edit, short_code_embed, time_limit, time_limit_cookie,
         form_data_hide, category_filter, category_filter_type, sort_random, form_data_random,
         custom_fields_include, category_point_activated, inline_answers, hide_quiz_result,
         answer_random, disable_mce, specific_result_text, min_max_points, end_hint,
         share_enabled, total_points_enabled)
        VALUES
        (%s, '', '', 0, 0,
         0, 0, 0, 0,
         0, 0, '', 0,
         0, 0, 0, 0,
         0, 0, 0, 0,
         0, 0, 0, 0,
         0, 0, 1, 0,
         0, 0, 0, 0, 0,
         0, 0, 0, 0, 0,
         '', 0, 0, 0,
         0, 0, 0, '', 0,
         0, 0)
    """, (quiz_name,))
    return cursor.lastrowid


def serialize_answer_data(answers: List[Dict]) -> str:
    """Serialize answers to WpProQuiz format"""
    parts = []
    for i, ans in enumerate(answers):
        # WpProQuiz serialized format
        answer_text = ans['text']
        is_correct = 'b:1' if ans['correct'] else 'b:0'
        points = 1 if ans['correct'] else 0

        part = f'i:{i};O:27:"WpProQuiz_Model_AnswerTypes":10:{{' \
               f's:10:"\x00*\x00_answer";s:{len(answer_text)}:"{answer_text}";' \
               f's:8:"\x00*\x00_html";b:0;' \
               f's:10:"\x00*\x00_points";i:{points};' \
               f's:11:"\x00*\x00_correct";{is_correct};' \
               f's:14:"\x00*\x00_sortString";s:0:"";' \
               f's:18:"\x00*\x00_sortStringHtml";b:0;' \
               f's:10:"\x00*\x00_graded";s:1:"1";' \
               f's:22:"\x00*\x00_gradingProgression";s:15:"not-graded-none";' \
               f's:14:"\x00*\x00_gradedType";s:4:"text";' \
               f's:21:"\x00*\x00_gradedAnswerData";N;}}'
        parts.append(part)

    return f'a:{len(answers)}:{{{";".join(parts)}}}'


def insert_question(cursor, quiz_id: int, category_id: int, question: Dict, sort_order: int) -> int:
    """Insert a question into the quiz"""
    answer_data = serialize_answer_data(question['answers'])

    correct_msg = question.get('explanation', 'Correct!')
    incorrect_msg = question.get('explanation', 'Incorrect. ' + question.get('explanation', ''))

    cursor.execute("""
        INSERT INTO wp_learndash_pro_quiz_question
        (quiz_id, online, previous_id, sort, title, points, question,
         correct_msg, incorrect_msg, correct_same_text, tip_enabled, tip_msg,
         answer_type, show_points_in_box, answer_points_activated, answer_data,
         category_id, answer_points_diff_modus_activated, disable_correct,
         matrix_sort_answer_criteria_width)
        VALUES
        (%s, 1, 0, %s, %s, 1, %s,
         %s, %s, 0, 0, '',
         'single', 0, 0, %s,
         %s, 0, 0, 0)
    """, (
        quiz_id,
        sort_order,
        f"{question['skill_code']}_Q{sort_order}",
        question['question'],
        correct_msg,
        incorrect_msg,
        answer_data,
        category_id
    ))

    return cursor.lastrowid


def create_wp_post_for_question(cursor, question_id: int, quiz_id: int, title: str) -> int:
    """Create WordPress post for the question"""
    cursor.execute("""
        INSERT INTO wp_posts
        (post_author, post_date, post_date_gmt, post_content, post_title,
         post_excerpt, post_status, comment_status, ping_status, post_password,
         post_name, to_ping, pinged, post_modified, post_modified_gmt,
         post_content_filtered, post_parent, guid, menu_order, post_type,
         post_mime_type, comment_count)
        VALUES
        (1, NOW(), NOW(), '', %s,
         '', 'publish', 'closed', 'closed', '',
         %s, '', '', NOW(), NOW(),
         '', 0, '', 0, 'sfwd-question',
         '', 0)
    """, (title, title.lower().replace(' ', '-')))

    post_id = cursor.lastrowid

    # Add post meta
    cursor.execute("""
        INSERT INTO wp_postmeta (post_id, meta_key, meta_value)
        VALUES (%s, 'question_pro_id', %s)
    """, (post_id, question_id))

    cursor.execute("""
        INSERT INTO wp_postmeta (post_id, meta_key, meta_value)
        VALUES (%s, 'quiz_id', %s)
    """, (post_id, quiz_id))

    cursor.execute("""
        INSERT INTO wp_postmeta (post_id, meta_key, meta_value)
        VALUES (%s, 'question_type', 'single')
    """, (post_id,))

    return post_id


def generate_questions_for_certificate(conn, cert_code: str, template: Dict):
    """Generate all questions for a certificate"""
    cursor = conn.cursor()

    print(f"\n=== Processing: {cert_code} - {template['name']} ===")

    # Create category
    category_id = create_quiz_category(cursor, cert_code)
    print(f"  Category ID: {category_id}")

    # Create quiz
    quiz_name = f"{template['name']} - Sample Questions"
    quiz_id = create_quiz(cursor, quiz_name, category_id)
    print(f"  Quiz ID: {quiz_id}")

    # Insert questions
    question_count = 0
    for idx, question in enumerate(template.get('sample_questions', [])):
        q_id = insert_question(cursor, quiz_id, category_id, question, idx + 1)

        # Create WP post
        title = f"{cert_code}_Q{idx + 1}"
        post_id = create_wp_post_for_question(cursor, q_id, quiz_id, title)

        question_count += 1
        print(f"    Created question: {title} (ID: {q_id}, Post: {post_id})")

    conn.commit()
    print(f"  Total questions created: {question_count}")

    return question_count


def main():
    """Main function"""
    print("=" * 60)
    print("Sample Question Generator for New Certificates")
    print("=" * 60)

    conn = get_connection()

    try:
        total_questions = 0

        for cert_code, template in CERTIFICATE_TEMPLATES.items():
            count = generate_questions_for_certificate(conn, cert_code, template)
            total_questions += count

        print("\n" + "=" * 60)
        print(f"COMPLETE! Total questions generated: {total_questions}")
        print("=" * 60)

    except Exception as e:
        conn.rollback()
        print(f"\nError: {e}")
        raise
    finally:
        conn.close()


if __name__ == '__main__':
    main()
