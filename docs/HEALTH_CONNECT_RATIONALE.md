# Health Connect Rationale

## Purpose

THE SYSTEM: LEVEL UP uses Health Connect only to improve fitness planning context. Health Connect data is optional, permission-gated, and the app remains useful without it.

The app is not a medical product and does not provide diagnosis, treatment, emergency monitoring, or clinical recommendations.

## Requested Permissions

The manifest currently declares:

- `android.permission.health.READ_SLEEP`
- `android.permission.health.READ_STEPS`
- `android.permission.health.READ_HEART_RATE`
- `android.permission.health.READ_EXERCISE`

The app maps domain permissions through `HealthConnectPermissions.kt`:

- `SLEEP` -> `SleepSessionRecord`
- `STEPS` -> `StepsRecord`
- `HEART_RATE` -> `HeartRateRecord`
- `EXERCISE_SESSIONS` -> `ExerciseSessionRecord`

## Why Each Permission Exists

### Sleep

Used for readiness and recovery context. Fresh sleep can influence Today Order and readiness scoring. If sleep is missing, the system uses manual readiness inputs or neutral fallback.

### Steps

Used as general movement/load context. Steps can help the system understand whether the day already contains meaningful physical activity.

### Heart Rate

Used as workout/recovery context when available. The current implementation estimates a low/resting heart-rate value from available samples for context only.

### Exercise Sessions

Used to understand whether workouts were already recorded in Health Connect and to support recent activity context.

## Data Handling

`HealthConnectSignalsRepositoryImpl` reads signals through the Health Connect client and returns a `HealthSignals` model:

- `sleepDurationMinutes`
- `stepsToday`
- `restingHeartRate`
- `workoutSessions`
- `sourceFreshness`

The repository returns unavailable/null values when Health Connect is unavailable, permissions are missing, reads fail, or data does not exist. The app should show fallback behavior rather than blocking the user.

## User-Facing Permission Copy

Suggested rationale text:

> THE SYSTEM can read selected Health Connect signals to improve readiness and training context. Sleep helps recovery decisions, steps and exercise sessions help understand daily load, and heart-rate data can provide extra workout context. This is optional; the app still works without Health Connect.

## Release Checklist

- Permission request screen explains the benefit before the Android permission prompt.
- Privacy Policy lists each Health Connect data type and purpose.
- Play Console Data safety matches these permissions.
- Store listing avoids medical claims.
- App handles denied permissions and missing Health Connect gracefully.
- Sensitive fitness data is not silently included in Android automatic backup.

## References

- Android Health Connect: https://developer.android.com/health-and-fitness/health-connect
- Google Play Health apps policy: https://support.google.com/googleplay/android-developer/answer/14738291
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311

