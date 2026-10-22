#!/usr/bin/env python3
"""Route a Takt bolt. The model does the work inside a stage; this script names the stage."""

from __future__ import annotations

import argparse
import json
import re
import shlex
import sys
from datetime import datetime
from pathlib import Path

DEPTHS: dict[str, list[str]] = {
    "fix": ["intake", "intent", "build", "verify", "learn", "done"],
    "change": ["intake", "intent", "design", "tasks", "build", "verify", "learn", "done"],
    "feature": ["intake", "intent", "design", "tasks", "build", "verify", "learn", "done"],
}

STAGE_FILES = {
    "intake": "00-intake.md",
    "intent": "10-intent.md",
    "design": "20-design.md",
    "tasks": "30-tasks.md",
    "build": "40-build.md",
    "verify": "50-verify.md",
    "learn": "60-learn.md",
}

ARTIFACTS = {
    "intake": "intake.md",
    "intent": "intent.md",
    "design": "design.md",
    "tasks": "tasks.md",
    "build": "build.md",
    "verify": "verify.md",
    "learn": "learn.md",
}

HEADINGS: dict[str, list[str]] = {
    "intake": ["## Depth", "## Summary"],
    "intent": ["## Problem", "## Done when"],
    "tasks": ["## Tasks"],
    "build": ["## Notes"],
    "verify": ["## Result", "## Test", "## Acceptance"],
    "learn": ["## Learning"],
}

DESIGN_HEADINGS = {
    "change": ["## Approach"],
    "feature": ["## Approach", "## Non-goals", "## Acceptance criteria"],
}

SKILL_ROOT = Path(__file__).resolve().parent.parent
STAGES_DIR = SKILL_ROOT / "references" / "stages"
SLUG_RE = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
CHECKBOX_RE = re.compile(r"^- \[([ xX])\]\s*(.*)$")
STATE_KEYS = (
    "id",
    "slug",
    "depth",
    "stage",
    "created",
    "approvals",
    "test_command",
    "test_exit_code",
    "acceptance_checked",
)


class TaktError(Exception):
    """A bolt is missing, invalid, or not ready to advance."""


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    try:
        if args.command == "start":
            print(cmd_start(args.root, args.depth, args.slug, args.date))
        elif args.command == "next":
            print(cmd_next(args.root, args.bolt))
        elif args.command == "approve":
            print(cmd_approve(args.root, args.gate, args.bolt))
        elif args.command == "check":
            print(cmd_check(args.root, args.bolt))
        elif args.command == "advance":
            print(cmd_advance(args.root, args.bolt))
        else:
            parser.error(f"unknown command: {args.command}")
    except TaktError as exc:
        print(exc, file=sys.stderr)
        return 1
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="takt")
    parser.add_argument("--root", type=Path, default=Path.cwd())
    sub = parser.add_subparsers(dest="command", required=True)

    start = sub.add_parser("start")
    start.add_argument("--depth", required=True, choices=sorted(DEPTHS))
    start.add_argument("--slug", required=True)
    start.add_argument("--date")

    for name in ("next", "check", "advance"):
        command = sub.add_parser(name)
        command.add_argument("--bolt")

    approve = sub.add_parser("approve")
    approve.add_argument("gate", choices=("intent", "design"))
    approve.add_argument("--bolt")
    return parser


def cmd_start(root: Path, depth: str, slug: str, created: str | None) -> str:
    root = root.resolve()
    if not SLUG_RE.fullmatch(slug):
        raise TaktError(f"invalid slug: {slug}")
    if created is None:
        created = datetime.now().date().isoformat()
    try:
        datetime.strptime(created, "%Y-%m-%d")
    except ValueError as exc:
        raise TaktError(f"invalid date: {created}") from exc
    bolt_id = f"{created}-{slug}"
    bolt = root / "docs" / "bolts" / bolt_id
    if bolt.exists():
        raise TaktError(f"bolt already exists: {rel(root, bolt)}")
    state = {
        "id": bolt_id,
        "slug": slug,
        "depth": depth,
        "stage": "intake",
        "created": created,
        "approvals": {"intent": False, "design": False},
        "test_command": "",
        "test_exit_code": None,
        "acceptance_checked": False,
    }
    bolt.mkdir(parents=True)
    save_state(bolt / "state.yaml", state)
    return "\n".join(
        [
            f"created: {rel(root, bolt)}",
            f"depth: {depth}",
            "stage: intake",
        ]
    )


