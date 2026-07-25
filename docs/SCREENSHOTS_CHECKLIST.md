# Screenshots Checklist

Use this checklist before public release screenshots are uploaded.

## Global Rules

- Show real app screens, not web mockups.
- Use native Android screenshots from a clean release/debug build.
- Do not show API keys, debug overlays, stack traces, raw exception text, or private user data.
- Use safe demo data: neutral name, realistic workouts, no sensitive health condition text.
- Avoid medical claims in captions.
- Keep screenshots visually consistent with the dark sci-fi/HUD theme.

## Required Screens

1. Status / Today Order
   - Shows the daily decision clearly.
   - Includes day type, readiness/reason, main action, and expected outcome.

2. Workout Logging
   - Shows fast set logging.
   - Completed and incomplete states are understandable.
   - Finish workout CTA is visible.

3. Workout Report
   - Shows completed work, progress/verdict, and next-session action.
   - If AI is unavailable, message reads as local fallback, not as a crash/error.

4. Calendar
   - Shows selected day, markers, and event list.
   - No empty/garbled text or over-promised streak claims.

5. System / Cycle
   - Shows cycle overview, active day, edit cycle action, and training days.

6. Statistics
   - Shows weekly report and progression proof with realistic data.
   - Avoid implying medical diagnosis or guaranteed results.

7. Profile
   - Shows profile, rank/level, personal metrics, and settings access.
   - Demo avatar/name only.

8. Health Connect Permission Context
   - If included, show an explanatory pre-permission state.
   - Copy must explain optional use and graceful fallback.

## Visual QA

- Large panels use the shared rounded panel shape.
- Buttons, cards, dialogs, empty states, and nav match the same material system.
- Text fits within containers on the screenshot device.
- Cyan/violet glow is controlled and does not obscure labels.
- No layout shifts, clipped CTA text, or hidden nav item labels.

## Device Set

Minimum recommended capture set:

- Phone portrait: 1080x2400 or similar.
- Smaller phone portrait: 720x1280 or similar.
- Optional tablet/large screen only if the app layout is validated there.

## Final Pre-Upload Check

- Privacy Policy URL is ready.
- Store listing has no medical/treatment claims.
- Health Connect rationale matches declared permissions.
- Data safety answers match actual release behavior.
- Release build has Gemini disabled unless a separate AI disclosure/review package is prepared.

