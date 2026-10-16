# Bounded team review

Use when the user requests the product/design team review. Setup alone does not
start it. No feature edits, architecture migration or implementation prompts
until the relevant next stage is approved.

On 2026-10-08 the owner authorized the phased remaster prompt package. Its entry is
REMASTER_PLAN.md; specific scope/design/billing decisions remain gates in R01/R02,
R04-R06 and R19. Package authorization is not a completed independent team review.

## Dispatch

The coordinator chooses only useful independent passes, normally up to three
subagents while owning architecture/synthesis itself. Use the host's available
subagent tools, not separate user chats or simulated dialogue. If unavailable,
disclose a sequential single-agent review instead of claiming independence.
No nested delegation or parallel edits. Select models supported by this host;
use a lighter model for bounded evidence extraction and a capable model for
ambiguous trade-offs. Do not bake model names or claimed prices into project rules.

Before dispatch, follow the [context router](CONTEXT_ROUTER.md), identify the
question and current revision/local changes, and send each role only its inputs.
Agree a small output budget per role, typically five findings and about 500 words.
These are planning defaults, not limits on reporting a serious safety risk.

## Role briefs

| Role | Inputs | Question and boundary |
| --- | --- | --- |
| Product / creative | `the-system-product-lab`; product index; selected rows of feature-value and positioning docs | What user problem deserves focus? Offer a small number of testable alternatives, including simplification. Do not promise market success or add scope. |
| Native UX/UI | `the-system-native-ux`; UI guidelines; relevant journey/screens and product recommendation | Can a new user understand and complete the task? Review language, hierarchy, accessibility and friction. No web prototypes or domain changes. |
| Android/data architecture (coordinator by default) | GRAPH_REPORT; affected FEATURE_MAP row; stack/evidence sections; connected contracts when necessary | Is the proposal feasible within current Kotlin/Compose/Room/Hilt ownership? Identify data and dependency risks. Do not pre-approve a rewrite. |
| Quality / skeptical review | `the-system-quality-review`; relevant diff, implementation evidence and test entry points | Which claims lack evidence, which regressions could occur, and what verification is required? No code edits during review. |

Each pass is read-only and independent before synthesis. Ask for this shape:

```text
Question and inspected files/revision:
Findings: severity, evidence path/symbol, consequence, confidence.
Proposed change: smallest option, trade-off, what NOT to add.
Validation: cheap experiment or exact relevant test/device check.
Uncertainty / disagreement / missing input:
```

## Synthesis and decisions

1. Merge duplicate findings; preserve genuine disagreements and distinguish code facts from product hypotheses.
2. Offer a prioritized recommendation with alternatives, implementation cost and a small validation plan. Do not score unknown retention/performance as measured.
3. Allow one targeted clarification/rebuttal round only when it changes a decision. Avoid endless agent debate.
4. Discuss with the user. Record accepted/rejected/deferred decisions in [SESSION_HANDOFF](SESSION_HANDOFF.md), with date and scope.
5. After approval, propose product/information architecture. Create implementation prompts only after that structure is approved.

Each future implementation prompt should name the applicable skill, MD sections,
exact files/contracts, preserved behavior, checks, device requirement and stop
condition. Point to existing evidence rather than copying the whole audit.

Keep local source/fitness data out of external design/research services. A
synthetic persona is a hypothesis aid, not a real user interview. Device testing
and user validation remain separate gates from this document review.
