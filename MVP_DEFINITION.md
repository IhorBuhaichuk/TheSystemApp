# MVP_DEFINITION.md

MVP definition for **THE SYSTEM: LEVEL UP**.

This file defines what must exist before beta and public release. It intentionally limits scope so the project can reach a polished first version instead of expanding endlessly.

## MVP Goal

The MVP must prove this loop:

> The user opens THE SYSTEM, sees a clear Today Order, completes or respects the mission, logs the result, and sees progress/reward feedback.

The MVP is successful if the app is useful without AI and becomes stronger with AI.

## Must-Have MVP

### 1. First-Run Onboarding

Required:

- User can enter name/profile basics.
- User can select goal or training intent.
- User can configure equipment.
- User can create or accept a starter cycle.
- User lands on Status with a usable first Today Order.

Acceptance:

- New user does not need manual database/setup help.
- Completed onboarding does not repeat.
- User can edit configuration later.

### 2. Today Order

Required:

- Shows one clear day type: training, recovery, deload, no-excuse, or rest.
- Explains why the system chose it.
- Shows one primary CTA.
- Shows reward/consequence: XP, streak, quest progress, recovery, or next-step value.
- Has graceful fallback when readiness/health/history data is missing.

Acceptance:

- User can understand today's action in under 10 seconds.
- No blank/cryptic state for missing AI or missing Health Connect.

### 3. Workout Logging

Required:

- User can start planned workout.
- User can log sets/reps/weight quickly.
- User can complete workout.
- System saves local session first.
- Finish report shows completed work and useful verdict.

Acceptance:

- First workout can be completed end-to-end.
- Failed AI/network does not lose workout data.
- Fallback report still feels intentional.

### 4. Progression System

Required:

- XP/rank/progression matrix updates are visible.
- Main quests or daily tasks connect to training progress.
- Statistics show enough proof to validate effort.

Acceptance:

- User can see that completing a mission changed something.

### 5. Calendar and Cycle

Required:

- Calendar shows training/rest/task context.
- System/Cycle tab lets user inspect and edit active cycle.
- Cycle state stays consistent with Today Order.

Acceptance:

- User can understand what is planned this week.
- Changing cycle day does not break Status.

### 6. No-AI Mode

Required:

- Release build with disabled Gemini remains useful.
- AI unavailable state is clearly explained.
- Fallback workout report and recommendations work.

Acceptance:

- User can complete the core loop without an API key.

### 7. Backup and Data Trust

Required:

- Explicit JSON export/import exists.
- Silent cloud backup excludes Room DB.
- User-facing copy explains backup state.

Acceptance:

- User can export data before risky changes or beta builds.
- Import failure gives useful message.

### 8. Health Connect Handling

Required:

- App handles unavailable, denied, and granted states.
- Readiness can fall back to manual/neutral data.
- Permissions are explained in docs and UI.

Acceptance:

- Denying Health Connect does not block the app.
- Health data use is transparent.

### 9. Release Safety

Required:

- Kotlin compile passes.
- Unit tests pass.
- Guard tests for architecture/release/source quality pass.
- Compose-only guard passes.
- Privacy and store docs exist before public release.

Acceptance:

- `scripts/check-tests.cmd` passes before beta candidate.

## Should-Have MVP

Useful, but can slip if must-have work is not done:

- Better visual polish across Profile and Statistics.
- More detailed onboarding personalization.
- Local beta metrics dashboard.
- Better empty states for all secondary screens.
- First-week checklist.
- Screenshot-ready demo data.
- Clear AI Architect dashboard with concise insights.

## Not-Now

Do not build before MVP proof:

- Social/community features.
- Leaderboards.
- Web app.
- Cloud account/sync system.
- Public coaching marketplace.
- Full nutrition macro tracker.
- Complex wearable analytics.
- Paid subscriptions.
- Advanced periodization editor for expert athletes.
- React/HTML/CSS/Tailwind or any web UI implementation.

## Beta Readiness Criteria

Beta can start when:

- Onboarding works on a clean install.
- Status screen gives a useful Today Order.
- A user can log the first workout end-to-end.
- No-AI mode works.
- Health Connect denied/unavailable does not break the app.
- Backup export/import UI is present and understandable.
- `scripts/check-tests.cmd` passes.
- `scripts/check-web-ui-guard.cmd` passes.
- Major screens do not have obvious broken layout or missing CTA.

Recommended beta size:

- 5-10 users.
- 7-14 days.

Beta questions:

- Did you know what to do today?
- Was logging a workout annoying?
- Did the system explanation feel believable?
- Did the RPG layer motivate or distract?
- Did you come back the next day?

## Public Release Criteria

Public release can start when beta proves:

- Users complete onboarding without help.
- Users complete at least one planned mission.
- Users understand Today Order.
- No critical crash appears in the core loop.
- Store/privacy/Health Connect docs are complete.
- Release build is verified.
- Launcher icon and screenshots are production-ready.
- Public claims avoid medical or transformation guarantees.

Required public artifacts:

- `README.md`
- `PRIVACY_POLICY.md`
- `STORE_LISTING.md`
- `docs/HEALTH_CONNECT_RATIONALE.md`
- `docs/SCREENSHOTS_CHECKLIST.md`
- Production screenshots
- Release APK/AAB verification notes

## MVP Quality Bar

The MVP should feel:

- tactical;
- fast;
- clear;
- native;
- premium;
- strict but fair;
- useful without AI;
- enhanced by AI.

The MVP should not feel:

- like a generic habit tracker;
- like a skin over a weak workout logger;
- like a chatbot with a database;
- like a dashboard full of unexplained stats;
- like a web app port;
- like a prototype that needs developer help.

## Cut Line

If time is tight, cut in this order:

1. Fancy secondary animations.
2. Deep Statistics details.
3. Advanced AI insight variants.
4. Extra Profile customization.
5. Additional quest types.

Do not cut:

1. Today Order clarity.
2. Workout logging reliability.
3. No-AI fallback.
4. Backup trust.
5. Release/privacy safety.
