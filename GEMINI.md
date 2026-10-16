# Antigravity project entry

Read [AGENTS.md](AGENTS.md), then [GRAPH_REPORT.md](GRAPH_REPORT.md), then only the
assigned task's entry points. This file supplements, never replaces, those rules.

THE SYSTEM is a local-first Android application: Kotlin, Jetpack Compose, Hilt,
Room and Coroutines/Flow. Pure business contracts/rules belong to `domain/`;
Android UI/ViewModels and data implementations belong to `app/`. UI is native
Compose only. AI suggests; domain validation decides. Never inspect credentials,
developer backups, fitness databases, signing files or `local.properties`.

## Paired-agent pilot

The owner authorizes Codex to coordinate architecture and review. Antigravity is
a co-developer, not an unattended replacement for user approval. Both agents
must pass the same tests and device/owner gates; neither can approve itself.

Start at [pilot README](docs/agent-workflow/gemini-pilot/README.md). During this
pilot only explicitly assigned G01-G03 tasks are authorized. Do not resume R05,
take a P/R prompt, edit Figma or expand scope automatically. The main remaster
queue remains authoritative and unchanged by pilot completion.

One writer at a time in this checkout. The coordinator owns claims, shared
QUEUE/PROGRESS files and acceptance. Read the pilot progress table before work;
stop on a conflicting claim. These are advisory locks, not filesystem security.
Never commit, stash, reset, switch branches, push, merge or run an installer.
Do not edit existing dirty files outside the explicit task allowlist.

Model/effort are recommendations, not permission to claim a model was used.
Use the CLI's actual selected model; report unknown values as unverified. No
silent fallback, API-key authentication, credit purchase or paid API setup.
Do not request High for mechanical work. Ask the owner to change model/effort
when the client cannot do so; Markdown cannot change runtime configuration.

## Handoff

Return a compact report: task/base revision; inspected or changed paths/symbols;
findings and preserved behavior; exact checks/results; remaining risks/gates.
Separate observations from proposals. Never describe an unrun test as passing.
Submit as `review_ready`, not accepted/done. Codex reviews the actual diff and
evidence; the owner retains final product/design authority. Load relevant skills
from `.agents/skills/` on demand, never the entire skill directory.
