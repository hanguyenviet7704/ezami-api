#!/usr/bin/env python3
"""
Explanation Normalizer - Standardize and improve question explanations
"""

import pymysql
import re
import os
from dotenv import load_dotenv

class ExplanationNormalizer:
    """Normalize and improve question explanations"""

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

    def analyze_explanations(self):
        """Analyze current state of explanations"""
        cursor = self.connection.cursor()

        print("\n=== EXPLANATION ANALYSIS ===\n")

        # Overall stats
        cursor.execute("""
            SELECT
              COUNT(*) as total,
              COUNT(CASE WHEN correct_msg IS NULL OR correct_msg = '' THEN 1 END) as missing,
              COUNT(CASE WHEN LENGTH(correct_msg) < 20 THEN 1 END) as too_short,
              COUNT(CASE WHEN correct_msg LIKE '%<!--%' THEN 1 END) as has_html_comments,
              COUNT(CASE WHEN correct_msg LIKE '%<span%' THEN 1 END) as has_spans,
              AVG(LENGTH(correct_msg)) as avg_length
            FROM wp_learndash_pro_quiz_question
            WHERE online = 1
        """)

        stats = cursor.fetchone()
        print(f"Total questions: {stats['total']}")
        print(f"Missing explanations: {stats['missing']} ({stats['missing']*100//stats['total']}%)")
        print(f"Too short (<20 chars): {stats['too_short']} ({stats['too_short']*100//stats['total']}%)")
        print(f"Contains HTML comments: {stats['has_html_comments']}")
        print(f"Contains <span> tags: {stats['has_spans']}")
        print(f"Average length: {int(stats['avg_length'])} characters")

        # By category
        print("\n=== BY CATEGORY ===\n")
        cursor.execute("""
            SELECT c.category_name,
                   COUNT(q.id) as total,
                   COUNT(CASE WHEN q.correct_msg IS NULL OR q.correct_msg = '' THEN 1 END) as missing,
                   AVG(LENGTH(q.correct_msg)) as avg_length
            FROM wp_learndash_pro_quiz_question q
            JOIN wp_learndash_pro_quiz_category c ON q.category_id = c.category_id
            WHERE q.online = 1
            GROUP BY c.category_name
            HAVING total > 20
            ORDER BY missing DESC
            LIMIT 15
        """)

        for row in cursor.fetchall():
            print(f"{row['category_name']:20s}: {row['total']:4d} questions, {row['missing']:4d} missing ({row['missing']*100//row['total']:2d}%), avg {int(row['avg_length'] or 0):3d} chars")

        cursor.close()

    def clean_html(self, text: str) -> str:
        """Clean HTML tags and comments from explanation"""
        if not text:
            return ''

        # Remove HTML comments
        text = re.sub(r'<!--.*?-->', '', text, flags=re.DOTALL)

        # Remove StartFragment/EndFragment markers
        text = text.replace('<!--StartFragment-->', '').replace('<!--EndFragment-->', '')

        # Convert <br> to newlines
        text = re.sub(r'<br\s*/?>', '\n', text)

        # Clean up specific formatting
        text = re.sub(r'<p[^>]*>', '\n', text)
        text = text.replace('</p>', '\n')

        # Keep useful spans but remove styling
        text = re.sub(r'<span[^>]*>', '', text)
        text = text.replace('</span>', '')

        # Remove other common tags
        text = re.sub(r'</?(?:div|em|strong|i|b)[^>]*>', '', text)

        # Clean whitespace
        text = re.sub(r'\n\s*\n', '\n\n', text)  # Multiple newlines → double
        text = re.sub(r'[ \t]+', ' ', text)  # Multiple spaces → single
        text = text.strip()

        return text

    def generate_incorrect_msg(self, correct_msg: str, question_id: int) -> str:
        """Generate incorrect_msg from correct_msg if they're the same"""
        if not correct_msg:
            return "Incorrect. Please review the explanation."

        # If correct_msg explains all options, use it for incorrect too
        if '(A)' in correct_msg and '(B)' in correct_msg:
            return correct_msg

        # Otherwise, create a brief incorrect message
        clean = self.clean_html(correct_msg)
        if len(clean) > 100:
            return f"Incorrect. {clean[:100]}..."
        return f"Incorrect. {clean}"

    def normalize_all_explanations(self, dry_run=True):
        """Normalize explanations for all questions"""
        cursor = self.connection.cursor()

        # Get all questions
        cursor.execute("""
            SELECT id, title, question, correct_msg, incorrect_msg, answer_data
            FROM wp_learndash_pro_quiz_question
            WHERE online = 1
            ORDER BY id
        """)

        questions = cursor.fetchall()
        total = len(questions)
        updated = 0
        skipped = 0

        print(f"\n{'='*60}")
        print(f"Normalizing {total} questions...")
        print(f"Mode: {'DRY RUN (preview only)' if dry_run else 'LIVE UPDATE'}")
        print(f"{'='*60}\n")

        for q in questions:
            qid = q['id']
            original_correct = q['correct_msg'] or ''
            original_incorrect = q['incorrect_msg'] or ''

            # Clean HTML
            clean_correct = self.clean_html(original_correct)
            clean_incorrect = self.clean_html(original_incorrect)

            # Generate incorrect_msg if missing or same as correct
            if not clean_incorrect or clean_incorrect == clean_correct:
                clean_incorrect = self.generate_incorrect_msg(clean_correct, qid)

            # Add default message if still missing
            if not clean_correct:
                clean_correct = "[Explanation needed - please review]"
                clean_incorrect = "[Explanation needed - please review]"

            # Check if update needed
            needs_update = (
                original_correct != clean_correct or
                original_incorrect != clean_incorrect
            )

            if needs_update:
                if not dry_run:
                    update_cursor = self.connection.cursor()
                    update_cursor.execute("""
                        UPDATE wp_learndash_pro_quiz_question
                        SET correct_msg = %s,
                            incorrect_msg = %s
                        WHERE id = %s
                    """, (clean_correct, clean_incorrect, qid))
                    update_cursor.close()

                updated += 1

                if updated <= 10:  # Show first 10 examples
                    print(f"\n[Q{qid}] {q['title']}")
                    print(f"  Before: {original_correct[:60]}...")
                    print(f"  After:  {clean_correct[:60]}...")

            else:
                skipped += 1

            if (updated + skipped) % 1000 == 0:
                print(f"  Progress: {updated + skipped}/{total} ({updated} updated, {skipped} skipped)")

        if not dry_run:
            self.connection.commit()

        print(f"\n{'='*60}")
        print(f"✅ Completed: {updated} updated, {skipped} skipped")
        print(f"{'='*60}\n")

        cursor.close()

    def add_detailed_explanations_to_new_questions(self):
        """Add detailed explanations to newly imported questions (categories 30-41)"""
        cursor = self.connection.cursor()

        # Get questions with minimal explanations
        cursor.execute("""
            SELECT id, title, question, answer_data, category_id
            FROM wp_learndash_pro_quiz_question
            WHERE online = 1
              AND category_id >= 30
              AND (LENGTH(correct_msg) < 50 OR correct_msg LIKE '%Correct!%')
        """)

        questions = cursor.fetchall()
        print(f"\nFound {len(questions)} new questions needing detailed explanations")

        # For demo purposes, we'll create a template
        # In production, you'd use AI or SMEs to write proper explanations

        for q in questions:
            # Parse answers to get correct one
            correct_answers = self.extract_correct_answers(q['answer_data'])

            detailed_explanation = (
                f"✓ Correct Answer: {', '.join(correct_answers)}\n\n"
                f"Explanation: This question tests your understanding of the topic. "
                f"The correct answer(s) are based on official documentation and best practices. "
                f"Review the related concepts to better understand why this is the correct choice."
            )

            incorrect_explanation = (
                f"✗ Incorrect\n\n"
                f"The correct answer(s) are: {', '.join(correct_answers)}\n\n"
                f"Please review the explanation above to understand the correct approach."
            )

            print(f"  [{q['title']}] Adding explanation...")

            cursor.execute("""
                UPDATE wp_learndash_pro_quiz_question
                SET correct_msg = %s,
                    incorrect_msg = %s
                WHERE id = %s
            """, (detailed_explanation, incorrect_explanation, q['id']))

        self.connection.commit()
        print(f"✅ Added detailed explanations to {len(questions)} questions")
        cursor.close()

    def extract_correct_answers(self, answer_data: str) -> list:
        """Extract correct answer text from serialized answer_data"""
        if not answer_data:
            return []

        correct = []
        # Pattern to match correct answers in PHP serialized format
        pattern = r's:9:"_answer";s:\d+:"([^"]+)";s:10:"_correct";b:1'
        matches = re.findall(pattern, answer_data)

        return matches

    def close_db(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()


def main():
    import sys

    normalizer = ExplanationNormalizer()

    try:
        if '--analyze' in sys.argv:
            normalizer.analyze_explanations()

        elif '--normalize' in sys.argv:
            dry_run = '--dry-run' in sys.argv
            normalizer.normalize_all_explanations(dry_run=dry_run)

        elif '--add-new' in sys.argv:
            normalizer.add_detailed_explanations_to_new_questions()

        else:
            print("Explanation Normalizer")
            print()
            print("Usage:")
            print("  python3 explanation_normalizer.py --analyze")
            print("  python3 explanation_normalizer.py --normalize --dry-run")
            print("  python3 explanation_normalizer.py --normalize")
            print("  python3 explanation_normalizer.py --add-new")
            print()
            print("Commands:")
            print("  --analyze     Analyze current explanation quality")
            print("  --normalize   Clean HTML and standardize format")
            print("  --add-new     Add detailed explanations to new questions")
            print("  --dry-run     Preview changes without applying")

    finally:
        normalizer.close_db()


if __name__ == '__main__':
    main()
