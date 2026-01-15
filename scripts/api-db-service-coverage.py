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
from typing import Optional


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
class JavaType:
    package: str
    name: str
    kind: str  # class|interface
    path: str

    @property
    def fqn(self) -> str:
        return f"{self.package}.{self.name}" if self.package else self.name


@dataclass(frozen=True)
class EntityInfo:
    fqn: str
    table: Optional[str]
    path: str


@dataclass(frozen=True)
class RepoInfo:
    fqn: str
    entity_fqn: Optional[str]
    path: str


@dataclass(frozen=True)
class Endpoint:
    controller_fqn: str
    http_method: str
    path: str
    category: str  # app|web|system|unknown
    tables: list[str]


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
    return [line.strip() for line in raw.splitlines() if line.strip()]


_RE_PACKAGE = re.compile(r"^\s*package\s+([a-zA-Z0-9_.]+)\s*;\s*$", re.M)
_RE_IMPORT = re.compile(r"^\s*import\s+([a-zA-Z0-9_.$]+)\s*;\s*$", re.M)
_RE_CLASS_OR_INTERFACE = re.compile(r"^\s*(?:public\s+)?(class|interface)\s+([A-Za-z0-9_]+)\b", re.M)
_RE_ENTITY = re.compile(r"@Entity\b")
_RE_TABLE = re.compile(r"@Table\s*\(\s*name\s*=\s*\"([^\"]+)\"", re.S)
_RE_REST_CONTROLLER = re.compile(r"@RestController\b")
_RE_API_PREFIX = re.compile(r"@ApiPrefixController\b")

_RE_CLASS_REQUEST_MAPPING = re.compile(r"@RequestMapping\s*\(\s*\"([^\"]*)\"\s*\)", re.S)
_RE_CLASS_REQUEST_MAPPING_VALUE = re.compile(r"@RequestMapping\s*\(\s*value\s*=\s*\"([^\"]*)\"", re.S)

_RE_METHOD_MAPPING = re.compile(
    r"@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\s*\(([^)]*)\)",
    re.S,
)
_RE_METHOD_MAPPING_NOARGS = re.compile(r"@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\b")

_RE_FIRST_STRING = re.compile(r"\"([^\"]*)\"")
_RE_VALUE_STRING = re.compile(r"value\s*=\s*\"([^\"]*)\"")
_RE_PATH_STRING = re.compile(r"path\s*=\s*\"([^\"]*)\"")

_RE_REPO_EXTENDS = re.compile(
    r"extends\s+(?:JpaRepository|CrudRepository|PagingAndSortingRepository)\s*<\s*([A-Za-z0-9_.$]+)\s*,",
    re.M,
)
_RE_IMPLEMENTS = re.compile(r"\bclass\s+[A-Za-z0-9_]+\s+implements\s+([^{]+)\{", re.M)


def normalize_path(prefix: str, suffix: str) -> str:
    if prefix is None:
        prefix = ""
    if suffix is None:
        suffix = ""
    if not prefix:
        base = ""
    else:
        base = prefix
    if base and not base.startswith("/"):
        base = "/" + base
    if base.endswith("/"):
        base = base[:-1]
    if suffix and not suffix.startswith("/"):
        suffix = "/" + suffix
    full = (base or "") + (suffix or "")
    return full or "/"


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


def parse_java_type(path: Path, text: str) -> Optional[JavaType]:
    pkg = _RE_PACKAGE.search(text)
    m = _RE_CLASS_OR_INTERFACE.search(text)
    if not m:
        return None
    return JavaType(
        package=pkg.group(1) if pkg else "",
        kind=m.group(1),
        name=m.group(2),
        path=str(path),
    )


def parse_imports(text: str) -> dict[str, str]:
    out: dict[str, str] = {}
    for m in _RE_IMPORT.finditer(text):
        fqn = m.group(1)
        simple = fqn.split(".")[-1]
        out[simple] = fqn
    return out


def resolve_fqn(token: str, package: str, imports: dict[str, str]) -> str:
    if "." in token:
        return token
    if token in imports:
        return imports[token]
    return f"{package}.{token}" if package else token


def find_injected_type_tokens(text: str) -> set[str]:
    # Lombok @RequiredArgsConstructor -> fields are the source of dependencies.
    # Match both "private final X x;" and "private X x;".
    dep_tokens: set[str] = set()
    for m in re.finditer(r"^\s*private\s+(?:final\s+)?([A-Za-z0-9_.$]+)\s+[A-Za-z0-9_]+\s*;\s*$", text, re.M):
        token = m.group(1)
        if token.endswith("Service") or token.endswith("Repository"):
            dep_tokens.add(token)
    return dep_tokens


