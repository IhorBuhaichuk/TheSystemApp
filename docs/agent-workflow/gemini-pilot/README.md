# Codex + Antigravity pilot

Authorized 2026-10-16. Test handoff quality and subscription continuity, not a
claimed model advantage or guaranteed savings. Phone not required for G01-G03.

## Start

From the repository root in a new PowerShell terminal:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-gemini-pilot.ps1
```

Complete Google login yourself; select personal-account subscription auth, not
API key, ADC or enterprise billing. No API key is configured by this setup.
Do not buy credits or authorize overage. Entitlement/quota are account-specific;
no API key does not by itself guarantee zero extra cost. Never paste tokens.

CLI commands: `/model`, `/effort low`, `/effort medium`, `/usage`, `/permissions`,
`/mcp`. `agy models` lists actual model IDs. Existing terminals may need restarting
after installation updates PATH; the launcher resolves the user-local binary.
Figma OAuth is separate. A configured endpoint is not an authenticated connection.

## Sequence

[PROGRESS](PROGRESS.md) owns pilot claims and evidence. Run one trial at a time:

1. [G01](01-G01-context-handoff.md): bounded context handoff, Flash / Low.
2. [G02](02-G02-parser-review.md): independent parser review, Flash / Medium.
3. [G03](03-G03-parser-tests.md): JUnit characterization tests, Flash / Medium,
   after Codex accepts G01 and G02. No production behavior changes.

These are tooling exercises, not new P/R stages. [QUEUE](../../remaster/QUEUE.md)
remains R05. No Figma edit or Android feature is approved/completed by this pilot.

After coordinator claim and login, choose an actual available model ID:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-gemini-pilot.ps1 -Task G01 -Model gemini-3.8-flash-medium
```

The documented example ID is not proof of account access. The launcher rejects
unlisted IDs. G01/G02 capture JSON in ignored local reports/. G03 is interactive
so shell approval is explicit rather than silently denied in headless mode.

## Ownership

One writer per checkout. Codex claims tasks and maintains shared QUEUE/PROGRESS;
Antigravity edits only explicit task allowlists and submits `review_ready`.
No self-acceptance, commits, reset/stash, branch switch, pushes or installs.
Do not touch unrelated dirty files. Future concurrent edits require separate
worktrees and an agreed base, not a reset of this dirty checkout. Figma and device
operations require exclusive ownership even across worktrees. Claims are advisory.

G01/G02 use plan mode; it is not an OS-enforced security boundary. Do not grant
broad shell/MCP access or use --dangerously-skip-permissions. G03 permission
requests must match its test-only allowlist. No automatic teams or remote control.

Recommended models are flexible: fallback G01 = GPT-6 Luna / Low; G02/G03 =
GPT-6.1 Sol / Medium. Record actual client model/effort, or unverified. Markdown
cannot change the runtime. Review is Medium normally, Low for mechanical changes,
deeper for data/security risk. Passing a command is evidence, not acceptance.

## Adoption criteria

Accurate handoffs, no unauthorized edits, passing meaningful tests and affordable
review/correction cost. Record actual model/effort, duration/usage if available,
correction count and remaining gates. Three trials are not a model benchmark.
Keep the existing 55 prompt model/status schema until the pilot is evaluated.

## Setup provenance

Installed CLI 1.4.0. Official installer inspected before execution; binary SHA-512
verification ran. Installer SHA-256:
`51C2CB4FADA22CE0228DA71B9506370383D6544BFEBCEC85FE7616A52B805344`.
Account login, model entitlement and Figma tools still need live verification.

- [Google installation/auth](https://www.antigravity.google/docs/install?tab=cli)
- [CLI commands](https://www.antigravity.google/docs/cli/reference/)
- [Rules](https://www.antigravity.google/docs/rules/)
- [MCP schema](https://www.antigravity.google/docs/cli/gcli-migration)
- [Headless permissions](https://www.antigravity.google/docs/cli/headless/)
