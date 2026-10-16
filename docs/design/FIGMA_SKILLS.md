# Figma Skills / Mobile Quality Gates

Checked 2026-10-13. Scope: skill/workflow setup, not approval or completion of R04.

## Available Official Provider Skills

The connected official Figma MCP resource inventory exposes these skills; no
duplicate download, new account, API key, paid upgrade or Android library needed.
Read the indicated resource when using its operation, not every skill per task.

| Operation | Provider resources |
| --- | --- |
| Native screen composition | `skill://figma/figma-use/SKILL.md` + `skill://figma/figma-generate-design/SKILL.md` |
| Tokens, components and variants | `skill://figma/figma-use/SKILL.md` + `skill://figma/figma-generate-library/SKILL.md` |
| Approved Figma -> Compose implementation | `skill://figma/figma-design-to-code/SKILL.md` |
| Purposeful approved motion | `skill://figma/figma-use-motion/SKILL.md` or `skill://figma/figma-implement-motion/SKILL.md`, only when required |

Provider instructions are served by the connector, not pinned vendor files in
skills-lock.json. The lock's SHA-256 check covers local vendor skills, not these
live resources. Provider resource availability is not proof of usable write quota.

The previous R03/R04 sessions loaded the screen/library/use resources, but R04-v1
still failed the owner's UX review. Skills are procedural help, not extra model
intelligence, an independent design team or a guarantee of a good result.

## Project Adapter

[the-system-figma-design](../../.agents/skills/the-system-figma-design/SKILL.md)
is a new project-authored skill with mobile composition, action-parity and native
layer/binding checks. It works alongside the existing native-ux skill. Its files
are installed; automatic skill registry discovery is pending the next turn.
The instructions can be read and applied in the current turn without that claim.

Start with the selected prompt and [R04 rework contract](R04_REWORK_CONTRACT.md)
for the current revision; use [ACTION_INVENTORY](ACTION_INVENTORY.md) for control
meaning and [FIGMA_MAP](FIGMA_MAP.md) for existing components/nodes. Do not load
the full prompt pack or rescan Kotlin for every design tweak.

## Acceptance / What Skills Must Not Hide

- Three concepts differ in composition and interaction emphasis, not font size alone.
- The same facts, actions and existing route/event meanings survive in every concept.
- Phone layouts are inspected at 360x640 and 390x844; primary actions and scrolling
  account for bottom navigation. Each navigation item has its own centered label.
- Date cells and todo rows are real editable structured groups/components; honest
  data states and consistent counts replace concatenated text or empty score panels.
- Reuse R03 variables/components/styles. Verify native text, instances and bindings;
  SVG import or an attractive screenshot alone does not prove these properties.
- Capture actual Figma renders; track unresolved wiring, responsive and device gates.
- Owner choice remains required. No skill can approve R04, remove actions, change
  business logic, authorize a purchase or start later prompts automatically.

MCP write quota was blocked during the R04 rework; browser editing remains the
available route. Skills cannot lift plan limits. No further blocked write retry is
needed simply to verify that official skill resources exist.

No generic React/full-stack design pack is installed. Native Kotlin/Compose
constraints remain authoritative even when provider examples contain web code.

Official reference: [Figma guidance for custom skills](https://developers.figma.com/docs/figma-mcp-server/create-skills/).

## Setup Verification / 2026-10-13

- `check-agent-skills.cmd`: PASS, 7 local skills, 37 pinned vendor files match hashes.
- `check-remaster-plan.cmd`: PASS, 54 ordered prompts and 981 local links.
- `git diff --check`: PASS; existing source line-ending warnings only.
- Bundled `skill-creator/quick_validate.py`: could not run because its Python
  environment lacks PyYAML. Required frontmatter and UI metadata were inspected;
  the repository skill-integrity guard passed. No package installed to mask this limit.
- Task-start/end SHA-256 comparison: all 472 tracked/untracked Kotlin/KTS files
  unchanged by this setup. Existing P02/P03 source changes are preserved.
- `check-doc-only.cmd`: FAIL on those pre-existing Kotlin/KTS changes, not claimed
  as passing. No Android build, device test or new Figma render was performed in
  this skill-setup step. R04 remains in_progress/rework, not approved or complete.
