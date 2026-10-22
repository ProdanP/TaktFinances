---
name: takt-fix
description: Start a Takt fix bolt for a small defect. Use only when the user explicitly asks for a Takt fix or invokes takt-fix.
---

# Takt fix

Start a fix bolt, then hand off to the `takt` conductor for a single stage.

1. Choose a slug of lowercase words separated by hyphens from the user's request.
2. Run the sibling `takt` skill's router:

```bash
python3 "<takt-skill>/scripts/takt.py" --root "<project-root>" start --depth fix --slug <slug>
```

3. Follow the `takt` skill. Do only the stage `next` prints, then stop.
