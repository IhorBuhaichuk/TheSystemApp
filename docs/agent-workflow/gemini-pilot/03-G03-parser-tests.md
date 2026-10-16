# G03 - Test-only implementation pilot

Recommended: Antigravity, Gemini 3.8 Flash / Medium.
Fallback: Codex, GPT-6.1 Sol / Medium. Device: not needed. Mode: accept-edits.
Requires G01/G02 accepted and coordinator claim G03 for Gemini in pilot PROGRESS.

Read AGENTS.md, GRAPH_REPORT.md, pilot PROGRESS, accepted G02 evidence and the
parser/build/JUnit convention files named in G02. Allowed new files only:
- domain/src/test/java/com/ihor/thesystem/domain/util/AnnualProgressionPlanNoteParserTest.kt
- docs/agent-workflow/gemini-pilot/G03-HANDOFF.md

Add four to six deterministic JUnit4 characterization tests from accepted G02
cases. Preserve actual behavior; no production fixes or desired-but-unimplemented
validation assertions. Report defects rather than weakening assertions.
No edits to existing files, dependencies, queue/progress, scripts or Figma.

Request permission for the narrowly scoped test command; never bypass approvals:
`./gradlew.bat :domain:test --tests com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParserTest`
Use existing Android Studio JBR if JAVA_HOME is absent, without persistent system
configuration. Denied commands/missing dependencies mean unverified, not passing.

Handoff, at most 250 words: base revision if available, exact changed files/test
symbols, command/results/report paths, actual model/effort (or unverified), risks.
Report review_ready; Codex alone accepts. No commits/pushes/install/data reset,
secret access, bulk shell edits or work outside the two-file allowlist. Then stop.
