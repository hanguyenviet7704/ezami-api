#!/usr/bin/env python3
"""
Generate practice questions from official documentation (SAFE & LEGAL approach)
Uses official AWS, Azure, Kubernetes docs to create original practice questions
"""

import requests
import json
import re
import sys
from typing import List, Dict, Any

class DocumentationQuestionGenerator:
    """Generate original questions based on official docs"""

    def __init__(self):
        self.official_docs = {
            'AWS_SAA_C03': {
                'url': 'https://aws.amazon.com/certification/certified-solutions-architect-associate/',
                'domains': [
                    'Design Resilient Architectures (26%)',
                    'Design High-Performing Architectures (24%)',
                    'Design Secure Applications and Architectures (30%)',
                    'Design Cost-Optimized Architectures (20%)'
                ]
            },
            'KUBERNETES_CKA': {
                'url': 'https://kubernetes.io/docs/home/',
                'domains': [
                    'Cluster Architecture, Installation & Configuration (25%)',
                    'Workloads & Scheduling (15%)',
                    'Services & Networking (20%)',
                    'Storage (10%)',
                    'Troubleshooting (30%)'
                ]
            },
            'AZURE_AZ104': {
                'url': 'https://learn.microsoft.com/en-us/credentials/certifications/azure-administrator/',
                'domains': [
                    'Manage Azure identities and governance (15–20%)',
                    'Implement and manage storage (15–20%)',
                    'Deploy and manage Azure compute resources (20–25%)',
                    'Implement and manage virtual networking (15–20%)',
                    'Monitor and maintain Azure resources (10–15%)'
                ]
            }
        }

    def get_exam_domains(self, cert_code: str) -> List[str]:
        """Get official exam domains for a certification"""
        return self.official_docs.get(cert_code, {}).get('domains', [])

    def generate_template_questions(self, cert_code: str) -> List[Dict[str, Any]]:
        """
        Generate template questions based on exam domains
        These are placeholders that should be filled in by subject matter experts
        """
        domains = self.get_exam_domains(cert_code)

        if not domains:
            print(f"⚠️ No domains found for {cert_code}")
            return []

        questions = []

        for domain_idx, domain in enumerate(domains, start=1):
            # Create 5 template questions per domain
            for q_num in range(1, 6):
                questions.append({
                    'title': f'{cert_code}_D{domain_idx}_Q{q_num}',
                    'question': f'[TEMPLATE] Question about: {domain}',
                    'question_type': 'single',
                    'answers': [
                        {'text': '[Option A - Replace with actual answer]', 'correct': False},
                        {'text': '[Option B - Replace with actual answer]', 'correct': True},
                        {'text': '[Option C - Replace with actual answer]', 'correct': False},
                        {'text': '[Option D - Replace with actual answer]', 'correct': False}
                    ],
                    'explanation': f'[Add explanation for {domain}]',
                    'domain': domain,
                    'cert_code': cert_code,
                    'status': 'template',  # Needs to be filled by SME
                    'skill_code': f'{cert_code}_DOMAIN_{domain_idx}'
                })

        return questions

    def save_template(self, cert_code: str, output_file: str):
        """Save template questions to file for SMEs to fill in"""
        questions = self.generate_template_questions(cert_code)

        if questions:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump({
                    'certification': cert_code,
                    'total_templates': len(questions),
                    'instructions': 'Replace all [TEMPLATE] and [Replace...] text with actual content',
                    'official_source': self.official_docs.get(cert_code, {}).get('url', ''),
                    'questions': questions
                }, f, indent=2, ensure_ascii=False)

            print(f"✅ Created {len(questions)} question templates in {output_file}")
            print(f"📝 Next step: Have SME fill in the template questions")
        else:
            print(f"❌ Failed to generate templates for {cert_code}")


def main():
    if len(sys.argv) < 3:
        print("Generate question templates based on official exam domains")
        print()
        print("Usage: python3 generate_questions_from_docs.py <cert_code> <output_file>")
        print()
        print("Examples:")
        print("  python3 generate_questions_from_docs.py AWS_SAA_C03 templates/aws_saa_template.json")
        print("  python3 generate_questions_from_docs.py KUBERNETES_CKA templates/cka_template.json")
        print("  python3 generate_questions_from_docs.py AZURE_AZ104 templates/azure_template.json")
        print()
        print("Supported certifications:")
        gen = DocumentationQuestionGenerator()
        for cert in gen.official_docs.keys():
            print(f"  - {cert}")
        sys.exit(1)

    cert_code = sys.argv[1]
    output_file = sys.argv[2]

    generator = DocumentationQuestionGenerator()
    generator.save_template(cert_code, output_file)


if __name__ == '__main__':
    main()