def controller_base_path(text: str) -> str:
    # Prefer explicit @RequestMapping
    m = _RE_CLASS_REQUEST_MAPPING.search(text) or _RE_CLASS_REQUEST_MAPPING_VALUE.search(text)
    if m:
        return m.group(1)
    return ""


def controller_has_api_prefix(text: str) -> bool:
    return bool(_RE_API_PREFIX.search(text))


def controller_explicit_api_prefix(text: str) -> Optional[str]:
    m = re.search(r"@RequestMapping\s*\(\s*\"(/api[^\"]*)\"\s*\)", text)
    if m:
        return m.group(1)
    m = re.search(r"@RequestMapping\s*\(\s*value\s*=\s*\"(/api[^\"]*)\"", text)
    if m:
        return m.group(1)
    return None


def endpoint_category(full_path: str) -> str:
    p = full_path.lower()
    if p.startswith("/webhook") or "/webhook" in p:
        return "system"
    if "/admin" in p or p.startswith("/admin"):
        return "web"
    return "app"


def parse_endpoints_from_controller(text: str, base_prefix: str) -> list[tuple[str, str]]:
    endpoints: list[tuple[str, str]] = []
    for m in _RE_METHOD_MAPPING.finditer(text):
        ann = m.group(1)  # GetMapping...
        args = m.group(2) or ""
        http_method = {
            "GetMapping": "GET",
            "PostMapping": "POST",
            "PutMapping": "PUT",
            "DeleteMapping": "DELETE",
            "PatchMapping": "PATCH",
        }[ann]
        path = ""
        mv = _RE_VALUE_STRING.search(args) or _RE_PATH_STRING.search(args)
        if mv:
            path = mv.group(1)
        else:
            ms = _RE_FIRST_STRING.search(args)
            if ms:
                path = ms.group(1)
        endpoints.append((http_method, normalize_path(base_prefix, path)))

    # Handle annotations without args e.g. @GetMapping
    for m in _RE_METHOD_MAPPING_NOARGS.finditer(text):
        ann = m.group(1)
        if ann not in {"GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping"}:
            continue
        # Skip those already captured with args by checking if immediately followed by '('
        after = text[m.end() : m.end() + 1]
        if after == "(":
            continue
        http_method = {
            "GetMapping": "GET",
            "PostMapping": "POST",
            "PutMapping": "PUT",
            "DeleteMapping": "DELETE",
            "PatchMapping": "PATCH",
        }[ann]
        endpoints.append((http_method, normalize_path(base_prefix, "")))
    return endpoints


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(
        description="Estimate API(service/repo) coverage vs DB tables (focus on web vs app sync)."
    )
    parser.add_argument("--container", default=os.getenv("MYSQL_CONTAINER", "ezami-mysql"))
    parser.add_argument("--schema", default=os.getenv("MYSQL_SCHEMA", ""))
    parser.add_argument("--user", default=os.getenv("MYSQL_USER", ""))
    parser.add_argument("--password", default=os.getenv("MYSQL_PASSWORD", ""))
    parser.add_argument("--dotenv", default=".env")
    parser.add_argument("--src-root", default="src/main/java")
    parser.add_argument("--controllers-root", default="src/main/java/com/hth/udecareer")
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

    # 1) DB tables
    tables = docker_mysql_tables(args.container, user, password, schema)
    db_tables = sorted({t.lower() for t in tables})
    db_tables_set = set(db_tables)

    # 2) Index java types / entities / repos / services / controllers
    src_root = Path(args.src_root)
    types_by_fqn: dict[str, JavaType] = {}
    java_text_cache: dict[str, str] = {}

    entity_by_fqn: dict[str, EntityInfo] = {}
    repo_by_fqn: dict[str, RepoInfo] = {}
    controllers: list[JavaType] = []
    services: set[str] = set()
    service_impls_by_interface: dict[str, set[str]] = {}

    for path in sorted(src_root.rglob("*.java")):
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        jt = parse_java_type(path, text)
        if not jt:
            continue
        types_by_fqn[jt.fqn] = jt
        java_text_cache[jt.fqn] = text

        if _RE_REST_CONTROLLER.search(text):
            controllers.append(jt)
        if re.search(r"@Service\b", text):
            services.add(jt.fqn)
            # Map interface -> implementation so controller injecting interface can resolve tables.
            imports = parse_imports(text)
            impl_m = _RE_IMPLEMENTS.search(text.replace("\n", " ") + "\n")
            if impl_m:
                raw = impl_m.group(1)
                for token in raw.split(","):
                    itf = token.strip()
                    if not itf:
                        continue
                    itf = re.sub(r"<.*?>", "", itf).strip()
                    itf = itf.split()[-1]
                    itf_fqn = resolve_fqn(itf, jt.package, imports)
                    service_impls_by_interface.setdefault(itf_fqn, set()).add(jt.fqn)
        if _RE_ENTITY.search(text):
            table_m = _RE_TABLE.search(text)
            entity_by_fqn[jt.fqn] = EntityInfo(
                fqn=jt.fqn,
                table=(table_m.group(1).lower() if table_m else None),
                path=str(path),
            )
        repo_m = _RE_REPO_EXTENDS.search(text)
        if repo_m:
            imports = parse_imports(text)
            entity_token = repo_m.group(1)
            entity_fqn = resolve_fqn(entity_token, jt.package, imports)
            repo_by_fqn[jt.fqn] = RepoInfo(fqn=jt.fqn, entity_fqn=entity_fqn, path=str(path))

    # 3) Dependency graph: type -> injected types (Service/Repository only)
    deps: dict[str, set[str]] = {}
    for fqn, jt in types_by_fqn.items():
        text = java_text_cache.get(fqn, "")
        imports = parse_imports(text)
        dep_tokens = find_injected_type_tokens(text)
        resolved: set[str] = set()
        for token in dep_tokens:
            resolved.add(resolve_fqn(token, jt.package, imports))
        deps[fqn] = resolved

    # 4) Resolve tables reachable from a type (controller/service/repo)
    table_cache: dict[str, set[str]] = {}
    unknown_entity_tables: set[str] = set()

    def tables_for_type(fqn: str, stack: set[str]) -> set[str]:
        if fqn in table_cache:
            return table_cache[fqn]
        if fqn in stack:
            return set()
        stack.add(fqn)

        out: set[str] = set()
        # If this is a service interface injected via DI, fan-out to its impl(s).
        for impl in service_impls_by_interface.get(fqn, set()):
            out |= tables_for_type(impl, stack)
        if fqn in repo_by_fqn:
            entity_fqn = repo_by_fqn[fqn].entity_fqn
            if entity_fqn and entity_fqn in entity_by_fqn:
                t = entity_by_fqn[entity_fqn].table
                if t:
                    out.add(t)
                else:
                    unknown_entity_tables.add(entity_fqn)
            else:
                # repo points to entity we didn't index (or not an @Entity)
                if entity_fqn:
                    unknown_entity_tables.add(entity_fqn)
        for dep in deps.get(fqn, set()):
            out |= tables_for_type(dep, stack)

        stack.remove(fqn)
        table_cache[fqn] = out
        return out

    # 5) Extract endpoints and attach tables
    endpoints: list[Endpoint] = []
    for ctl in controllers:
        text = java_text_cache.get(ctl.fqn, "")
        base = controller_base_path(text)
        explicit_api = controller_explicit_api_prefix(text)
        if explicit_api is not None:
            # controller already includes /api... in its own @RequestMapping
            base_prefix = explicit_api
        else:
            base_prefix = normalize_path("/api/" if controller_has_api_prefix(text) else "", base)
        for http_method, full_path in parse_endpoints_from_controller(text, base_prefix):
            tables_used = sorted(tables_for_type(ctl.fqn, set()))
            endpoints.append(
                Endpoint(
                    controller_fqn=ctl.fqn,
                    http_method=http_method,
                    path=full_path,
                    category=endpoint_category(full_path),
                    tables=tables_used,
                )
            )

    api_tables = sorted({t for e in endpoints for t in e.tables})
    api_tables_set = set(api_tables)

    # 6) Web vs App split (by endpoint path heuristic)
    web_tables = sorted({t for e in endpoints if e.category == "web" for t in e.tables})
    app_tables = sorted({t for e in endpoints if e.category == "app" for t in e.tables})
    system_tables = sorted({t for e in endpoints if e.category == "system" for t in e.tables})

    # 7) Focus sync: ez_/eil_ tables
    def prefixed(prefix: str) -> list[str]:
        return [t for t in db_tables if t.startswith(prefix)]

    ez_tables = prefixed("ez_")
    eil_tables = prefixed("eil_")

    entity_tables = sorted({info.table for info in entity_by_fqn.values() if info.table})
    entity_tables_set = set(entity_tables)

    def coverage_breakdown(table_list: list[str]) -> dict[str, list[str]]:
        s = set(table_list)
        has_entity = sorted(s & entity_tables_set)
        has_api = sorted(s & api_tables_set)
        entity_no_api = sorted((s & entity_tables_set) - api_tables_set)
        api_no_entity = sorted((s & api_tables_set) - entity_tables_set)
        no_entity_no_api = sorted(s - entity_tables_set - api_tables_set)
        return {
            "has_entity": has_entity,
            "has_api": has_api,
            "entity_no_api": entity_no_api,
            "api_no_entity": api_no_entity,
            "no_entity_no_api": no_entity_no_api,
        }

    result = {
        "schema": schema,
        "mysql_container": args.container,
        "db_tables_total": len(db_tables),
        "entities_total": len(entity_by_fqn),
        "entity_tables_total": len(entity_tables),
        "repos_total": len(repo_by_fqn),
        "controllers_total": len(controllers),
        "endpoints_total": len(endpoints),
        "api_tables_total": len(api_tables),
        "api_tables_coverage_pct": pct(len(api_tables), len(db_tables)),
        "web_tables_total": len(web_tables),
        "app_tables_total": len(app_tables),
        "system_tables_total": len(system_tables),
        "tables_in_both_web_and_app_total": len(set(web_tables) & set(app_tables)),
        "unknown_entity_tables": sorted(unknown_entity_tables),
        "coverage_by_group": [],
        "sync_focus": {
            "ez_": {
                "db_total": len(ez_tables),
                "entity_covered": len(set(ez_tables) & entity_tables_set),
                "api_covered": len(set(ez_tables) & api_tables_set),
                "api_pct": pct(len(set(ez_tables) & api_tables_set), len(ez_tables)),
                "detail": coverage_breakdown(ez_tables),
            },
            "eil_": {
                "db_total": len(eil_tables),
                "entity_covered": len(set(eil_tables) & entity_tables_set),
                "api_covered": len(set(eil_tables) & api_tables_set),
                "api_pct": pct(len(set(eil_tables) & api_tables_set), len(eil_tables)),
                "detail": coverage_breakdown(eil_tables),
            },
        },
        "endpoints": [asdict(e) for e in endpoints] if args.json else None,
    }

    # coverage by group (DB -> API tables)
    grouped: dict[str, set[str]] = {}
    for t in db_tables:
        grouped.setdefault(group_table(t), set()).add(t)
    for g in sorted(grouped.keys()):
        total = len(grouped[g])
        covered = len(grouped[g] & api_tables_set)
        result["coverage_by_group"].append(
            {"group": g, "api_covered": covered, "total": total, "pct": pct(covered, total)}
        )

    if args.json:
        print(json.dumps(result, indent=2, ensure_ascii=False))
        return 0

    print(f"Schema: {schema}")
    print(f"DB tables: {len(db_tables)}")
    print(f"Controllers: {len(controllers)} | Endpoints: {len(endpoints)}")
    print(f"API tables (tables reachable via controller->service/repo): {len(api_tables)} ({pct(len(api_tables), len(db_tables))})")
    print(f"Web tables: {len(web_tables)} | App tables: {len(app_tables)} | Both: {len(set(web_tables) & set(app_tables))}")
    print("")
    print("Sync focus:")
    for k in ("ez_", "eil_"):
        block = result["sync_focus"][k]
        print(
            f"- {k}: DB {block['db_total']}, entity {block['entity_covered']}, api {block['api_covered']} ({block['api_pct']})"
        )
        missing = block["detail"]["no_entity_no_api"]
        if missing:
            print(f"  - no entity & no api ({len(missing)}): {', '.join(missing[:12])}{' ...' if len(missing) > 12 else ''}")
        entity_no_api = block["detail"]["entity_no_api"]
        if entity_no_api:
            print(f"  - has entity but no api ({len(entity_no_api)}): {', '.join(entity_no_api[:12])}{' ...' if len(entity_no_api) > 12 else ''}")
    if unknown_entity_tables:
        print("")
        print(f"Unresolved entity tables (repo->entity missing @Table/index) ({len(unknown_entity_tables)}):")
        for t in sorted(unknown_entity_tables)[:20]:
            print(f"  - {t}")
        if len(unknown_entity_tables) > 20:
            print("  ...")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
