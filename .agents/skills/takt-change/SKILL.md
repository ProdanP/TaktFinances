---
name: takt-change
description: Start a Takt change bolt for a modest change that needs a short design. Use only when the user explicitly asks for a Takt change or invokes takt-change.
---

# Takt change

Start a change bolt, then hand off to the `takt` conductor for a single stage.

1. Choose a slug of lowercase words separated by hyphens from the user's request.
2. Run the sibling `takt` skill's router:

```bash
python3 "<takt-skill>/scripts/takt.py" --root "<project-root>" start --depth change --slug <slug>
```

3. Follow the `takt` skill. Do only the stage `next` prints, then stop.
