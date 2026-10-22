---
name: takt-verify
description: Verify a Takt bolt from its files, the diff, and a real test command. Use only when the user asks to verify a Takt bolt or invokes takt-verify.
---

# Takt verify

Check a bolt from files on disk. A summary from the builder is not evidence.

1. Run the sibling `takt` skill's router: `next`, plus `--bolt docs/bolts/<id>` when the user named a bolt.
2. Continue only when `stage` is `verify`. If the bolt is earlier, stop and name the open stage. If it is later, stop and say verification has already advanced.
3. Read `intent.md`, `design.md` when present, `tasks.md` when present, `git status`, and `git diff`.
4. Run the project's test command yourself. Record the command and the exit code you observed.
5. Follow the sibling file `takt/references/stages/50-verify.md` and write `verify.md` from `takt/assets/verify.md`.
6. Run `check`. When it prints `ok`, run `advance`, then stop. Do not start the learn stage in this turn.
