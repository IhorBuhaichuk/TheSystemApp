# UI_UX_GUIDELINES.md

Visual reference for **THE SYSTEM: LEVEL UP** Android UI.

This file is an aesthetic guide only. It does not authorize web UI code.

## Hard Compose-Only Rule

Файл `UI_UX_GUIDELINES.md` є ВИКЛЮЧНО естетичним довідником. КАТЕГОРИЧНО ЗАБОРОНЕНО генерувати код на React, HTML, CSS, Tailwind або використовувати веб-бібліотеки типу Framer Motion чи 21st.dev. Весь UI має генеруватися виключно нативним Kotlin з використанням Jetpack Compose.

## Core Mood

- Dark anime sci-fi HUD with RPG progression energy.
- Premium dark techno-metal and tinted glass, not flat plastic.
- Neon accents should feel like controlled instrument light, not full-screen bloom.
- The player should feel like they are operating a personal leveling system: precise, tactical, sharp, and alive.

## Color Direction

Use `SystemTheme.colors` and `SystemTheme.material` before adding any new color.

- Background: near-black blue/graphite.
- Primary accent: cyan/blue for active system state, progress, selected tabs, primary actions.
- AI accent: violet/purple for Architect and analysis flows.
- Success accent: green for completed tasks, streaks, positive bonuses.
- Warning accent: amber for caution, soft attention, deadlines.
- Error accent: red/pink for missed, blocked, dangerous, or penalty states.
- Text hierarchy: high-contrast primary, cooler secondary gray-blue, muted metadata.

Avoid:

- One-note purple or blue screens where every element has the same hue.
- Oversaturated RGB-gaming glow.
- Bright backgrounds that break the dark HUD identity.
- Hardcoded per-Composable colors when a token can own the effect.

## Surfaces and Materials

Preferred primitives:

- `SystemPanel`
- `techSurface`
- `DarkGlassCard`
- `SystemButton`
- `SystemHexIcon`
- `SystemProgressBar`
- Shared dialog/bottom-nav components

Surface language:

- Large panels use the shared large rounded shape from the system surface layer.
- Raised objects need subtle depth: ambient shadow, contact shadow, top-left edge highlight, bottom-right shade.
- Active panels may reflect a local cyan/violet glow, but glow must stay soft and local.
- Inner gradients should be barely visible: enough to imply material, never enough to compete with content.

## Layout Rhythm

Use existing spacing tokens:

- `SystemScreenPadding` for screen edges.
- `SystemCardPadding` for large card interiors.
- `SystemItemSpacing` for vertical rhythm between sections.
- Existing component dimensions should be preserved unless the task explicitly requests layout work.

For polish passes:

- Do not change navigation order, section order, content meaning, or text hierarchy.
- Treat the same screen as the same UI under better studio lighting.
- If an effect repeats, move it into a shared token/modifier/component.

## Typography

- Section headers may use uppercase cyber/HUD tone.
- Big numbers should feel like RPG stats: bold, readable, high contrast.
- Metadata should stay compact and muted.
- Avoid tiny low-contrast body copy on glass surfaces.
- Never let text overflow buttons or cards; use `maxLines`, `overflow`, and stable dimensions.

## Cards, Buttons, and Icons

- Cards are physical panels, not decorative boxes.
- Buttons should feel pressable, with defined edge, contact shadow, and controlled active glow.
- Icons should remain semantic. Use existing Material icons where available.
- Hexagon and panel motifs are part of the identity; use them sparingly as signal elements.
- Repeated list items can be plates; major sections should be full large panels.

## Motion

Use `SystemTheme.motion` or existing animation conventions.

- Prefer subtle breathing/glow/progress motion.
- Keep transitions quick and tactical.
- Avoid heavy blur, bloom, elastic gimmicks, or motion that delays task completion.

## Feature Tone

- Status: command center, daily quest, readiness, player identity.
- Calendar: tactical schedule, clean date grid, training/rest signal clarity.
- System/Cycle: machine room for training cycle setup and active plan logic.
- Statistics: analysis cockpit with charts, rank, proof, and progression.
- Architect: AI operator, violet/cyan analytical energy, never chaotic.
- Profile: identity, settings, backup/export, personal system controls.

## Implementation Reminder

All UI must be native Kotlin with Jetpack Compose. If a visual idea sounds like CSS, Tailwind, React, Framer Motion, or a web component library, translate the intent into existing Compose tokens and primitives instead.
