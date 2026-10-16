# G02 - Bounded independent parser review

Recommended: Antigravity, Gemini 3.8 Flash / Medium.
Fallback: Codex, GPT-6.1 Sol / Medium. Device: not needed. Mode: plan/read-only.
Requires accepted G01 and coordinator claim G02 for Gemini in pilot PROGRESS.

Read AGENTS.md, GRAPH_REPORT.md and pilot PROGRESS, then only:
- domain/src/main/java/com/ihor/thesystem/domain/util/AnnualProgressionPlanNoteParser.kt
- domain/build.gradle.kts
- domain/src/test/java/com/ihor/thesystem/domain/model/WorkoutDraftCompatibilityTest.kt
  (JUnit conventions only; do not audit workout-draft logic).

Return at most 350 words in Ukrainian:
1. Up to three findings: file/symbol, concrete input and observable consequence.
2. Distinguish actual behavior from unknown product requirements; unusual input
   does not automatically violate an established contract.
3. Four to six characterization tests: null/blank, wrong prefix, invalid/missing
   start date, absent/invalid targets, sorted valid targets and optional metadata.
   Expected results must follow current implementation.
4. Actual model/effort, inspected files and checks not executed.

No writes, shell, network/Figma, migrations, dependencies or broad audits.
Findings do not authorize fixes. Report review_ready; Codex accepts before G03.