def cmd_next(root: Path, bolt_arg: str | None) -> str:
    root = root.resolve()
    bolt = find_bolt(root, bolt_arg)
    state = load_state(bolt / "state.yaml")
    return render_next(root, bolt, state)


def cmd_approve(root: Path, gate: str, bolt_arg: str | None) -> str:
    root = root.resolve()
    bolt = find_bolt(root, bolt_arg)
    state = load_state(bolt / "state.yaml")
    if gate == "design" and "design" not in DEPTHS[state["depth"]]:
        raise TaktError("fix bolts have no design gate")
    state["approvals"][gate] = True
    save_state(bolt / "state.yaml", state)
    return f"approved: {gate}"


def cmd_check(root: Path, bolt_arg: str | None) -> str:
    root = root.resolve()
    bolt = find_bolt(root, bolt_arg)
    state = load_state(bolt / "state.yaml")
    if state["stage"] == "done":
        raise TaktError("bolt is complete")
    found = problems(state, bolt)
    if found:
        raise TaktError("\n".join(found))
    return f"ok: {state['stage']}"


def cmd_advance(root: Path, bolt_arg: str | None) -> str:
    root = root.resolve()
    bolt = find_bolt(root, bolt_arg)
    state = load_state(bolt / "state.yaml")
    stages = DEPTHS[state["depth"]]
    if state["stage"] == "done":
        raise TaktError("bolt is complete")
    found = problems(state, bolt)
    if found:
        raise TaktError("\n".join(found))
    current = state["stage"]
    state["stage"] = stages[stages.index(current) + 1]
    save_state(bolt / "state.yaml", state)
    return f"advanced: {current} -> {state['stage']}"


def render_next(root: Path, bolt: Path, state: dict) -> str:
    stage = state["stage"]
    header = [
        f"bolt: {rel(root, bolt)}",
        f"depth: {state['depth']}",
        f"stage: {stage}",
    ]
    if stage == "done":
        header.append("action: done")
        header.append("message: Bolt is complete.")
        return "\n".join(header)
    found = problems(state, bolt)
    content = [item for item in found if not item.startswith("approval required:")]
    approvals = [item for item in found if item.startswith("approval required:")]
    if content:
        stage_file = STAGES_DIR / STAGE_FILES[stage]
        if not stage_file.is_file():
            raise TaktError(f"stage file missing: {stage_file}")
        header.extend(
            [
                "action: follow",
                f"follow: {stage_file}",
                f"artifact: {rel(root, bolt / ARTIFACTS[stage])}",
            ]
        )
        header.extend(f"pending: {item}" for item in content)
        return "\n".join(header)
    if approvals:
        gate = approvals[0].split(": ", 1)[1]
        header.extend(
            [
                "action: wait",
                f"waiting: {gate}",
                "message: "
                + " ".join(
                    [
                        f"Stop until the user approves {gate}.",
                        "Then run:",
                        router_command(root, bolt, "approve", gate),
                    ]
                ),
            ]
        )
        return "\n".join(header)
    header.extend(
        [
            "action: advance",
            "message: "
            + " ".join(
                [
                    f"Stage {stage} is complete.",
                    "Run:",
                    router_command(root, bolt, "advance"),
                    "Then stop.",
                ]
            ),
        ]
    )
    return "\n".join(header)


def router_command(root: Path, bolt: Path, *extra: str) -> str:
    parts = [
        "python3",
        shlex.quote(str(Path(__file__).resolve())),
        "--root",
        shlex.quote(str(root)),
        *extra,
        "--bolt",
        shlex.quote(rel(root, bolt)),
    ]
    return " ".join(parts)


