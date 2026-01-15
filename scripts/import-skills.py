#!/usr/bin/env python3
"""
Script to import skills taxonomy into WordPress database
"""
import json
import mysql.connector
from datetime import datetime

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 3307,
    'user': 'root',
    'password': '12345678aA@',
    'database': 'wordpress'
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def insert_skill(cursor, skill, certification_id, parent_id=None, level=0, sort_order=0):
    """Insert a skill and return its ID"""
    sql = """
    INSERT INTO wp_ez_skills
    (parent_id, certification_id, code, name, description, level, sort_order, status, version, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, %s, %s, 'active', 1, NOW(), NOW())
    """
    values = (
        parent_id,
        certification_id,
        skill['code'],
        skill['name'],
        skill.get('description', ''),
        level,
        sort_order
    )
    cursor.execute(sql, values)
    return cursor.lastrowid

def process_skills(cursor, skills, certification_id, parent_id=None, level=0):
    """Recursively process skills tree"""
    count = 0
    for idx, skill in enumerate(skills):
        skill_id = insert_skill(cursor, skill, certification_id, parent_id, level, idx)
        count += 1

        if 'children' in skill and skill['children']:
            count += process_skills(cursor, skill['children'], certification_id, skill_id, level + 1)

    return count

def main():
    # Load seed data
    with open('skills-seed-data.json', 'r') as f:
        data = json.load(f)

    conn = get_connection()
    cursor = conn.cursor()

    total_skills = 0

    try:
        for cert in data['certifications']:
            cert_id = cert['certification_id']
            print(f"\nImporting {cert['name']} ({cert_id})...")

            # Insert root skill
            root_skill = {
                'code': f"{cert_id}_ROOT",
                'name': cert['name'],
                'description': cert.get('description', '')
            }
            root_id = insert_skill(cursor, root_skill, cert_id, None, 0, 0)
            total_skills += 1

            # Process children
            count = process_skills(cursor, cert['skills'], cert_id, root_id, 1)
            total_skills += count

            print(f"  Imported {count + 1} skills for {cert_id}")

        conn.commit()
        print(f"\n=== Total: {total_skills} skills imported successfully ===")

    except Exception as e:
        conn.rollback()
        print(f"Error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
