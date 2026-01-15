#!/usr/bin/env python3
"""
Generate explanations for questions that are missing them
Uses answer data to create basic explanations
"""

import pymysql
import re
import os
from dotenv import load_dotenv

class ExplanationGenerator:
    """Generate explanations for questions missing them"""

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

    def extract_answers_from_data(self, answer_data: str) -> list:
        """Extract answer options with correct flags from serialized data"""
        if not answer_data:
            return []

        answers = []

        # Pattern for private properties: s:10:"\0*\0_answer";s:LENGTH:"TEXT"
        # and s:11:"\0*\0_correct";b:0|1

        # Find all answer texts
        answer_pattern = r's:\d+:"\\0\*\\0_answer";s:(\d+):"([^"]+)"'
        answer_matches = re.findall(answer_pattern, answer_data)

        # Find all correct flags
        correct_pattern = r's:\d+:"\\0\*\\0_correct";b:(\d)'
        correct_matches = re.findall(correct_pattern, answer_data)

        # Combine them
        for idx, ((length, text), is_correct) in enumerate(zip(answer_matches, correct_matches)):
            answers.append({
                'index': idx,
                'text': text,
                'correct': is_correct == '1'
            })

        return answers

    def generate_explanation(self, question_text: str, answers: list, title: str) -> tuple:
        """Generate explanation from answers"""
        if not answers:
            return ("[No answers found]", "[No answers found]")

        correct_answers = [a for a in answers if a['correct']]
        incorrect_answers = [a for a in answers if not a['correct']]

        if not correct_answers:
            return ("[No correct answer marked]", "[No correct answer marked]")

        # Build correct_msg
        correct_parts = []

        if len(correct_answers) == 1:
            correct_parts.append(f"✓ The correct answer is: {correct_answers[0]['text']}")
        else:
            correct_texts = [a['text'] for a in correct_answers]
            correct_parts.append(f"✓ The correct answers are: {', '.join(correct_texts)}")

        # Add why others are wrong
        if incorrect_answers:
            correct_parts.append("\n\nWhy other options are incorrect:")
            for ans in incorrect_answers[:3]:  # Max 3 incorrect explanations
                correct_parts.append(f"• {ans['text']}: Not the best choice for this scenario.")

        correct_msg = '\n'.join(correct_parts)

        # Build incorrect_msg
        if len(correct_answers) == 1:
            incorrect_msg = (
                f"✗ Incorrect.\n\n"
                f"The correct answer is: {correct_answers[0]['text']}\n\n"
                f"Please review the question and the correct answer explanation."
            )
        else:
            correct_texts = [a['text'] for a in correct_answers]
            incorrect_msg = (
                f"✗ Incorrect.\n\n"
                f"The correct answers are: {', '.join(correct_texts)}\n\n"
                f"This is a multiple-choice question. Make sure you selected all correct options."
            )

        return (correct_msg, incorrect_msg)

    def generate_for_missing(self, dry_run=True, limit=None):
        """Generate explanations for questions missing them"""
        cursor = self.connection.cursor()

        # Get questions with missing or placeholder explanations
        query = """
            SELECT id, title, question, answer_data, correct_msg, category_id
            FROM wp_learndash_pro_quiz_question
            WHERE online = 1
              AND (
                  correct_msg IS NULL
                  OR correct_msg = ''
                  OR correct_msg = '[Explanation needed - please review]'
                  OR LENGTH(correct_msg) < 10
              )
            ORDER BY category_id, id
        """

        if limit:
            query += f" LIMIT {limit}"

        cursor.execute(query)
        questions = cursor.fetchall()

        print(f"\n{'='*70}")
        print(f"Generating explanations for {len(questions)} questions")
        print(f"Mode: {'DRY RUN' if dry_run else 'LIVE UPDATE'}")
        print(f"{'='*70}\n")

        generated = 0
        failed = 0

        for q in questions:
            answers = self.extract_answers_from_data(q['answer_data'])

            if not answers:
                failed += 1
                continue

            correct_msg, incorrect_msg = self.generate_explanation(
                q['question'], answers, q['title']
            )

            # Show first 5 examples
            if generated < 5:
                print(f"\n[{q['title']}]")
                print(f"  Category: {q['category_id']}")
                print(f"  Answers: {len(answers)} options, {sum(1 for a in answers if a['correct'])} correct")
                print(f"  Generated: {correct_msg[:80]}...")

            if not dry_run:
                update_cursor = self.connection.cursor()
                update_cursor.execute("""
                    UPDATE wp_learndash_pro_quiz_question
                    SET correct_msg = %s,
                        incorrect_msg = %s
                    WHERE id = %s
                """, (correct_msg, incorrect_msg, q['id']))
                update_cursor.close()

            generated += 1

            if generated % 500 == 0:
                print(f"  Progress: {generated}/{len(questions)}")

        if not dry_run:
            self.connection.commit()

        print(f"\n{'='*70}")
        print(f"✅ Generated: {generated}")
        print(f"⚠️  Failed: {failed}")
        print(f"{'='*70}\n")

        cursor.close()

    def close_db(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()


def main():
    import sys

    gen = ExplanationGenerator()

    try:
        if '--generate' in sys.argv:
            dry_run = '--dry-run' in sys.argv

            # Parse limit if provided
            limit = None
            if '--limit' in sys.argv:
                limit_idx = sys.argv.index('--limit') + 1
                if limit_idx < len(sys.argv):
                    limit = int(sys.argv[limit_idx])

            gen.generate_for_missing(dry_run=dry_run, limit=limit)

        else:
            print("Generate Missing Explanations")
            print()
            print("Usage:")
            print("  python3 generate_missing_explanations.py --generate --dry-run")
            print("  python3 generate_missing_explanations.py --generate --limit 100")
            print("  python3 generate_missing_explanations.py --generate")
            print()
            print("Options:")
            print("  --generate    Generate explanations for missing ones")
            print("  --dry-run     Preview without updating database")
            print("  --limit N     Process only N questions")

    finally:
        gen.close_db()


if __name__ == '__main__':
    main()
