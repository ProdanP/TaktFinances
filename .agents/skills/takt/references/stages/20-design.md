# Design

Write `design.md` from this skill's `assets/design.md` when the file does not exist yet.

A change requires this heading on its own line:

- `## Approach` — a short note on how the change will be made

A feature also requires:

- `## Non-goals`
- `## Acceptance criteria`

Do not add a design for a fix bolt. The router has no design stage there.

If two approaches would change the behavior or the acceptance criteria, follow the Questions section of `references/protocol.md` and stop before you write `design.md`.

## Figma

Read `docs/system/figma.md` in the project. When that file contains a `figma.com` URL, that URL is the only Figma file for this bolt. Draft the visible screens there before you finish `design.md`. Do not create a new Figma file. Use the Figma tools and skills in the session to draw the frames.

Add this heading on its own line:

- `## Figma` — the URL of the frame you drafted, in that same file, including `node-id`. Link the new frame, not the page URL copied from `docs/system/figma.md`. When this bolt does not change a screen, write `none` on its own line and do not create frames.

Record the link in `design.md` only. Do not add keys to `state.yaml`.

When `docs/system/figma.md` is missing or has no `figma.com` URL, skip this and do not add `## Figma`.

Stop after the artifact is written. Do not run `advance` in this turn. Do not implement the screens in this turn. The router leaves this stage only after `approve design`. When the project names a Figma file, the router reports `figma frame missing` until `## Figma` links a new frame or contains a line that is exactly `none`.
