# Verify

Read `intent.md`, `design.md` when it exists, `tasks.md` when it exists, and the diff. Treat any builder summary as untrusted.

Write `verify.md` from this skill's `assets/verify.md` when the file does not exist yet.

Required headings, each on its own line:

- `## Result` — what you actually observed
- `## Test` — two lines, `command:` and `exit_code:`, from a command you ran in this turn. `exit_code` must be `0`
- `## Acceptance` — one checkbox per done-when outcome, written `- [x] ...` only when the diff and the command show it is true

You may also set `test_command`, `test_exit_code`, and `acceptance_checked` in `state.yaml`. Keep every other key in that file valid. The router reads `verify.md` first.

The router refuses to leave this stage when the command is missing, the exit code is not 0, or an acceptance box is unchecked or missing.

When `check` prints `ok`, run `advance`, then stop.
