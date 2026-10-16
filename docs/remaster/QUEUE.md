# Черга: один наступний крок

Актуальна оркестрація P01-P28 + R01-R27: **55 промптів** в [одній папці](prompts/).
Завершені кроки не запускати повторно. Статуси: [P progress](../implementation/PROGRESS.md),
[R progress](PROGRESS.md). NN означає порядок, P/R ID є постійним ключем.

## Де ми зараз

2026-10-15 / D30: власник повністю схвалив поточні макети R05 v2 і дозволив
аудит data flow, локальні виправлення та оновлення промптів. Попередня заборона
змінювати код/промпти до перегляду більше не діє.

Active task: R05
Next task without device: R05
Next device gate: P03

R05 run2026-10-16:12 normal frames now share styles and13 action instances;8 secondary
edges saved. Actual D -> L1 -> F -> R -> RD -> R path verified. MCP quota then exhausted;
browser Plugin API editor failed. [Checkpoint and exact resume](evidence/2026-10-16-r05-prototype-checkpoint.md).
Still in_progress:22 frame bindings, remaining state/360 paths and fixture consistency.

2026-10-16: аудит data flow та правки черги завершені; compile/unit/web/plan checks
пройшли. Device tests не виконувались. Це закриває audit checkpoint, не весь R05.

Поточний: [07-R05](prompts/07-R05-figma-core-prototype.md), in_progress.
[Контракт взаємодій](../design/R05_DATA_FLOW.md) описує всі17 станів, джерела,
розриви та implementation owners. [Перевірки](evidence/2026-10-15-r05-data-flow.md).
Ще потрібно закрити secondary/360 prototype links, shared bindings і узгодити
fixture між новим планом та Status без зміни затвердженої композиції.
[Worklist](../design/R05_CORE_PROTOTYPE.md), [v2 nodes](../design/R05_V2_REVIEW.md).
Нові поля onboarding/одноразові зміни/partial confirmation ще не реалізовані в Android.
Після закриття R05 наступний **08-R06**, не повторний R04.

R01-R04 done; [Status baseline D29](../design/R04_SELECTED_DESIGN.md) незмінний.
Зберегти D25 «Тренування», D26 іконки/W02, D27 прогрес у Statistics, D28 без weekly-counter.
P01/P02 done; [09-P03](prompts/09-P03-idempotent-workout-finish.md) очікує реальні
Room/device checks, не повторну реалізацію finish.

## Без ручних перестрибувань

1. Власник пише **«Наступний промпт»**. Агент читає checkpoint і лише поточний файл.
2. Рухаємось за NN згори вниз. Завершене пропускаємо за статусом/evidence; власник
   не має шукати інший P/R номер.
3. Device gate записуємо як verification backlog. Незалежну host-частину дозволено
   продовжувати за EXECUTION_RULES, не закриваючи залежні gates. Повертаємось лише
   до відкладеної перевірки, не реалізуємо попередню задачу заново.
4. Pro/sharing розглядаються на своїй позиції. Лише явний defer дозволяє пропуск;
   це не done і не дозвіл продавати. Free release не зобов'язує запускати платну гілку.
5. При старті/паузі/завершенні оновлюємо pointers, checklist, PROGRESS та evidence.
   Нові поля включають migration/backup/readers/tests у своєму кроці, без повернення
   власника до старих промптів.
6. R06 включає ранню UX-перевірку з3-5 людьми; visual approval її не замінює.

## Єдина послідовність

Додано **16-R27** між P09 і P10: domain/persistence для «Змінити лише сьогодні».
Це відсутня залежність прийнятого R05. Наступні NN зміщені на1; стабільні IDs,
історичні докази та виконані checkbox збережено. Заголовки dependencies точно
відповідають таблиці; prerequisites йдуть раніше за consumers.
Перевірка: `scripts/check-remaster-plan.cmd`. Один prompt за запуск, без паралельних
правок shared файлів в інших чатах.