def problems(state: dict, bolt: Path) -> list[str]:
    stage = state["stage"]
    if stage == "done":
        return []
    found: list[str] = []
    if stage == "design":
        headings = DESIGN_HEADINGS[state["depth"]]
        artifact = ARTIFACTS["design"]
    else:
        headings = HEADINGS[stage]
        artifact = ARTIFACTS[stage]
    text = read_artifact(bolt, artifact, found)
    if text is not None:
        found.extend(missing_headings(text, headings, artifact))
    if stage == "tasks" and text is not None and not any(body for _, body in checkboxes(text)):
        found.append("task missing")
    if stage == "build" and state["depth"] in ("change", "feature"):
        task_text = (bolt / "tasks.md").read_text(encoding="utf-8") if (bolt / "tasks.md").is_file() else ""
        if not any(checked and body for checked, body in checkboxes(task_text)):
            found.append("completed task missing")
    if stage == "verify" and text is not None and not any(item.startswith("missing heading:") for item in found):
        found.extend(verify_problems(text, state))
    if stage == "intent" and not state["approvals"]["intent"]:
        found.append("approval required: intent")
    if stage == "design" and not state["approvals"]["design"]:
        found.append("approval required: design")
    return found


def read_artifact(bolt: Path, name: str, found: list[str]) -> str | None:
    path = bolt / name
    if not path.is_file():
        found.append(f"missing file: {name}")
        return None
    return path.read_text(encoding="utf-8")


def missing_headings(text: str, headings: list[str], artifact: str) -> list[str]:
    lines = {line.strip() for line in text.splitlines()}
    return [f"missing heading: {heading} in {artifact}" for heading in headings if heading not in lines]


def verify_problems(text: str, state: dict) -> list[str]:
    found: list[str] = []
    command, exit_code = recorded_test(text, state)
    if not command:
        found.append("test command missing")
    elif exit_code != 0:
        found.append("test exit code is not 0")
    boxes = checkboxes(section(text, "## Acceptance") or "")
    if not any(body for _, body in boxes):
        found.append("acceptance lines missing")
    elif any(not checked and body for checked, body in boxes):
        found.append("unchecked acceptance line")
    return found


def recorded_test(text: str, state: dict) -> tuple[str, int | None]:
    body = section(text, "## Test") or ""
    command = ""
    exit_code: int | None = None
    for line in body.splitlines():
        if line.startswith("command:"):
            command = scalar_text(line.split(":", 1)[1])
        elif line.startswith("exit_code:"):
            raw = line.split(":", 1)[1].strip()
            if raw and raw != "null":
                try:
                    exit_code = int(raw)
                except ValueError:
                    exit_code = None
    if not command:
        command = state["test_command"]
    if exit_code is None:
        exit_code = state["test_exit_code"]
    return command, exit_code


def section(text: str, heading: str) -> str | None:
    lines = text.splitlines()
    start = None
    for index, line in enumerate(lines):
        if line.strip() == heading:
            start = index + 1
            break
    if start is None:
        return None
    chunk: list[str] = []
    for line in lines[start:]:
        if line.startswith("## "):
            break
        chunk.append(line)
    return "\n".join(chunk)


def checkboxes(text: str) -> list[tuple[bool, str]]:
    found = []
    for line in text.splitlines():
        match = CHECKBOX_RE.match(line.strip())
        if match:
            found.append((match.group(1).lower() == "x", match.group(2).strip()))
    return found


