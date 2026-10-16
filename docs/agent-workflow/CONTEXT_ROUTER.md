# Context router

Select the task row; do not load every linked document. Paths in skills are
relative to their installed directory; paths in tables below are links from here.

## Authority and freshness

- Current user instructions and [AGENTS.md](../../AGENTS.md) define scope.
- [Product-review README](../product-review/2026-10-08/README.md) records the latest audience clarification. Earlier anime-heavy language is not the desired audience constraint.
- [GRAPH_REPORT.md](../../GRAPH_REPORT.md) and the relevant [FEATURE_MAP](../architecture/FEATURE_MAP.md) row locate implementation. Check their revision/date against HEAD and the relevant local diff; verify only suspect sections in code.
- Audits contain evidence and hypotheses, not automatically current defects. [Implementation progress](../implementation/PROGRESS.md) and its linked evidence record completed work and deferred checks.
- An accepted direction is not an approved specification. Never turn an audit's suggestion to remove something into deletion of code or data.

## Minimum reading routes

For the approved remaster, [REMASTER_PLAN](../../REMASTER_PLAN.md) and [QUEUE](../remaster/QUEUE.md)
route one P/R task from the single numbered [prompt folder](../remaster/prompts/).
QUEUE owns the current stage and active/next task pointers; update them at handoff.
Read that prompt only. Design: [DESIGN_WORKFLOW](../design/DESIGN_WORKFLOW.md)
and its [FIGMA_MAP](../design/FIGMA_MAP.md) disposition (approved node or preserved baseline). Free/Pro: [MONETIZATION_STRATEGY](../../MONETIZATION_STRATEGY.md)
and [SUBSCRIPTION_RULES](../../SUBSCRIPTION_RULES.md). Update context via [DOC_MAINTENANCE](../remaster/DOC_MAINTENANCE.md).

| Task | Start with | Follow up only when needed |
| --- | --- | --- |
| Codex/Antigravity pilot | [Pilot README](gemini-pilot/README.md), [pilot progress](gemini-pilot/PROGRESS.md), assigned G01-G03 brief | GEMINI.md and only named source files; do not reorder or resume P/R stages |
| Product direction / broad review | [Product index](../product-review/2026-10-08/README.md), [feature value](../product-review/2026-10-08/03-FEATURE-VALUE.md) | [Positioning](../product-review/2026-10-08/04-POSITIONING-AND-GROWTH.md), [recommendations](../product-review/2026-10-08/05-RECOMMENDATIONS.md) |
| Creative feature hypothesis | Relevant feature-value rows, relevant positioning section | [Logic audit](../product-review/2026-10-08/02-PRODUCT-LOGIC-AUDIT.md); map/code if feasibility is disputed |
| Stack / architecture | GRAPH_REPORT, [stack review](../product-review/2026-10-08/01-STACK.md), relevant FEATURE_MAP row | [Evidence](../product-review/2026-10-08/06-EVIDENCE.md); connected contracts/build files only |
| Existing visual polish | [UI playbook](../playbooks/UI_POLISH.md), [UI guidelines](../../UI_UX_GUIDELINES.md) | Mapped screen + shared tokens/components; do not load product research for a color tweak |
| Mobile Figma concepts / design handoff | Selected prompt, [Figma skills](../design/FIGMA_SKILLS.md), relevant FIGMA_MAP/action inventory section | `$the-system-figma-design`; official provider skills for the specific operation only; R04 rework contract before rejected v1 |
| UX journey / new behavior | GRAPH_REPORT, relevant FEATURE_MAP row, [new-feature playbook](../playbooks/NEW_FEATURE.md), UI guidelines | Relevant product recommendation, screen + ViewModel/use case, state/action tests |
| Native remaster action preservation | Relevant J/H section of [UX_CONTRACT](../design/UX_CONTRACT.md), one [ACTION_INVENTORY](../design/ACTION_INVENTORY.md) section | Its source aliases and assigned G01-G06 gaps; do not load every screen or treat planned state as shipped |
| Bug / regression review | GRAPH_REPORT, relevant FEATURE_MAP row, [bugfix playbook](../playbooks/BUGFIX.md) | Targeted tests, diff and relevant audit item; not all historical prompts |
| Room / draft / finish / backup | GRAPH_REPORT, relevant FEATURE_MAP row, [Room playbook](../playbooks/ROOM_CHANGE.md) | Affected entity/DAO/repository/use case/schema/migration and tests as one unit |
| Insets / hidden CTA | Relevant screen/navigation + UI guidelines | `$edge-to-edge`; preserve current routes, dimensions unless required by the authorized fix |
| Startup / jank | GRAPH_REPORT startup section + existing benchmark evidence | `$android-profiler`, real same-device traces; no new benchmark framework by default |
| Test infrastructure | Relevant build/test files and existing check scripts | `$testing-setup`; ordinary test additions reuse existing tools without an infrastructure audit |
| Release review | Relevant audit/release evidence | [Privacy](../../PRIVACY_POLICY.md), [store listing](../../STORE_LISTING.md), manifest/build only for claims being checked |
| An existing implementation task | GRAPH_REPORT, [PROGRESS](../implementation/PROGRESS.md), selected prompt only | Linked evidence and affected files; do not regenerate the full prompt pack |

## Efficient evidence handling

- Start with a small file set. Expand only for a concrete unresolved dependency,
  stale claim or failing check; say why a broader read is needed.
- Use `rg` for symbols/headings and bounded reads. Summarize, do not paste whole files into every agent prompt.
- Reuse already-loaded content in the current turn. Give subagents file paths and narrow questions, not the full conversation.
- Recheck current external standards, competitors or API behavior only when the task depends on them; cite dated primary sources. Do not browse again for a purely local typo fix.
- Keep facts, inferences, hypotheses and proposed decisions separate. Unknown runtime behavior stays unverified.
- Read the [TEAM_REVIEW](TEAM_REVIEW.md) protocol only for multi-perspective review. It is not a mandatory cost for every edit.