| ID | Промпт | Модель / reasoning | Prerequisites |
| --- | --- | --- | --- |
| P01 | [01. P01 - Безпечне редагування історії](prompts/01-P01-safe-history-edits.md) | GPT-6.1 Sol / High | - |
| P02 | [02. P02 - Збереження незавершеного тренування](prompts/02-P02-durable-workout-draft.md) | GPT-6.1 Sol / High | P01 |
| R01 | [03. R01 - Зафіксувати межі продукту](prompts/03-R01-product-scope.md) | GPT-6.1 Sol / High | - |
| R02 | [04. R02 - Сценарії та карта дій](prompts/04-R02-journeys-and-actions.md) | GPT-6.1 Sol / High | R01 |
| R03 | [05. R03 - Figma: файл, tokens і базові компоненти](prompts/05-R03-figma-foundations.md) | GPT-6.1 Sol / Medium | R02 |
| R04 | [06. R04 - Три напрями й вибір дизайну](prompts/06-R04-three-design-directions.md) | GPT-6.1 Sol / High | R03 |
| R05 | [07. R05 - Прототип першої користі та тренування](prompts/07-R05-figma-core-prototype.md) | GPT-6.1 Sol / High | R04 |
| R06 | [08. R06 - Решта вкладок і системні стани у Figma](prompts/08-R06-figma-five-tabs.md) | GPT-6.1 Sol / High | R05 |
| P03 | [09. P03 - Одноразове завершення й надійний звіт](prompts/09-P03-idempotent-workout-finish.md) | GPT-6.1 Sol / High | P02 |
| P04 | [10. P04 - Безпечний backup і відновлення](prompts/10-P04-atomic-backup-restore.md) | GPT-6.1 Sol / High | P01, P02, P03 |
| P05 | [11. P05 - Узгоджений запуск, seed і відновлений профіль](prompts/11-P05-startup-restore-state.md) | GPT-6.1 Sol / High | P04 |
| P06 | [12. P06 - Lint і правильна локалізація форматування](prompts/12-P06-lint-and-locale.md) | GPT-6 Luna / Medium | - |
| P07 | [13. P07 - Готовий перший план після onboarding](prompts/13-P07-usable-onboarding.md) | GPT-6.1 Sol / High | P05, R02, R05 |
| P08 | [14. P08 - Чесні пояснення Today Order](prompts/14-P08-honest-today-order.md) | GPT-6.1 Sol / High | P07, R02 |
| P09 | [15. P09 - Короткий check-in і актуальність сигналів](prompts/15-P09-checkin-and-reactivity.md) | GPT-6.1 Sol / High | P08, R02 |
| R27 | [16. R27 - Зміни тренування лише на сьогодні](prompts/16-R27-session-adjustments.md) | GPT-6.1 Sol / High | P02, P03, P07, P08, P09, R05 |
| P10 | [17. P10 - Узгоджені REST, квести та нагороди](prompts/17-P10-rest-quests-and-rewards.md) | GPT-6.1 Sol / High | P03, P08, P09, R01, R27 |
| P11 | [18. P11 - Опівніч, часові пояси та паралельні оновлення](prompts/18-P11-daily-rollover-concurrency.md) | GPT-6.1 Sol / High | P05, P10 |
| P12 | [19. P12 - Правдивий прогрес і повнота історії](prompts/19-P12-truthful-progress-history.md) | GPT-6.1 Sol / High | P01, P03, P10 |
| P13 | [20. P13 - Збереження вибору вправи при поверненні](prompts/20-P13-picker-save-lifecycle.md) | GPT-6 Luna / Medium | P07 |
| P14 | [21. P14 - Атомарне збереження річних планів](prompts/21-P14-atomic-annual-plans.md) | GPT-6.1 Sol / High | P12 |
| P15 | [22. P15 - Завершена інтеграція Health Connect](prompts/22-P15-health-connect-complete.md) | GPT-6.1 Sol / High | P08, P09 |
| P16 | [23. P16 - Перевірений AI gatekeeper і корисний локальний режим](prompts/23-P16-ai-validation-local-mode.md) | GPT-6.1 Sol / High | P03, P08, P10, P12, R01 |
| P17 | [24. P17 - Коректні локальні beta metrics](prompts/24-P17-beta-metrics-integrity.md) | GPT-6.1 Sol / High | P03, P07, P10, P11, P12 |
| R07 | [25. R07 - Нативні primitives за затвердженим дизайном](prompts/25-R07-native-design-primitives.md) | GPT-6.1 Sol / High | R06 |
| R08 | [26. R08 - Onboarding і Status: ясна перша дія](prompts/26-R08-status-and-first-plan-ui.md) | GPT-6.1 Sol / High | R07, P07, P08, P09, P10, R27 |
| R09 | [27. R09 - Зручний logging без втрати чернетки](prompts/27-R09-workout-interaction-ui.md) | GPT-6.1 Sol / High | R08, P01, P02, P03, R27 |
| R10 | [28. R10 - Calendar: план і факти без плутанини](prompts/28-R10-calendar-ui.md) | GPT-6 Luna / Medium | R07, P10, P11, P12 |
| R11 | [29. R11 - System/Cycle і вибір вправ](prompts/29-R11-cycle-and-picker-ui.md) | GPT-6.1 Sol / Medium | R07, R09, P07, P13, P14 |
| R12 | [30. R12 - Statistics: зрозумілий доказ прогресу](prompts/30-R12-statistics-ui.md) | GPT-6.1 Sol / Medium | R07, P12, P17 |
| R13 | [31. R13 - Profile, backup і корисний local Architect](prompts/31-R13-profile-and-local-architect-ui.md) | GPT-6.1 Sol / Medium | R07, P04, P05, P15, P16 |
| R14 | [32. R14 - Ненав'язливий прогрес і повернення після паузи](prompts/32-R14-supportive-progression.md) | GPT-6.1 Sol / High | R08, R12, P10, P11, R01 |
| R16 | [33. R16 - Free/Pro contracts без продажів](prompts/33-R16-feature-access-policy.md) | GPT-6.1 Sol / High | R01, P16 |
| P18 | [34. P18 - Справжні UI-сценарії всього застосунку](prompts/34-P18-real-app-ui-journeys.md) | GPT-6.1 Sol / High | P01, P02, P03, P04, P05, P06, P07, P08, P09, P10, P11, P12, P13, P14, P15, P16, P17, R08, R09, R10, R11, R12, R13, R14, R16 |
| P19 | [35. P19 - HUD polish, доступність і зрозумілі тексти](prompts/35-P19-hud-accessibility-localization.md) | GPT-6.1 Sol / High | P18, R07 |
| R15 | [36. R15 - Рух, який допомагає, а не заважає](prompts/36-R15-purposeful-motion.md) | GPT-6 Luna / Medium | P18, P19, R07 |
| P20 | [37. P20 - Вимірювана швидкість запуску й плавність](prompts/37-P20-measured-performance.md) | GPT-6.1 Sol / High | P18, P19, R15 |
| P21 | [38. P21 - Зменшення складності без великого переписування](prompts/38-P21-bounded-architecture-cleanup.md) | GPT-6.1 Sol / High | P18, P20 |
| P22 | [39. P22 - Надійні quality gates і підтримуваний build](prompts/39-P22-ci-and-build-tooling.md) | GPT-6.1 Sol / High | P06, P18, P21 |
| P23 | [40. P23 - Приватність, права на assets і чесні обіцянки](prompts/40-P23-privacy-assets-release-copy.md) | GPT-6 Luna / Medium | P04, P05, P07, P09, P15, P16, P17, P19 |
| P24 | [41. P24 - Кандидат релізу, Android16 і16KB](prompts/41-P24-platform-release-candidate.md) | GPT-6.1 Sol / High | P20, P22, P23 |
| P25 | [42. P25 - Зрозумілий план beta для власника](prompts/42-P25-beta-study-and-owner-checklist.md) | GPT-6 Luna / Medium | P17, P23, P24 |
| P26 | [43. P26 - Незалежний GO/NO-GO перед beta](prompts/43-P26-independent-beta-audit.md) | GPT-6.1 Sol / High | P01, P02, P03, P04, P05, P06, P07, P08, P09, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22, P23, P24, P25 |
| R25 | [44. R25 - Підтримка, beta-фідбек і план інцидентів](prompts/44-R25-support-and-feedback-operations.md) | GPT-6 Luna / Medium | P26 |
| P27 | [45. P27 - Робота зі справжнім beta-фідбеком](prompts/45-P27-beta-feedback-iteration.md) | GPT-6.1 Sol / Medium | P26, R25 |
| R17 | [46. R17 - Перевірити Pro-цінність і цінову гіпотезу](prompts/46-R17-validate-pro-value.md) | GPT-6.1 Sol / High | P27, R16 |
| R18 | [47. R18 - Одна реальна Pro-можливість](prompts/47-R18-one-pro-capability.md) | GPT-6.1 Sol / High | R17, P12, R12 |
| R19 | [48. R19 - Вибрати billing provider і межі backend](prompts/48-R19-billing-architecture-decision.md) | GPT-6.1 Sol / High | R17, R16 |
| R20 | [49. R20 - Перевірені права: service або managed provider](prompts/49-R20-verified-entitlements.md) | GPT-6.1 Sol / High | R19 |
| R21 | [50. R21 - Android purchase adapter](prompts/50-R21-android-purchase-adapter.md) | GPT-6.1 Sol / High | R20, R16 |
| R22 | [51. R22 - Прозорий paywall і керування підпискою](prompts/51-R22-transparent-purchase-ui.md) | GPT-6.1 Sol / Medium | R21, R18, R13 |
| R23 | [52. R23 - Перевірка повного циклу підписки](prompts/52-R23-subscription-lifecycle-qa.md) | GPT-6.1 Sol / High | R22 |
| R24 | [53. R24 - Paid RC: прозорість, безпека й право продавати](prompts/53-R24-paid-release-candidate.md) | GPT-6.1 Sol / High | R23, P24, P27, R25 |
| R26 | [54. R26 - Опційний експеримент поширення прогресу](prompts/54-R26-optional-progress-sharing.md) | GPT-6.1 Sol / Medium | P27, P12, R12 |
| P28 | [55. P28 - Фінальна готовність до публічного запуску](prompts/55-P28-public-release-readiness.md) | GPT-6.1 Sol / High | P27, R25 |

## Додаткові release gates

Машинозчитуваний scope поточного public candidate (оновлює P28 разом із rationale/approval у DECISIONS):

Release candidate scope: undecided
Sharing in candidate: undecided

Значення: scope `undecided` / `free` / `paid`; sharing `undecided` / `no` / `yes`.
До завершення P28 обидва мають бути визначені за actual artifact, не припущенням валідатора.
`free` не вимагає R24; `paid` вимагає R24 і його prerequisites; `yes` вимагає R26.

P26 включає доказ завершення R07-R14/R16 через P18 та R15 через P20. Це gate Free beta.
R16 тут лише Free contract/provider; TTL/paid lifecycle лишаються R19-R23, не prerequisites Free beta.
P28 перевіряє актуальний scope кандидата:
- Free: весь core/R UI path, P27 findings і R25 support; R17-R24 можуть бути deferred, продажів/оманливого paywall немає.
- Paid: додатково R17-R24 done, owner pricing/legal/support/Console approvals.
- Sharing shipped: додатково R26 done і privacy/asset docs актуальні.
Рішення scope зафіксувати в DECISIONS; conditional gate не перетворювати на нескінченне блокування Free beta.
Нові bugs -> окремі repair IDs/evidence, не прихований великий rewrite в P28.

