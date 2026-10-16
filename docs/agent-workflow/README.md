# Agent workspace

Prepared 2026-10-08; updated after approval of the phased remaster package and
documentation-alignment fixes. Start at [REMASTER_PLAN](../../REMASTER_PLAN.md).
Setup is complete, but each implementation prompt still requires its own launch.
Neither this setup nor the plan grants blanket approval for feature removal,
new architecture, a final design, sales or deployment.

## Start here

1. Follow [AGENTS.md](../../AGENTS.md); for non-trivial code/architecture work read [GRAPH_REPORT.md](../../GRAPH_REPORT.md) first.
2. Pick one row in [CONTEXT_ROUTER.md](CONTEXT_ROUTER.md). Read the minimum relevant documents and code, not the whole repository.
3. For resuming product planning, read [SESSION_HANDOFF.md](SESSION_HANDOFF.md).
4. Use [TEAM_REVIEW.md](TEAM_REVIEW.md) only for a requested independent product/design review.

## Installed skills

| Skill | Purpose | Source |
| --- | --- | --- |
| `the-system-product-lab` | Product value, useful creative alternatives, ethical progression, validation | Project-authored |
| `the-system-native-ux` | Native user journeys, clear copy, accessibility, tokens and purposeful motion | Project-authored |
| `the-system-figma-design` | Editable mobile Figma concepts, genuinely different UX compositions, action parity and render gates | Project-authored, added 2026-10-13 |
| `the-system-quality-review` | Independent risk review, evidence and regression gates | Project-authored |
| `edge-to-edge` | Compose system bars, navigation/keyboard insets | Google Android skills |
| `android-profiler` | Recording/analysing performance evidence when requested | Google Android skills |
| `testing-setup` | Test strategy/infrastructure changes when requested | Google Android skills |

All seven are installed under `.agents/skills/`, not globally. Normal automatic
selection remains enabled; an explicit `$the-system-product-lab` mention can
select a skill. The original six were visible in the active skill registry on 2026-10-08.
The new Figma skill is readable now and should be discovered on the next turn;
automatic registry discovery has not yet been observed. Official Figma provider
skills are already available through the connected MCP server; see
[FIGMA_SKILLS](../design/FIGMA_SKILLS.md), not duplicate installations.
In a future session, distinguish registry availability from file integrity;
if discovery fails, reopen the project/chat before claiming a skill was loaded.

The built-in `skill-creator`, `skill-installer` and image-generation capability
remain available; they are not duplicated here. No new MCP connection, account,
API key, global npm package or Android library was added.

## Boundaries

- Project intent lives in the [product-review index](../product-review/2026-10-08/README.md). Code remains evidence of actual behavior, not proof of desired positioning.
- AI suggests; domain validation decides. No skill can bypass that contract.
- Upstream recipes are not project-wide change orders. In particular, do not upgrade SDK/AGP or install a new testing stack just because an upstream skill suggests it.
- `android-profiler` includes a Python wrapper that can download and execute a Perfetto binary on invocation. It was inspected but NOT executed during setup. Recheck the pinned helper before use, respect network/filesystem approvals and keep traces/fitness data local.
- Translate upstream Unix examples to the actual Windows environment. Do not clear app data, uninstall the app, or reset a device without authorization for that operation.
- Team roles are prepared task briefs, not permanently running agents. They do not incur work until a review is requested.
- Existing `.codex/config.toml` permission/model settings are untouched. These documents do not override the active sandbox.

## Deliberately not installed

UI UX Pro Max and the full Superpowers package remain optional references, not
active instruction sources. They include broad/web-oriented or process-heavy
recipes that are unnecessary for this initial native, token-conscious setup.
No generic React/full-stack pack is installed. Backend design requires a real
server-side product requirement, not a title in the AI team.

## Verification and maintenance

Run `scripts/check-agent-skills.cmd` to check required files and exact vendor
hashes. The lock records the upstream commit, paths and SHA-256 values. Vendor
files remain unchanged, with the upstream Apache-2.0 license copied into each
skill. `.agents/skills/.gitattributes` preserves their bytes across checkouts.

When updating vendor skills: review a fixed commit in temporary storage, inspect
entry points/helpers/license, compare changes, then replace only the selected
skills and refresh the lock. Do not auto-update from `main` or overwrite local
skills. Validate custom skill frontmatter with the bundled `skill-creator`
validator and review trigger behavior; a hash check is not a security audit.

For a docs-only task in a dirty tree, `check-doc-only.cmd` can fail on earlier
Kotlin changes. Preserve them and compare task-start/task-end source hashes;
never report the wrapper as passing or reset files to make it pass.

References: [Codex skills](https://learn.chatgpt.com/docs/build-skills),
[subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents),
[Android skills](https://github.com/android/skills/tree/42dc2270e96032bd860bb94511e440aa00a43125).
