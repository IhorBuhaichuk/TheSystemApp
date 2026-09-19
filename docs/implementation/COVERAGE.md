# Audit Coverage

Це відповідність знахідок задачам, **не** підтвердження виправлення. Статуси в PROGRESS, докази в evidence/Pxx.md після виконання.
Джерело: [PROJECT_AUDIT](../../PROJECT_AUDIT.md), [DEVELOPMENT_ROADMAP](../../DEVELOPMENT_ROADMAP.md).
Назви RISK-01..08 нижче введені тільки для відстеження восьми рядків «Ризики, які ще треба довести».

## Підтверджені знахідки

| Finding | Основні prompts | Що доводить закриття |
| --- | --- | --- |
| AUD-01 edit deletes other sets | P01, P18 | Real Room multi-exercise/two-session regression; інші вправи збережені |
| AUD-02 lost draft on resume | P02, P18 | Durable persistence + background/process-death recovery без XP |
| AUD-03 repeated finish | P03, P18 | Stable operation ID, storage uniqueness, crash/retry одна session/reward |
| AUD-04 backup/restore | P04, P05, P23, P24 | Snapshot/rollback/schema validation, SAF timestamp, coherent prefs/DB route |
| AUD-05 no starter schedule | P05, P07, P18 | Clean install -> setup -> реальний перший план; seed/retry safe |
| AUD-06 readiness truth/input | P08, P09, P15 | Provenance/freshness + optional check-in + reactive update; no false precision |
| AUD-07 REST/quest mismatch | P10, P11 | OFF/REST без phantom MAIN; completed quest не регенерує reward |
| AUD-08 lint | P06, P22, P26 | Actual Android lint errors=0; locale change test |
| AUD-09 HC rationale | P15, P23, P24 | System intent/alias/native policy; deny/revoke/provider cases |
| AUD-10 incomplete progress | P12, P18 | Real SQL completed40kg/incomplete100kg ->40kg proof |
| AUD-11 insufficient real UI/integration CI | P18, P22, P26 | Production NavGraph journeys + actual Room tests; CI evidence not overwritten |
| AUD-12 misleading beta metrics | P17, P25, P27 | Historical snapshots/idempotent events, defined denominators, actual feedback |

## Ризики для перевірки

| ID / audit row | Prompts | Evidence |
| --- | --- | --- |
| RISK-01 Worker/foreground double rollover | P11 | Concurrent fake-clock + persisted transaction test |
| RISK-02 Seed/onboarding race | P05, P07 | Slow/failing seed + setup retry/interruption без overwrite |
| RISK-03 ExercisePicker cancellation | P13 | Delayed write + pop/back у реальному owner lifetime |
| RISK-04 HC sleep double count/window | P15 | Overlap/overnight/DST/pagination/provider fixtures |
| RISK-05 Stale Today після HC/check-in | P08, P09, P15 | Signal change оновлює decision без restart, без refresh loop |
| RISK-06 Annual plan save partial | P14 | Failure на другій вправі -> повний rollback |
| RISK-07 Ліміти history | P12 | Dense/long-history fixtures, bounded queries із чесними contracts |
| RISK-08 Avatar/asset portability/licensing | P04, P23 | Restore/missing URI test; offline assets; source/license inventory/owner proof |

Не підтвердився ризик -> not_reproduced_verified із тестом. Це краще за непотрібний refactor,
але не можна закрити ризик тільки фразою «начебто працює».

## Наскрізні прогалини

| Область | Prompts | Межа scope |
| --- | --- | --- |
| Manual nutrition не підключена | P09, P23 | Мінімальний input якщо shipped consumer; інакше явно deferred/not-shipped, без false claims; не calorie tracker |
| AI adversarial/context safety, cancellation | P03, P16 | Gatekeeper fail-safe; no raw exception; release no-AI без paid API tests |
| Dead-end AI/future modules | P16, P19 | Чесні states, доступні local actions, не додавати backend |
| Touch/contrast/TalkBack/font2.0/motion | P18, P19 | Real screens та screenshots; не лише assertIsDisplayed |
| Localization/copy/decimal/date | P06, P19 | Actual resources/observable locale; default Ukrainian не втратити |
| Slow frames/startup/allocation/wakeups | P20, P24 | Current same-device measurements, separate first-install/cold/warm; battery claims тільки з evidence |
| Oversized ViewModel/domain-test placement | P21 | Bounded refactor за tests, не створення десятків modules |
| Parallel session tables/annual note format | P14, P21 | Ownership/contracts, targeted compatibility tests; без сліпого destructive removal |
| Deprecated tooling/profile compatibility | P22 | Verified supported versions, no warning suppression/mass upgrade |
| Privacy, deletion, minimal data/permissions | P04, P05, P15, P17, P23 | Actual data inventory, safe backup, optional diagnostics, no credential logging |
| Android16/API36/16KB/R8/signed artifact | P24, P26, P28 | Candidate-specific compatibility і device evidence; phone alone недостатній |
| Training-content/safety claims | P07, P08, P23, P25, P28 | Human expert review поза LLM; жодних medical guarantees |
| Store/policy/contact/assets/Console | P23, P24, P25, P28 | Owner evidence і current official requirements, не вигадані approvals |
| Retention/time-to-value/feedback | P17, P25, P27 | Реальні testers, defined metrics; no fabricated cohort numbers |
| Positioning/scope/monetization | P25, P27 | Перевірка гіпотез; не автоматичний Billing/social/cloud scope |
| Final regression/readiness | P26, P28 | Independent GO/NO-GO за current evidence; unknown critical gate лишається open |

## Реальні межі «під ключ»

Пакет покриває відомий аудит, але не може наперед перелічити bugs майбутньої beta.
P26/P27/P28 зобов'язують створювати вузькі repair prompts для нових доведених проблем.
Account доступ, signing secrets, public policy contact, права на assets, експертний review і tester feedback
потребують власника/людей. Відсутність їхніх доказів має явний статус, а не фальшиві10/10.
