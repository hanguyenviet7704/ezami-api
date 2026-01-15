#!/usr/bin/env python3
"""
Import wp_ez_diagnostic_questions to wp_learndash_pro_quiz_question
Converts clean JSON format to PHP serialized format
"""

import pymysql
import json
import os
from dotenv import load_dotenv

# Category code to category_id mapping
CATEGORY_MAPPING = {
    'PSM_I': 1, 'ISTQB_CTFL': 25, 'CCBA': 14, 'CBAP': 26, 'ECBA': 15,
    'SCRUM_PSPO_I': 11, 'SCRUM_PSM_II': 12, 'ISTQB_AGILE': 28, 'ISTQB_AI': 29,
    'AWS_SAA_C03': 30, 'AWS_DVA_C02': 31, 'AWS_SAP_C02': 56, 'AWS_DOP_C02': 55,
    'AZURE_AZ104': 33, 'KUBERNETES_CKA': 34, 'KUBERNETES_CKAD': 57,
    'DOCKER_DCA': 36, 'HASHICORP_TERRAFORM': 37, 'JAVA_OCP_17': 38,
    'VMWARE_SPRING_PRO': 39, 'COMPTIA_SECURITY_PLUS': 40, 'ISC2_CISSP': 41,
    'DEV_GOLANG': 42, 'DEV_SYSTEM_DESIGN': 43, 'DEV_SQL_DATABASE': 44,
    'DEV_FRONTEND': 45, 'DEV_DEVOPS': 46, 'DEV_BACKEND': 47,
    'DEV_PYTHON': 48, 'DEV_API_DESIGN': 49, 'DEV_SOFTWARE_ARCH': 50,
    'DEV_NODEJS': 51, 'DEV_REACT': 52, 'DEV_JAVASCRIPT_TS': 53,
    'GCP_ACE': 54, 'AGILE_SCRUM_MASTER': 58, 'PMI_PMP': 59
}

