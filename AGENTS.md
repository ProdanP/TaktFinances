<!-- takt:start -->
## Takt

When the user invokes Takt or asks to continue an open bolt, follow `.agents/skills/takt/SKILL.md`.
Do not start a bolt unless asked. An open bolt is a directory under `docs/bolts/` whose `state.yaml` stage is not `done`.
If a missing answer would change the outcome, the behavior, or the depth of a bolt, ask before writing. Do not guess a blocking unknown.
Resume by running `python3 .agents/skills/takt/scripts/takt.py next` from the project root.
<!-- takt:end -->