def find_bolt(root: Path, bolt_arg: str | None) -> Path:
    bolts = root / "docs" / "bolts"
    if bolt_arg:
        raw = Path(bolt_arg)
        candidates = [raw] if raw.is_absolute() else [root / raw, bolts / bolt_arg]
        for candidate in candidates:
            if (candidate / "state.yaml").is_file():
                resolved = candidate.resolve()
                if not resolved.is_relative_to(root):
                    raise TaktError(f"bolt is outside the project: {bolt_arg}")
                return resolved
        raise TaktError(f"bolt not found: {bolt_arg}")
    if not bolts.is_dir():
        raise TaktError("no open bolt")
    open_bolts = []
    all_bolts = []
    for state_path in sorted(bolts.glob("*/state.yaml")):
        all_bolts.append(state_path.parent)
        if load_state(state_path)["stage"] != "done":
            open_bolts.append(state_path.parent)
    if len(open_bolts) > 1:
        listed = "\n".join(rel(root, bolt) for bolt in open_bolts)
        raise TaktError(f"multiple open bolts:\n{listed}\nPass --bolt.")
    if len(open_bolts) == 1:
        return open_bolts[0].resolve()
    if len(all_bolts) == 1:
        return all_bolts[0].resolve()
    raise TaktError("no open bolt")


def load_state(path: Path) -> dict:
    if not path.is_file():
        raise TaktError(f"state file missing: {path}")
    data: dict = {"approvals": {}}
    parent = None
    for line_number, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        if not raw.strip() or raw.strip().startswith("#"):
            continue
        if raw.startswith("  "):
            if parent != "approvals":
                raise TaktError(f"invalid state at line {line_number}")
            key, value = split_pair(raw.strip(), line_number)
            if key not in ("intent", "design"):
                raise TaktError(f"invalid state at line {line_number}")
            data["approvals"][key] = parse_bool(value, line_number)
            continue
        parent = None
        key, value = split_pair(raw, line_number)
        if key == "approvals":
            if value:
                raise TaktError(f"invalid state at line {line_number}")
            parent = "approvals"
            continue
        if key == "test_command":
            data[key] = scalar_text(value)
        elif key == "test_exit_code":
            data[key] = None if value in ("", "null", "~") else parse_int(value, line_number)
        elif key == "acceptance_checked":
            data[key] = parse_bool(value, line_number)
        elif key in ("id", "slug", "depth", "stage", "created"):
            data[key] = value.strip()
        else:
            raise TaktError(f"invalid state at line {line_number}")
    for key in STATE_KEYS:
        if key not in data:
            raise TaktError(f"invalid state: {key}")
    for key in ("intent", "design"):
        if key not in data["approvals"]:
            raise TaktError(f"invalid state: approvals.{key}")
    if data["depth"] not in DEPTHS:
        raise TaktError(f"invalid depth: {data['depth']}")
    if data["stage"] not in DEPTHS[data["depth"]]:
        raise TaktError(f"invalid stage: {data['stage']}")
    return data


def save_state(path: Path, state: dict) -> None:
    exit_code = "null" if state["test_exit_code"] is None else str(state["test_exit_code"])
    path.write_text(
        "\n".join(
            [
                f"id: {state['id']}",
                f"slug: {state['slug']}",
                f"depth: {state['depth']}",
                f"stage: {state['stage']}",
                f"created: {state['created']}",
                "approvals:",
                f"  intent: {str(state['approvals']['intent']).lower()}",
                f"  design: {str(state['approvals']['design']).lower()}",
                f"test_command: {json.dumps(state['test_command'])}",
                f"test_exit_code: {exit_code}",
                f"acceptance_checked: {str(state['acceptance_checked']).lower()}",
                "",
            ]
        ),
        encoding="utf-8",
    )


def split_pair(raw: str, line_number: int) -> tuple[str, str]:
    if ":" not in raw:
        raise TaktError(f"invalid state at line {line_number}")
    key, value = raw.split(":", 1)
    return key.strip(), value.strip()


def parse_bool(value: str, line_number: int) -> bool:
    if value not in ("true", "false"):
        raise TaktError(f"invalid state at line {line_number}")
    return value == "true"


def parse_int(value: str, line_number: int) -> int:
    try:
        return int(value)
    except ValueError as exc:
        raise TaktError(f"invalid state at line {line_number}") from exc


def scalar_text(value: str) -> str:
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] == '"':
        return json.loads(value)
    return value


def rel(root: Path, path: Path) -> str:
    return path.resolve().relative_to(root.resolve()).as_posix()


if __name__ == "__main__":
    sys.exit(main())
