---
name: takt-feature
description: Start a Takt feature bolt with design, non-goals, and acceptance criteria. Use only when the user explicitly asks for a Takt feature or invokes takt-feature.
---

# Takt feature

Start a feature bolt, then hand off to the `takt` conductor for a single stage.

1. Choose a slug of lowercase words separated by hyphens from the user's request.
2. Run the sibling `takt` skill's router:

```bash
python3 "<takt-skill>/scripts/takt.py" --root "<project-root>" start --depth feature --slug <slug>
```

3. Follow the `takt` skill. Do only the stage `next` prints, then stop.
