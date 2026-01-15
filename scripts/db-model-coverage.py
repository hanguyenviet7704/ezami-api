#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Iterable, Optional


CORE_WP_TABLES = {
    "wp_posts",
    "wp_postmeta",
    "wp_users",
    "wp_usermeta",
    "wp_terms",
    "wp_term_taxonomy",
    "wp_term_relationships",
    "wp_termmeta",
    "wp_comments",
    "wp_commentmeta",
    "wp_options",
    "wp_links",
}


@dataclass(frozen=True)
class EntityInfo:
    class_name: str
    package: str
    table: Optional[str]
    path: str


def _run(cmd: list[str]) -> str:
    return subprocess.check_output(cmd, text=True, stderr=subprocess.STDOUT)


def read_dotenv(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    out: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        out[key.strip()] = value.strip().strip("'").strip('"')
    return out


def parse_schema_from_jdbc_url(jdbc_url: str) -> Optional[str]:
    m = re.search(r"jdbc:mysql://[^/]+/([^?]+)", jdbc_url)
    if not m:
        return None
    return m.group(1)


def docker_mysql_tables(container: str, user: str, password: str, schema: str) -> list[str]:
    query = (
        "SELECT table_name FROM information_schema.tables "
        f"WHERE table_schema='{schema}' AND table_type='BASE TABLE' "
        "ORDER BY table_name;"
    )
    cmd = [
        "docker",
        "exec",
        "-i",
        "-e",
        f"MYSQL_PWD={password}",
        container,
        "mysql",
        f"-u{user}",
        "-N",
        "-e",
        query,
    ]
    raw = _run(cmd)
    tables = [line.strip() for line in raw.splitlines() if line.strip()]
    return tables


def find_entity_files(src_root: Path) -> list[Path]:
    entity_files: list[Path] = []
    for path in src_root.rglob("*.java"):
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        if re.search(r"@Entity\b", text):
            entity_files.append(path)
    return sorted(entity_files)


_RE_PACKAGE = re.compile(r"^\s*package\s+([a-zA-Z0-9_.]+)\s*;\s*$", re.M)
_RE_CLASS = re.compile(r"\bclass\s+([A-Za-z0-9_]+)\b")
_RE_TABLE = re.compile(r"@Table\s*\(\s*name\s*=\s*\"([^\"]+)\"", re.S)


def parse_entity_info(path: Path) -> EntityInfo:
    text = path.read_text(encoding="utf-8", errors="replace")
    package = _RE_PACKAGE.search(text)
    class_name = _RE_CLASS.search(text)
    table = _RE_TABLE.search(text)
    return EntityInfo(
        class_name=(class_name.group(1) if class_name else path.stem),
        package=(package.group(1) if package else ""),
        table=(table.group(1) if table else None),
        path=str(path),
    )


def group_table(table: str) -> str:
    t = table.lower()
    if t.startswith("ez_"):
        return "ez_"
    if t.startswith("eil_"):
        return "eil_"
    if t.startswith("wp_ez_"):
        return "wp_ez_"
    if t in CORE_WP_TABLES:
        return "wp_core"
    if t.startswith("wp_learndash_pro_quiz_"):
        return "wp_learndash_pro_quiz_"
    if t.startswith("wp_learndash_"):
        return "wp_learndash_"
    if t.startswith("wp_"):
        parts = t.split("_")
        return f"wp_{parts[1]}" if len(parts) > 1 else "wp_"
    return "other"


def pct(n: int, d: int) -> str:
    if d == 0:
        return "0.0%"
    return f"{(n * 100.0 / d):.1f}%"


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(
        description="Compare MySQL tables vs JPA @Entity/@Table coverage."
    )
    parser.add_argument("--container", default=os.getenv("MYSQL_CONTAINER", "ezami-mysql"))
    parser.add_argument("--schema", default=os.getenv("MYSQL_SCHEMA", ""))
    parser.add_argument("--user", default=os.getenv("MYSQL_USER", ""))
    parser.add_argument("--password", default=os.getenv("MYSQL_PASSWORD", ""))
    parser.add_argument("--dotenv", default=".env")
    parser.add_argument("--src-root", default="src/main/java")
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args(argv)

    dotenv = read_dotenv(Path(args.dotenv))
    db_url = os.getenv("DB_URL") or dotenv.get("DB_URL", "")
    schema = args.schema or os.getenv("DB_SCHEMA") or parse_schema_from_jdbc_url(db_url) or "wordpress"
    user = args.user or os.getenv("DB_USER") or dotenv.get("DB_USER", "root")
    password = args.password or os.getenv("DB_PASS") or dotenv.get("DB_PASS", "")

    if not password:
        print("Missing MySQL password. Set DB_PASS / MYSQL_PASSWORD or fill .env DB_PASS.", file=sys.stderr)
        return 2

    tables = docker_mysql_tables(args.container, user, password, schema)
    tables_lc = sorted({t.lower() for t in tables})

    entity_files = find_entity_files(Path(args.src_root))
    entities = [parse_entity_info(p) for p in entity_files]

    mapped_tables = sorted({(e.table or "").lower() for e in entities if e.table})
    mapped_tables_set = set(mapped_tables)
    tables_set = set(tables_lc)

    covered_tables = sorted(tables_set & mapped_tables_set)
    uncovered_tables = sorted(tables_set - mapped_tables_set)
    orphan_mappings = sorted(mapped_tables_set - tables_set)

    by_group: dict[str, dict[str, object]] = {}
    for t in tables_lc:
        g = group_table(t)
        by_group.setdefault(g, {"tables": set(), "covered": set()})
        by_group[g]["tables"].add(t)
        if t in mapped_tables_set:
            by_group[g]["covered"].add(t)

    group_rows = []
    for g in sorted(by_group.keys()):
        total = len(by_group[g]["tables"])
        covered = len(by_group[g]["covered"])
        group_rows.append({"group": g, "covered": covered, "total": total, "pct": pct(covered, total)})

    result = {
        "schema": schema,
        "mysql_container": args.container,
        "tables_total": len(tables_lc),
        "entities_total": len(entities),
        "entities_with_table_total": len(mapped_tables),
        "tables_covered_total": len(covered_tables),
        "tables_coverage_pct": pct(len(covered_tables), len(tables_lc)),
        "tables_uncovered_total": len(uncovered_tables),
        "table_groups": group_rows,
        "uncovered_tables": uncovered_tables,
        "orphan_entity_table_mappings": orphan_mappings,
        "entities": [asdict(e) for e in entities],
    }

    if args.json:
        print(json.dumps(result, indent=2, ensure_ascii=False))
        return 0

    print(f"Schema: {schema}")
    print(f"MySQL container: {args.container}")
    print(f"Tables: {len(tables_lc)}")
    print(f"Entities (@Entity): {len(entities)}")
    print(f"Entities with @Table: {len(mapped_tables)}")
    print(f"Covered tables (have @Entity/@Table): {len(covered_tables)} ({pct(len(covered_tables), len(tables_lc))})")
    print("")
    print("Coverage by group:")
    for row in group_rows:
        print(f"- {row['group']}: {row['covered']}/{row['total']} ({row['pct']})")
    print("")
    for focus_group in ("ez_", "eil_", "wp_ez_", "wp_core", "wp_learndash_", "wp_learndash_pro_quiz_"):
        group_tables = sorted(t for t in uncovered_tables if group_table(t) == focus_group)
        if group_tables:
            print(f"Uncovered tables in {focus_group} ({len(group_tables)}):")
            for t in group_tables:
                print(f"  - {t}")
            print("")
    if orphan_mappings:
        print(f"Orphan entity table mappings (not in DB) ({len(orphan_mappings)}):")
        for t in orphan_mappings:
            print(f"  - {t}")
        print("")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
