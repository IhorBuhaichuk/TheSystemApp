---
name: the-system-figma-design
description: "Create or review editable THE SYSTEM mobile Figma concepts with genuine UX alternatives, shared tokens and preserved actions. Use for Figma design and handoff, not routine Kotlin fixes."
---

# Mobile Figma Design

## When to use

Use for mobile screen concepts, component composition, Figma revisions and design
handoff. Follow AGENTS.md and the selected task in QUEUE; this skill does not
authorize implementing a design, changing business rules or approving a direction.
Use the-system-native-ux for Android implementation and accessibility details.

## Instructions

1. Read the selected prompt, relevant DESIGN_WORKFLOW/FIGMA_MAP entries and one
   ACTION_INVENTORY journey. For R04 read R04_REWORK_CONTRACT before historical
   R04_DIRECTIONS. Verify current approval, file/node and revision; do not reuse
   rejected directions as an accepted baseline.
2. Load official Figma provider skills only for the operation: figma-use with
   figma-generate-design for screens, figma-generate-library for components, or
   figma-design-to-code for an approved implementation. Do not duplicate all
   provider manuals into project context. Project Kotlin/Compose rules override
   generic web examples; no HTML/React prototype or web-capture workflow.
3. Before drawing, write the screen's main user goal and control -> existing
   event -> destination/outcome mapping. An attractive button must not invent a
   route, mutation or promise. Include cancel/back and disabled/unavailable states
   relevant to the proposal. Keep unresolved source gaps explicitly open.
4. For multiple UX proposals use identical facts and action coverage. Differentiate
   composition, grouping, scan path or action position, not just typography or
   accent colors. Describe the trade-off of each; preserve all required functions.
5. Inspect existing library components and token mapping first. Use editable text,
   real frame/auto-layout structure, component instances and semantic variables.
   Repeated navigation cells each own an icon and a centered label. A calendar is
   individual date cells; todos are individual managed rows, not one text block.
   Keep vectors for icons, not outlined body text or a rasterized whole screen.
6. Compose at 360x640 and 390x844 logical sizes, with fixed readable text sizes,
   explicit scroll/inset space and reachable primary actions. Check 48x48 Android
   action targets, label alignment, wrapping, counts and non-color state cues.
   Treat px-to-dp/sp as a documented mapping, not automatic device equivalence.
7. Use the established restrained metal/glass tokens. Audit hierarchy before
   polishing light/shadow; effects must not obscure content. Do not introduce
   new fonts, shaders, assets or dependencies just to make variants look different.
8. Inspect actual whole-frame renders and affected component states; correct
   clipping, collisions, missing controls and broken bindings before presenting.
   Log node/revision, observed checks and deferred evidence in FIGMA_MAP and task
   evidence. Compilation, Figma renders and owner preference do not prove Android
   interaction, TalkBack, fontScale behavior or user comprehension.

## Examples

Input: redo R04 after owner feedback. Output: three structurally distinct native
mobile proposals, a shared action-parity map and actual render evidence. Owner
choice remains pending; no Kotlin change or later task starts automatically.

## Common edge cases

- If MCP quota blocks writes, stop repeated calls. Use the authenticated browser
  editor if available and record the limit; do not buy an upgrade or change sharing.
- After any partial write inspect state before retrying to avoid duplicate nodes.
- Clipboard SVG import is design transport, not production UI. Verify imported
  text stays editable; restore native components, constraints and variable/style
  bindings before calling it a reusable design-system screen. Import alone is not
  proof of prototype wiring or responsive behavior.
- If the file cannot be inspected, record the missing evidence rather than invent
  node IDs, screenshots or successful checks. Preserve existing library masters.
- Requests for surface-only polish retain layout and copy. Approved UX rework
  permits only its specified structural/copy changes and preserves action meaning.

Reference paths are repo-relative: docs/design/DESIGN_WORKFLOW.md,
docs/design/FIGMA_MAP.md, docs/design/ACTION_INVENTORY.md,
docs/design/R04_REWORK_CONTRACT.md and docs/remaster/QUEUE.md.
