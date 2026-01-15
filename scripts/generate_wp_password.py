#!/usr/bin/env python3
"""
Generate WordPress-compatible bcrypt password hash.
WordPress 4.4+ supports bcrypt with $wp$2y$ prefix.
"""

import bcrypt
import sys

def generate_wp_password(password: str) -> str:
    """Generate WordPress-compatible password hash."""
    # Generate bcrypt hash
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)

    # Convert to WordPress format: $wp$2y$...
    # Replace $2b$ with $wp$2y$
    hash_str = hashed.decode('utf-8')
    wp_hash = hash_str.replace('$2b$', '$wp$2y$')

    return wp_hash

if __name__ == "__main__":
    password = sys.argv[1] if len(sys.argv) > 1 else "Ezami@2024"
    hash_result = generate_wp_password(password)
    print(f"Password: {password}")
    print(f"Hash: {hash_result}")
