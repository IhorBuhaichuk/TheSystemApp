# Health Connect Rationale

## Purpose

THE SYSTEM: LEVEL UP uses Health Connect only to improve fitness planning context. Health Connect data is optional, permission-gated, and the app remains useful without it.

The app is not a medical product and does not provide diagnosis, treatment, emergency monitoring, or clinical recommendations.

## Requested Permission

The manifest declares only:

- `android.permission.health.READ_SLEEP`

The app maps the domain permission through `HealthConnectPermissions.kt`:

- `SLEEP` -> `SleepSessionRecord`

## Why This Permission Exists

Sleep-session data is used for readiness and recovery context. Fresh sleep can influence Today Order and readiness scoring. If sleep is missing, the system uses manual readiness inputs or a neutral fallback.

The app does not request steps, heart-rate, or exercise-session access because these data types are not used by the shipped core loop or user interface.

## Data Handling

`HealthConnectSignalsRepositoryImpl` reads the Health Connect signal through the Health Connect client and returns a `HealthSignals` model:

- `sleepDurationMinutes`
- `sourceFreshness`

The repository returns unavailable/null values when Health Connect is unavailable, permission is missing, reads fail, or data does not exist. The app shows fallback behavior rather than blocking the user.

## User-Facing Permission Copy

Suggested rationale text:

> THE SYSTEM can read sleep sessions from Health Connect to improve readiness and recovery context. This is optional; the app still works without Health Connect.

## Release Checklist

- Permission request screen explains the benefit before the Android permission prompt.
- Privacy Policy lists the Health Connect data type and purpose.
- Play Console Data safety matches this permission.
- Store listing avoids medical claims.
- App handles denied permission and missing Health Connect gracefully.
- Sensitive fitness data is not silently included in Android automatic backup.

## References

- Android Health Connect: https://developer.android.com/health-and-fitness/health-connect
- Google Play Health apps policy: https://support.google.com/googleplay/android-developer/answer/14738291
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
