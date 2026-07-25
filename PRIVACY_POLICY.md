# Privacy Policy

Last updated: 2026-07-25

**THE SYSTEM: LEVEL UP** is a personal fitness planning and workout logging app. It helps you decide what to do today, log training work, track habits, and review progress. It is not a medical device and does not provide diagnosis, treatment, or emergency advice.

## Data We Process

The app may store the following data locally on your device:

- Profile and setup data: display name, avatar URI, goal, experience level, equipment, training cycle.
- Fitness logs: workout plans, exercises, sets, reps, weights, completion status, workout reports, progression targets.
- Body and progress data: weight, height, age when entered, streaks, XP, quests, statistics, annual progression data.
- Daily planning data: to-do items, readiness inputs, calendar cycle, training/rest markers.
- Optional AI/Architect data if AI features are enabled in a non-release/debug build: chat prompts, responses, recommendations, and generated workout analysis.

## Health Connect Data

The app can optionally read a Health Connect sleep signal after you grant permission. The current requested data type is:

- Sleep sessions: used to estimate readiness and recovery context.

Health Connect access is optional. If you do not grant it, the app continues working with manual inputs and local fallback decisions.

The app reads Health Connect data on demand for fitness planning and readiness context. It does not sell Health Connect data and does not use it for ads.

## AI/Gemini Behavior

For the public release build, Gemini client AI is disabled in `app/build.gradle.kts`:

- `GEMINI_CLIENT_AI_ENABLED = false`
- `GEMINI_API_KEY = ""`

That means the release build runs core planning, workout logging, readiness, statistics, and fallback workout reports locally. AI Architect screens show an unavailable/local-mode state instead of exposing raw errors.

If AI is enabled in a debug/internal build, AI suggestions are treated as suggestions only. Domain validation remains the final gate before recommendations affect training directives. Future public AI-enabled releases must update this policy and the store disclosure before launch.

## Backup and Device Transfer

Android automatic cloud backup and device-transfer backup exclude the Room database files:

- `the_system_db`
- `the_system_db-shm`
- `the_system_db-wal`

This is intentional because the database contains sensitive fitness and body-progress data. Users can use the explicit in-app JSON export/import flow when they choose to create a backup file. Before an import writes data, the app validates the file and shows a preview with explicit confirmation. Failed imports are transactional and do not leave a partial database update.

Important: exported JSON backup files are user-controlled files. Store them only in places you trust.

## Data Sharing

The current public release does not include ads, account sync, or a custom cloud backend. The app does not sell personal data.

If future features add cloud sync, analytics, crash reporting, or public AI processing, this policy and the Play Store Data safety disclosures must be updated before release.

## Data Retention and Deletion

Data is stored locally until you delete the app, clear app data, edit/remove entries inside the app where available, or import a backup that replaces matching stored rows.

Uninstalling the app removes local app data from the device according to Android behavior. User-created exported backup files remain wherever the user saved them.

## Safety Notice

THE SYSTEM: LEVEL UP is for fitness planning and self-tracking. It does not diagnose conditions, treat injuries, prescribe medical care, or replace guidance from a qualified healthcare professional.

Stop exercise and seek professional help if you experience concerning symptoms, injury, chest pain, faintness, or other urgent health concerns.

## Policy References

- Android Health Connect: https://developer.android.com/health-and-fitness/health-connect
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Health apps policy: https://support.google.com/googleplay/android-developer/answer/14738291
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
