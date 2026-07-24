# UI Polish Playbook

Use for visual polish, styling consistency, spacing review, screen restyling, and sci-fi/HUD material work.

## Read First

1. `GRAPH_REPORT.md`
2. `UI_UX_GUIDELINES.md`
3. Target Compose file and shared primitives it already uses.

## Rules

- Keep UI native Kotlin + Jetpack Compose only.
- Do not create React, HTML, CSS, Tailwind, Framer Motion, 21st.dev, or web prototypes.
- Prefer central tokens and shared primitives over inline one-off styling.
- Reuse `SystemTheme`, `SystemPanel`, `techSurface`, `DarkGlassCard`, `SystemButton`, `SystemProgressBar`, and bottom-nav/dialog components.
- If an effect repeats in multiple places, extract it into a shared token, modifier, or component.

## Guardrails

- Do not accidentally remove existing actions, dialogs, logs, navigation paths, or ViewModel state collection.
- Do not change business logic while doing visual polish.
- Preserve layout unless the task explicitly asks for redesign.
- For dark sci-fi screens, use controlled local glow and material depth, not full-screen neon bloom.

## Verification

- Run `scripts/check-quick.cmd`.
- If visual risk is high, launch the app and inspect the target screen after compile succeeds.
