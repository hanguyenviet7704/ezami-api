#!/usr/bin/env python3
"""
GitHub Question Fetcher - Fetch practice questions from open-source GitHub repos
IMPORTANT: Only use repos with permissive licenses (MIT, Apache, GPL with attribution)
"""

import requests
import json
import re
import sys
from typing import List, Dict, Any
from pathlib import Path

class GitHubQuestionFetcher:
    """Fetch questions from GitHub repositories"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Ezami-Question-Importer/1.0 (Educational purposes)'
        })

    def fetch_ditectrev_questions(self, cert_code: str) -> List[Dict[str, Any]]:
        """
        Fetch from Ditectrev repositories (MIT License)
        These repos have questions in README.md format

        Example repo: AWS-Certified-Solutions-Architect-Associate-SAA-C03-Practice-Tests-Exams-Questions-Answers
        """
        repo_map = {
            'AWS_SAA_C03': 'Ditectrev/AWS-Certified-Solutions-Architect-Associate-SAA-C03-Practice-Tests-Exams-Questions-Answers',
            'AWS_DVA_C02': 'Ditectrev/AWS-Certified-Developer-Associate-DVA-C02-Practice-Tests-Exams-Questions-Answers',
            'AZURE_AZ104': 'Ditectrev/Microsoft-Azure-AZ-104-Microsoft-Azure-Administrator-Practice-Tests-Exams-Questions-Answers',
        }

        if cert_code not in repo_map:
            print(f"Warning: No Ditectrev repo mapped for {cert_code}")
            return []

        repo = repo_map[cert_code]
        raw_url = f"https://raw.githubusercontent.com/{repo}/main/README.md"

        try:
            print(f"Fetching from: {raw_url}")
            response = self.session.get(raw_url, timeout=30)
            response.raise_for_status()

            markdown_content = response.text
            questions = self.parse_ditectrev_markdown(markdown_content, cert_code)

            print(f"✅ Fetched {len(questions)} questions from {repo}")
            return questions

        except requests.RequestException as e:
            print(f"❌ Error fetching from {repo}: {e}")
            return []

    def parse_ditectrev_markdown(self, markdown: str, cert_code: str) -> List[Dict[str, Any]]:
        """
        Parse Ditectrev markdown format
        Format usually: #### Q1. Question text?
                       - [ ] Option A
                       - [x] Option B (correct)
        """
        questions = []

        # Split by question headers (#### Q1, #### Q2, etc.)
        question_pattern = r'####\s*Q(\d+)\.\s*(.+?)(?=####\s*Q\d+\.|$)'
        matches = re.findall(question_pattern, markdown, re.DOTALL)

        for q_num, q_content in matches:
            question_text, answers = self.parse_question_content(q_content)

            if question_text and answers:
                questions.append({
                    'title': f'{cert_code}_Q{q_num}',
                    'question_text': question_text.strip(),
                    'answers': answers,
                    'question_type': 'multiple' if sum(a['correct'] for a in answers) > 1 else 'single',
                    'cert_code': cert_code,
                    'source': 'Ditectrev (Open Source)'
                })

        return questions

    def parse_question_content(self, content: str) -> tuple:
        """Parse question text and answer options"""
        lines = content.strip().split('\n')

        question_text = ''
        answers = []

        for line in lines:
            line = line.strip()

            # Answer option: - [x] or - [ ]
            if line.startswith('- ['):
                is_correct = line.startswith('- [x]') or line.startswith('- [X]')
                answer_text = re.sub(r'^-\s*\[[xX ]?\]\s*', '', line)

                answers.append({
                    'text': answer_text.strip(),
                    'correct': is_correct
                })
            # Question text (non-answer lines)
            elif not line.startswith('#') and line:
                question_text += line + ' '

        return question_text.strip(), answers

    def fetch_from_json_url(self, url: str, cert_code: str) -> List[Dict[str, Any]]:
        """Fetch from a JSON file URL"""
        try:
            response = self.session.get(url, timeout=30)
            response.raise_for_status()

            data = response.json()

            # Normalize format
            if isinstance(data, list):
                questions = data
            elif isinstance(data, dict) and 'questions' in data:
                questions = data['questions']
            else:
                print(f"❌ Unexpected JSON format from {url}")
                return []

            # Add cert_code to each question
            for q in questions:
                q['cert_code'] = cert_code

            print(f"✅ Fetched {len(questions)} questions from JSON")
            return questions

        except Exception as e:
            print(f"❌ Error fetching JSON: {e}")
            return []

    def save_to_file(self, questions: List[Dict[str, Any]], output_file: str):
        """Save questions to JSON file"""
        output_path = Path(output_file)
        output_path.parent.mkdir(parents=True, exist_ok=True)

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump({
                'total': len(questions),
                'questions': questions
            }, f, indent=2, ensure_ascii=False)

        print(f"💾 Saved {len(questions)} questions to {output_file}")


def main():
    if len(sys.argv) < 3:
        print("Usage: python3 github_question_fetcher.py <cert_code> <output_file>")
        print("Example: python3 github_question_fetcher.py AWS_SAA_C03 data/aws_saa.json")
        sys.exit(1)

    cert_code = sys.argv[1]
    output_file = sys.argv[2]

    fetcher = GitHubQuestionFetcher()
    questions = fetcher.fetch_ditectrev_questions(cert_code)

    if questions:
        fetcher.save_to_file(questions, output_file)
    else:
        print(f"⚠️ No questions fetched for {cert_code}")
        sys.exit(1)


if __name__ == '__main__':
    main()
