---
name: the-system-native-ux
description: "Review or improve THE SYSTEM native Android journeys, UX copy, accessibility, visual tokens and purposeful motion. Use for Compose UI/UX work, not backend or unrelated product audits."
---

# Native UX

## Route the task

Use repository AGENTS.md and [CONTEXT_ROUTER](../../../docs/agent-workflow/CONTEXT_ROUTER.md).
Read [UI guidelines](../../../UI_UX_GUIDELINES.md) and the relevant UI/bugfix/new-feature
playbook. Use GRAPH_REPORT and the relevant FEATURE_MAP row for connected behavior.
For a small surface fix, skip broad product research.

Distinguish surface-only polish, responsive bugfix and approved journey redesign.
If unspecified, preserve existing layout, content, actions and behavior; discuss
structural proposals separately. Audit screenshots only if actually supplied or
captured; do not invent runtime observations from code.

## Native and recognizable

- Kotlin and Jetpack Compose only. No React, HTML, CSS, Tailwind, web mockups or web animation libraries, even as a throwaway prototype.
- Reuse SystemTheme, existing tokens, SystemPanel, techSurface, SystemButton and shared dialogs/navigation. One shared owner per repeated visual effect; no copied inline styling.
- Preserve the current large-panel rounding and visual hierarchy unless the task approves a change. Keep restrained metal/glass depth; do not equate creativity with extra glow, shaders or dependencies.
- Keep state/decisions in the ViewModel/domain. UI renders state and sends actions. Preserve logging paths, dialogs and stable action contracts.

## User journey and creative direction

Check what the user wants to do, whether the next action is clear, and whether
the result explains what happened. Review loading, empty, error, unavailable,
offline and incomplete-data states relevant to the task.

Use plain language; a person unfamiliar with anime must understand the app.
Explore identity through restrained materials, meaningful progress, honest
explanations and quick motion feedback. Animation must communicate a state change
without delaying logging; respect reduced/disabled animation preferences.
Do not add mandatory mascot, story, reward screen or new setting just for novelty.

## Verification proportional to the change

- Preserve vertical scrolling; horizontal navigation must require horizontal intent. Respect system/IME/navigation insets.
- Check important actions and text at 360x640 dp and a normal phone size, with font scales 1.0 and 1.3. Use readable native sizing, not viewport-scaled fonts or ellipsis hiding critical content.
- Review semantic labels, focus order, touch targets, contrast, non-color status cues and TalkBack for affected controls.
- Compile/focused tests for code changes; add behavior tests/tags when state or interaction changes. Inspect actual screenshots and interactions when visual verification is required.
- Do not infer device success from compilation, screenshots from previews, or user comprehension from AI preference. If no device is available, finish non-device checks and record the exact deferred gate.

Use installed `edge-to-edge` only for relevant inset problems. Use profiling or
image-generation tools only when the requested work benefits; no automatic
Perfetto downloads, artwork generation or new animation stack on every UI task.

Report preserved behavior, changes/proposals, inspected evidence and checks.
In review/concept mode do not modify UI until implementation is approved.
