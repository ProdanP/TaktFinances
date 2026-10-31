# Build

Implement the bolt. If a task can be done in two ways that change behavior, follow the Questions section of `references/protocol.md` and stop before you edit code.

On a change or feature, check off each finished item in `tasks.md` by replacing `- [ ]` with `- [x]`. At least one task must be checked before this stage can advance. A fix has no task list.

Write `build.md` from this skill's `assets/build.md` when the file does not exist yet. Required heading on its own line:

- `## Notes` — what changed, and anything you left undone

Run the project's tests if it has them. Do not open the verify stage in this turn. When `check` prints `ok`, run `advance`, then stop.
