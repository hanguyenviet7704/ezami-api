# Question Crawler Scripts

Scripts for importing practice questions from various sources into Ezami database.

## Sources

### Open Source / Public Domain
1. **GitHub Repositories** - Many exam prep repos with open licenses
2. **Community Contributions** - User-contributed practice tests
3. **Official Practice Materials** - Free samples from certification providers

### Supported Certifications
- AWS (SAA, DVA, SAP, DOP)
- Azure (AZ-104, AZ-305)
- GCP (ACE, Professional)
- Kubernetes (CKA, CKAD)
- Docker (DCA)
- HashiCorp Terraform
- Java (OCP 17)
- VMware Spring Professional
- CompTIA Security+
- ISC2 CISSP

## Usage

```bash
# Install dependencies
pip3 install -r requirements.txt

# Crawl from GitHub sources
python3 crawl_github_sources.py --cert AWS_SAA_C03 --output data/aws_saa.json

# Import to database
python3 import_questions.py --input data/aws_saa.json --certification AWS_SAA_C03
```

## Data Format

Questions should be in JSON format:
```json
{
  "source": "GitHub repo name",
  "license": "MIT/Apache-2.0/CC-BY",
  "certification": "AWS_SAA_C03",
  "questions": [
    {
      "question_text": "What is...",
      "question_type": "single",
      "answers": [
        {"text": "Option A", "correct": false},
        {"text": "Option B", "correct": true}
      ],
      "explanation": "...",
      "skill_code": "AWS_EC2_BASICS"
    }
  ]
}
```

## Ethical Guidelines

1. Only use sources with permissive licenses (MIT, Apache, CC-BY, Public Domain)
2. Respect rate limits and robots.txt
3. Attribute sources properly
4. Do not scrape paid content or bypass paywalls
5. Follow copyright laws and ToS