class EzToLearnDashImporter:
    """Import wp_ez_diagnostic_questions to LearnDash format"""

    def __init__(self):
        load_dotenv()
        self.connection = None
        self.connect_db()

    def connect_db(self):
        """Connect to MySQL database"""
        try:
            self.connection = pymysql.connect(
                host=os.getenv('DB_HOST', 'localhost'),
                port=int(os.getenv('DB_PORT', '3306')),
                user=os.getenv('DB_USER', 'root'),
                password=os.getenv('DB_PASS', '12345678aA@'),
                database=os.getenv('DB_NAME', 'wordpress'),
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            print("✅ Connected to database")
        except Exception as e:
            print(f"❌ Database connection failed: {e}")
            exit(1)

    def convert_to_php_serialized(self, options_json_str, correct_answer_json_str):
        """Convert JSON options + correct answers to PHP serialized format"""
        try:
            options = json.loads(options_json_str)
            correct_answers = json.loads(correct_answer_json_str)

            # Build PHP serialized answer_data
            result = f"a:{len(options)}:{{"

            for idx, option in enumerate(options):
                key = option.get('key', chr(65 + idx))  # A, B, C, D
                text = option.get('text', '')
                is_correct = 1 if key in correct_answers else 0

                # Serialize answer object with private properties
                answer_obj = (
                    f"i:{idx};"
                    f'O:27:"WpProQuiz_Model_AnswerTypes":10:{{'
                    f's:10:"\\0*\\0_mapper";N;'
                    f's:10:"\\0*\\0_answer";s:{len(text)}:"{text}";'
                    f's:8:"\\0*\\0_html";b:0;'
                    f's:10:"\\0*\\0_points";i:{1 if is_correct else 0};'
                    f's:11:"\\0*\\0_correct";b:{is_correct};'
                    f's:14:"\\0*\\0_sortString";s:0:"";'
                    f's:18:"\\0*\\0_sortStringHtml";b:0;'
                    f's:10:"\\0*\\0_graded";s:1:"1";'
                    f's:22:"\\0*\\0_gradingProgression";s:15:"not-graded-none";'
                    f's:14:"\\0*\\0_gradedType";s:4:"text";'
                    f"}}"
                )
                result += answer_obj

            result += "}"
            return result

        except Exception as e:
            print(f"Error converting: {e}")
            return None

    def import_questions(self, dry_run=True, limit=None, categories=None):
        """Import wp_ez questions to LearnDash"""
        cursor = self.connection.cursor()

        # Build query
        query = """
            SELECT id, category_code, skill_id, skill_code, skill_name,
                   question_text, question_type, options_json, correct_answer_json,
                   difficulty, explanation, language_code
            FROM wp_ez_diagnostic_questions
            WHERE status = 'active'
              AND language_code = 'en'
        """

        if categories:
            placeholders = ','.join(['%s'] * len(categories))
            query += f" AND category_code IN ({placeholders})"

        query += " ORDER BY category_code, id"

        if limit:
            query += f" LIMIT {limit}"

        if categories:
            cursor.execute(query, categories)
        else:
            cursor.execute(query)

        questions = cursor.fetchall()
        total = len(questions)

        print(f"\n{'='*70}")
        print(f"Importing {total} questions from wp_ez_diagnostic_questions")
        print(f"Mode: {'DRY RUN' if dry_run else 'LIVE IMPORT'}")
        print(f"{'='*70}\n")

        imported = 0
        skipped = 0
        failed = 0

        for q in questions:
            category_id = CATEGORY_MAPPING.get(q['category_code'])

            if not category_id:
                print(f"⚠️  [{q['category_code']}] No category mapping, skipping...")
                skipped += 1
                continue

            # Convert JSON to PHP serialized
            answer_data = self.convert_to_php_serialized(
                q['options_json'],
                q['correct_answer_json']
            )

            if not answer_data:
                failed += 1
                continue

            # Determine answer_type
            answer_type = 'multiple' if q['question_type'] == 'multiple_choice' else 'single'

            # Create title
            title = f"EZ_{q['category_code']}_{q['id']}"

            # Show progress
            if imported < 5 or (imported + skipped + failed) % 500 == 0:
                print(f"[{imported + skipped + failed}/{total}] {title} → category {category_id}")

            if not dry_run:
                try:
                    insert_cursor = self.connection.cursor()
                    insert_cursor.execute("""
                        INSERT INTO wp_learndash_pro_quiz_question
                        (quiz_id, previous_id, sort, title, points, question, correct_msg, incorrect_msg,
                         correct_same_text, tip_enabled, tip_msg, answer_type, show_points_in_box,
                         answer_points_activated, answer_data, category_id, answer_points_diff_modus_activated,
                         disable_correct, matrix_sort_answer_criteria_width, online)
                        VALUES
                        (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    """, (
                        0,  # quiz_id
                        0,  # previous_id
                        imported + 1,  # sort
                        title,
                        1,  # points
                        q['question_text'],
                        q['explanation'] or 'Correct!',
                        f"Incorrect. {q['explanation']}" if q['explanation'] else 'Incorrect.',
                        0,  # correct_same_text
                        0,  # tip_enabled
                        '',  # tip_msg
                        answer_type,
                        0,  # show_points_in_box
                        0,  # answer_points_activated
                        answer_data,
                        category_id,
                        0,  # answer_points_diff_modus_activated
                        0,  # disable_correct
                        20,  # matrix_sort_answer_criteria_width
                        1  # online
                    ))
                    insert_cursor.close()
                except Exception as e:
                    print(f"❌ Error inserting {title}: {e}")
                    failed += 1
                    continue

            imported += 1

        if not dry_run:
            self.connection.commit()

        print(f"\n{'='*70}")
        print(f"✅ Imported: {imported}")
        print(f"⚠️  Skipped: {skipped} (no category mapping)")
        print(f"❌ Failed: {failed}")
        print(f"{'='*70}\n")

        cursor.close()

    def close_db(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()


def main():
    import sys

    importer = EzToLearnDashImporter()

    try:
        dry_run = '--dry-run' in sys.argv
        limit = None
        categories = None

        # Parse limit
        if '--limit' in sys.argv:
            limit_idx = sys.argv.index('--limit') + 1
            if limit_idx < len(sys.argv):
                limit = int(sys.argv[limit_idx])

        # Parse categories
        if '--categories' in sys.argv:
            cat_idx = sys.argv.index('--categories') + 1
            if cat_idx < len(sys.argv):
                categories = sys.argv[cat_idx].split(',')

        importer.import_questions(dry_run=dry_run, limit=limit, categories=categories)

    finally:
        importer.close_db()


if __name__ == '__main__':
    import sys

    if len(sys.argv) < 2 or '--help' in sys.argv:
        print("Import wp_ez_diagnostic_questions to LearnDash")
        print()
        print("Usage:")
        print("  python3 import_ez_to_learndash.py --dry-run")
        print("  python3 import_ez_to_learndash.py --dry-run --limit 100")
        print("  python3 import_ez_to_learndash.py --categories DEV_GOLANG,DEV_PYTHON")
        print("  python3 import_ez_to_learndash.py  # LIVE import all")
        print()
        print("Options:")
        print("  --dry-run              Preview without importing")
        print("  --limit N              Import only N questions")
        print("  --categories X,Y,Z     Import specific categories only")
        exit(0)

    main()
