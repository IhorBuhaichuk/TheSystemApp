# Покриття ремастеру

Це карта роботи, не заява про виправлення. Старі AUD-01..12/RISK-01..08 повністю залишаються
у [P coverage](../implementation/COVERAGE.md). Нові задачі не підміняють їх cosmetic changes.

| Вимога / новий ризик | Власник роботи | Доказ завершення |
| --- | --- | --- |
| Корисність усіх 53 функцій, широка аудиторія | R01 | FEATURE_SCOPE, approved keep/defer/hide; no silent deletion |
| Пов'язані сценарії й жодної втраченої дії | R02, R08-R14, P18 | ACTION_INVENTORY -> UI events -> real journey tests |
| Editable макет і точний handoff | R03-R07 | Реальні Figma nodes/variables, owner approval, Kotlin token map |
| Secondary screens/dialogs | R06, R10-R13 | ACTION_INVENTORY -> FIGMA_MAP: new-approved node або preserve-current baseline; жодних загублених дій |
| Зрозумілість до Compose rollout | R05 prototype, R06 real-person check, R07 gate | PROTOTYPE_FEEDBACK: 3-5 людей без аніме-контексту, факти, fixes/recheck, owner approval |
| Status/перший план | P07-P10, R05, R08 | Real initial schedule + honest state/CTA |
| Workout UX і no duplicate finish | P01-P03, R09 | Room/lifecycle regression + logging/IME interaction |
| Calendar/план/історія | P10-P14, R10-R12 | Cross-tab data truth + visible/screen reader accessible states |
| Profile/backup/permissions/local AI | P04-P05/P15-P16, R13 | Round-trip, refusal paths, useful fallback |
| Ненав'язлива гейміфікація | R14, P10-P11 | Approved presentation/policy, REST і return без примусу |
| Доступність і плавність | P18-P20, R15 | UI matrix, TalkBack, reduced motion, same-device traces |
| Мінімальний Free access contract | R16 | Free/default/provider tests; core не заблоковано, без paid lifecycle до R19 |
| Recurring value / price | P27, R17-R18 | Реальні observations + одна робоча додаткова capability |
| Billing/identity/security | R19-R21 | Approved ADR, verified service/provider, Android adapter tests |
| Paywall/lifecycle/refund/offline | R22-R23 | Store details, sandbox/device matrix, ownership і downgrade proof |
| Paid readiness | R24, P28 | Candidate artifact + policy/owner approval, no secrets |
| Підтримка й реальні люди | P25-P27, R25 | Support runbook, consented feedback, named owner |
| Етичне зростання | R26 optional | Opt-in native share; no sensitive defaults; experiment decision |
| Економія контексту й підтримка карти | DOC_MAINTENANCE, кожен prompt | Relevant MD + evidence + checkbox synchronized |

P26/P28 мають перевіряти actual current artifact, не лише наявність усіх MD.
Формальне закриття старих пунктів через відкладення UI неприпустиме, якщо underlying data path лишився shipped.
Нові beta defects додаються як окремі repair prompts із stable IDs. Гарантувати відсутність усіх майбутніх bugs неможливо.
