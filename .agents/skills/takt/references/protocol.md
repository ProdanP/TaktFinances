# Takt protocol

Takt keeps intent, design, implementation, and verification in the repository. Chat is scratch space. A new session resumes from the bolt directory.

## Authority

`scripts/takt.py` names the next stage. Do not skip ahead, merge stages, or invent a stage. If this prose and the router disagree, the router wins.

## Depths

A bolt is one branch-sized piece of work. The depth is chosen at the start and stored in `state.yaml`.

- **fix:** intake, intent, build, verify, learn, done. No design and no task list.
- **change:** intake, intent, design, tasks, build, verify, learn, done. Design is a short approach.
- **feature:** the same stages as change. Design must include non-goals and acceptance criteria.

## Gates

- After `intent.md` is written, stop. The router will not leave intent until `approve intent` has been run.
- After `design.md` is written, stop. The router will not leave design until `approve design` has been run.
- Verify cannot finish unless every acceptance checkbox in `verify.md` is checked and the recorded test command exited 0.

## Questions

Before writing an artifact or code, check whether you are about to guess.

Ask when an unanswered question would change the outcome, the behavior, the files you would touch, or whether this bolt is a fix, a change, or a feature. Ask when the request has two plausible readings. Ask when a constraint is missing and the repository does not already answer it.

Proceed without a question when the user's message, the bolt files, or the code already decide it. When a local choice follows an existing pattern in the repository, record that assumption in the artifact instead of asking.

Ask the blocking questions in one batch. Number them. Under each question, state the assumption you will use if the user tells you to proceed. Then stop. Wait for an answer, or for the user to accept those assumptions, before you write the artifact or edit code.

## One stage per turn

Do the stage `next` prints, then stop. When `next` prints `action: wait`, the gate is closed: do not run `advance` and do not start the next stage in that turn.

Bookkeeping is not a stage. If `next` prints `action: advance`, run `advance` and consult `next` again.

## Done

A bolt is done only when `advance` moves `learn` to `done`. On the learn stage, fold a short delta into `docs/system/overview.md` when behavior changed. Append one line to `docs/learnings.md` only when something surprised you.

## Bolt files

```text
docs/bolts/<YYYY-MM-DD-slug>/
  state.yaml
  intake.md
  intent.md
  design.md    # change and feature
  tasks.md     # change and feature
  build.md
  verify.md
  learn.md
```
