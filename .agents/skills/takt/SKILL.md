---
name: takt
description: Run the next Takt stage for this project. Use only when the user invokes Takt, asks to start or continue a Takt bolt, or a depth skill hands off to you.
---

# Takt conductor

Takt is a bolt lifecycle. Do one stage of work per turn. This skill's `scripts/takt.py` decides which stage that is. Read `references/protocol.md` for the gate rules. Do not invent stages and do not skip a gate.

## Clarifications

Follow the Questions section of `references/protocol.md` before you write.

Ask when an unanswered question would change the outcome, the behavior, the files you would touch, or the depth of this bolt. Ask when the request has two plausible readings, or when a constraint is missing and the repository does not already answer it.

Proceed without a question when the user's message, the bolt files, or the code already decide it. Record a local choice that follows an existing project pattern as an assumption in the artifact.

Ask the blocking questions in one numbered batch. Under each question, state the assumption you will use if the user tells you to proceed. Then stop. Wait for an answer, or for the user to accept those assumptions, before you write the artifact or edit code.

## Run the router

Use this skill's `scripts/takt.py`. Set `--root` to the project root, the directory that contains or will contain `docs/bolts`.

```bash
python3 "<this-skill>/scripts/takt.py" --root "<project-root>" next
```

If the user names a bolt, pass `--bolt docs/bolts/<id>`. If the router reports multiple open bolts, ask which one to continue.

## Act on the result

Read `action`.

- `advance` means the current stage is already complete. Run `advance`, then run `next` again.
- `wait` means stop. Tell the user which approval is missing (`intent` or `design`). Do not edit code and do not run `advance`. After they approve, run `approve <gate>` with the same `--root` and `--bolt`.
- `done` means tell the user the bolt is complete and stop.
- `follow` means read the stage file at `follow` and do only that stage. Copy the matching template from this skill's `assets/` directory when the artifact does not exist yet. Write it to the path in `artifact`.

## After a follow stage

- When the stage is `intent` or `design`, stop once the artifact is written. Tell the user what you assumed and that the bolt is waiting on approval. Do not run `advance` in this turn. On `design`, if `docs/system/figma.md` names a Figma file, draft the screens in that file before you finish the artifact.
- For every other stage, run `check`. Fix the files or headings it reports. When `check` prints `ok`, run `advance`, then stop. Report the stage you finished and the stage now stored in `state.yaml`.

## Starting a bolt

Start a bolt only when the user asks. If a depth skill already ran `start`, do not start another. Otherwise pick a depth:

- `fix` for a defect with a clear outcome and no design
- `change` for a modest change that needs a short approach
- `feature` for new behavior that needs non-goals and acceptance criteria

Choose a slug of lowercase words separated by hyphens, then:

```bash
python3 "<this-skill>/scripts/takt.py" --root "<project-root>" start --depth <depth> --slug <slug>
```

Run `next` and do that first stage.

## Resume

A new session has no transcript. Trust `docs/bolts/<id>/state.yaml` and the artifacts beside it. Do not ask the user to paste the old chat.
