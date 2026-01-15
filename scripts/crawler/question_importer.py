#!/usr/bin/env python3
"""
Question Importer - Import questions from CSV/JSON files into Ezami database
Safe alternative to web scraping
"""

import pymysql
import json
import csv
import os
import sys
from datetime import datetime
from typing import List, Dict, Any
from pathlib import Path
from dotenv import load_dotenv

class QuestionImporter:
    """Import questions from structured files into WordPress quiz database"""

    def __init__(self):
        load_dotenv()
        self.connection = None

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
            sys.exit(1)

    def close_db(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()

    def import_from_json(self, json_file: str, certification_code: str, category_id: int):
        """
        Import questions from JSON file

        JSON format:
        {
          "questions": [
            {
              "title": "Q1_Title",
              "question": "Question text?",
              "question_type": "single",  // or "multiple"
              "answers": [
                {"text": "Option A", "correct": false},
                {"text": "Option B", "correct": true}
              ],
              "explanation": "Why B is correct",
              "skill_code": "SKILL_CODE"  // optional
            }
          ]
        }
        """
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        questions = data.get('questions', [])

        if not questions:
            print(f"⚠️ No questions found in {json_file}")
            return

        print(f"📥 Importing {len(questions)} questions for {certification_code}...")

        cursor = self.connection.cursor()
        imported_count = 0

        try:
            for idx, q in enumerate(questions, start=1):
                # Prepare question data
                title = q.get('title', f'{certification_code}_Q{idx}')
                question_text = q.get('question', '')
                answers = q.get('answers', [])
                explanation = q.get('explanation', '')
                question_type = q.get('question_type', 'single')

                if not question_text or not answers:
                    print(f"⚠️ Skipping question {idx}: missing question text or answers")
                    continue

                # Serialize answers to WordPress format
                answer_data = self.serialize_answers(answers)

                # Insert into wp_learndash_pro_quiz_question
                insert_query = """
                INSERT INTO wp_learndash_pro_quiz_question
                (quiz_id, previous_id, sort, title, points, question, correct_msg, incorrect_msg,
                 correct_same_text, tip_enabled, tip_msg, answer_type, show_points_in_box,
                 answer_points_activated, answer_data, category_id, answer_points_diff_modus_activated,
                 disable_correct, matrix_sort_answer_criteria_width, online)
                VALUES
                (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """

                cursor.execute(insert_query, (
                    0,  # quiz_id = 0 for standalone questions
                    0,  # previous_id
                    idx,  # sort order
                    title,
                    1,  # points
                    question_text,
                    explanation if explanation else 'Correct!',
                    explanation if explanation else 'Incorrect.',
                    1,  # correct_same_text
                    0,  # tip_enabled
                    '',  # tip_msg
                    question_type,
                    0,  # show_points_in_box
                    0,  # answer_points_activated
                    answer_data,
                    category_id,
                    0,  # answer_points_diff_modus_activated
                    0,  # disable_correct
                    20,  # matrix_sort_answer_criteria_width
                    1  # online
                ))

                imported_count += 1

                if imported_count % 50 == 0:
                    print(f"  ... imported {imported_count}/{len(questions)}")

            self.connection.commit()
            print(f"✅ Successfully imported {imported_count} questions for {certification_code}")

        except Exception as e:
            self.connection.rollback()
            print(f"❌ Import failed: {e}")
            raise
        finally:
            cursor.close()

    def serialize_answers(self, answers: List[Dict[str, Any]]) -> str:
        """
        Serialize answers to WordPress quiz format (PHP serialized)
        Format: a:N:{i:0;O:27:"WpProQuiz_Model_AnswerTypes":4:{...}}
        """
        result = f"a:{len(answers)}:{{"

        for idx, ans in enumerate(answers):
            answer_text = ans.get('text', '')
            is_correct = 1 if ans.get('correct', False) else 0

            # Create serialized answer object
            answer_obj = (
                f"i:{idx};"
                f"O:27:\"WpProQuiz_Model_AnswerTypes\":4:{{"
                f"s:9:\"_answer\";s:{len(answer_text)}:\"{answer_text}\";"
                f"s:10:\"_correct\";b:{is_correct};"
                f"s:8:\"_points\";i:0;"
                f"s:15:\"_sortString\";s:0:\"\";"
                f"}}"
            )
            result += answer_obj

        result += "}"
        return result

    def import_from_csv(self, csv_file: str, certification_code: str, category_id: int):
        """
        Import from CSV file
        CSV columns: title, question, answer1, answer2, answer3, answer4, correct_answers, explanation
        """
        questions = []

        with open(csv_file, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                answers = []
                correct_indices = [int(x.strip()) - 1 for x in row.get('correct_answers', '1').split(',')]

                for i in range(1, 5):
                    ans_key = f'answer{i}'
                    if ans_key in row and row[ans_key]:
                        answers.append({
                            'text': row[ans_key],
                            'correct': (i - 1) in correct_indices
                        })

                questions.append({
                    'title': row.get('title', f'Q{len(questions) + 1}'),
                    'question': row['question'],
                    'answers': answers,
                    'explanation': row.get('explanation', ''),
                    'question_type': 'multiple' if len(correct_indices) > 1 else 'single'
                })

        # Convert to JSON and import
        temp_json = {'questions': questions}
        temp_file = '/tmp/temp_questions.json'

        with open(temp_file, 'w', encoding='utf-8') as f:
            json.dump(temp_json, f, indent=2)

        self.import_from_json(temp_file, certification_code, category_id)


def main():
    if len(sys.argv) < 4:
        print("Usage:")
        print("  python3 question_importer.py <json_file> <certification_code> <category_id>")
        print("  python3 question_importer.py <csv_file> <certification_code> <category_id> --csv")
        print()
        print("Example:")
        print("  python3 question_importer.py data/aws_saa.json AWS_SAA_C03 30")
        print("  python3 question_importer.py data/questions.csv AWS_SAA_C03 30 --csv")
        sys.exit(1)

    input_file = sys.argv[1]
    cert_code = sys.argv[2]
    category_id = int(sys.argv[3])
    is_csv = '--csv' in sys.argv

    importer = QuestionImporter()
    importer.connect_db()

    try:
        if is_csv:
            importer.import_from_csv(input_file, cert_code, category_id)
        else:
            importer.import_from_json(input_file, cert_code, category_id)
    finally:
        importer.close_db()


if __name__ == '__main__':
    main()
