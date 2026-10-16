# Figma і код: карта відповідності

Стан 2026-10-14: [THE SYSTEM Remaster / повний Status](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=188-178).
Starter MCP limit підтверджений, Chrome editor доступний. R03 masters/35 variants збережені;
демонстраційні3:117/12:43 прибрані за запитом власника, їхні докази є історичними.
Точні values/symbols/variants/limits: [R03_FOUNDATIONS](R03_FOUNDATIONS.md); ID ledger: [R03_FIGMA_STATE](R03_FIGMA_STATE.json).
R04 done: власник явно схвалив поточний початковий Status/D29,2026-10-14.
Прийнятий baseline: [Selected design / D26-D29](R04_SELECTED_DESIGN.md), не історичні A/B/C.
Усі10 вибраних іконок/W02 застосовані до обох phone frames; винагорода75 без дубля XP.
01 Status/page0:1: один editable overview188:178,390x946, увесь вміст видимий на полотні.
02 Phone previews/page190:210:39:683/39:1084,360x640/390x844; обидва Present top/bottom перевірені.
03 Components/page190:211:8 System roots та2 shared nav masters. Кандидатні дошки44:768/44:1499,
старий B-v1 і R03 demos видалені; потрібні компоненти/локальна історична evidence збережені.
D27: «Деталі прогресу» прибрано з усіх3 Status frames; дані надалі у Statistics/R06-R12,
без видалення storage/розрахунків. Зайвий нижній резерв скорочений, gap після останньої справи24px.
D28: «1 / 3 виконаних тренувань» видалено з усіх3 frames;7 дат/стани W02 лишилися,
normal week тепер192:102 (source-equivalent restoration), інші current nodes незмінні.
Поточні назви/copy/header/material appearance прийняті як показано/D29, без H01-H05.
Runtime/material bindings/contrast не перевірені в Android: owners R07/R08/P19.
R05 in_progress / v2 visual-content accepted D30: [current v2/node table](R05_V2_REVIEW.md).
Owner now authorizes code data-flow audit and prompt reconciliation: [contract](R05_DATA_FLOW.md).
NEW34 native phone frames:17 at360x640 and17 at390x844. Review sections390 `223:1022`,360 `225:1872`.
390 main fixture path and compact logger rendering checked; secondary/360 interactions,
complete shared bindings and consistent cross-screen fixtures remain open; visual approval is recorded. Not a completed design-system handoff.
Historical v1:34 specimens/partial360 walkthrough, [ledger](assets/r05/native-scenes.json).
Use v2 as the accepted visual baseline; preserve v1 evidence and approved Status. New Android flows remain pending.
`A` має значення з FEATURE_MAP. Спільні tokens не дублювати окремо на кожному екрані.

R05 execution2026-10-16: shared R05 Action masters271:39/271:43/271:47 on190:211;
G..D twelve390 frames now have shared text/paint bindings and13 action instances.
Eight secondary edges saved; D -> L1 -> F -> R -> RD -> R tested in actual Present.
[Exact node ledger](assets/r05/closure-state.json), [checkpoint/limits](../remaster/evidence/2026-10-16-r05-prototype-checkpoint.md).
Remaining5 normal +17 compact frame bindings and other state/route coverage remain open;
MCP Starter quota exhausted and browser script environment failed. D29 Status not modified.

| Область | Код зараз | Figma file/node | Approval / states |
| --- | --- | --- | --- |
| Tokens | `A/core/theme/SystemTokens.kt`, `Theme.kt`, `Dimensions.kt`, `Type.kt` | File-level styles/variables; [Components page190:211](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=190-211) | 56 semantic aliases, 17 Roboto styles; browser counts/scopes and tested radius path verified |
| Surfaces | `A/core/ui/components/SystemPanels.kt`, `SystemGlassComponents.kt` | [Surface3:23](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=3-23), page190:211 | 7 families/35 variants retained; radius 24->26->24 PASS; 11 shared effects/9 paint styles. Old review3:117 deleted; historical evidence in R03 |
| Navigation | `A/core/ui/components/SystemBottomNavBar.kt` | [Nav36044:705](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=44-705), [Nav39044:747](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=44-747), page190:211 | All5 chosen icons applied/D26; label «Тренування»/D25. Instances144:156/48:236 Fixed, internal groups Scroll with parent. Other-tab states/Android insets remain R06/native gates |
| Status | `A/feature/status/ui/RpgStatusDashboard.kt`, `RpgTodayOrderBlock.kt` | [Full overview188:178](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=188-178); [B36039:683](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=39-683), [B39039:1084](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=39-1084), page190:210 | Appearance approved D29; synthetic initial-day design. R05 adds STA-01 -> plan fixture at360, without layout edits; other actions/R08/native contrast/material proof remain open |
| Workout | `A/feature/status/ui/components/workout/ActiveDayCard.kt` | [W0/360](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=202-115), [W0/390](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-637) | R05 first iteration, not approved; input/first explicit completion walkthrough at360 only; draft/add/edit variants pending |
| Report | `A/feature/status/ui/components/dialogs/WorkoutReportDialog.kt` | [RP1/360](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-156), [RP1/390](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-853) | R05 synthetic local partial report; no actual commit/domain computation; owner approval pending |
| Calendar | `A/feature/calendar/ui/CalendarScreen.kt` | pending R06 | empty/planned/rest/history |
| Training/Cycle | `A/feature/cycle/ui/CycleScreen.kt` | screen pending R06; nav label approved/D25 | Тренування replaces Система in Figma; code route Cycle unchanged. plan/edit/active workout |
| Statistics | `A/feature/statistics/ui/StatisticsScreen.kt` | pending R06 | no history/real progress/sparse data; D27 approved destination for former Status progress information (streak/level/rank fixture), design R06/native R12, not yet implemented |
| Profile | `A/feature/profile/ui/ProfileScreen.kt` | pending R06 | settings/backup/permissions |
| Onboarding | `A/feature/onboarding/ui/OnboardingScreen.kt` | [N00/360](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-219), [N00/390](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=205-135) | R05 first iteration, five fixed-fixture steps;360 Present path verified; arbitrary selection/retry and owner approval pending. Usable starter schedule implementation remains P07 |
| Architect | `A/feature/architect/ui/ArchitectScreen.kt` | pending R06 | local/unavailable/proposals |
| CalendarSettings | `A/feature/calendar/ui/CalendarSettingsScreen.kt` | disposition pending R06 | settings/long labels/back; R10 |
| ExercisePicker | `A/feature/exercise_search/ui/ExerciseSearchScreen.kt` | disposition pending R06 | search/selection/save/back; R11 |
| WorkoutScheduleSettings | `A/feature/status/ui/components/dialogs/WorkoutScheduleSettingsDialog.kt` | disposition pending R06 | edit/validation/confirm/cancel; R11, R09 contract |
| AnnualProgressionPlan | `A/feature/architect/ui/AnnualProgressionPlanScreen.kt` | disposition pending R06 | empty/existing/create/back; R11 |
| AnnualProgressionDetails | `A/feature/statistics/ui/AnnualProgressionDetailsScreen.kt` | disposition pending R06 | sparse/details/edit/back; R12 |
| AnnualProgressionManualEditor | `A/feature/statistics/ui/AnnualProgressionManualEditorScreen.kt` | disposition pending R06 | validation/save/cancel; R12 |
| WorkoutAnalysis | `A/feature/architect/ui/WorkoutAnalysisScreen.kt` | disposition pending R06 | local/unavailable/result/back; R13 |
| Backup import | `A/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt` | disposition pending R06 | preview/confirm/failure/cancel; R13 |
| Purchase | NEW, після R19/R22 | pending | free/loading/pending/active/expired |

Для затвердженого рядка додати node URL, named component/variant, token mappings,
дату approval, source commit/local diff, viewport/font/data fixture та evidence Android comparison.
Не використовувати `pending` як дозвіл відтворювати дизайн навмання.

## Повнота handoff

### R05 Core Nodes / First Iteration

All nodes below are editable prototype specimens, **not owner-approved R05 or Android screens**.
Only the360 fixture forward path and X0 Back have Present evidence. Normal-size reactions remain
unfinished; scene existence alone is not a test. Stable node/reaction metadata: [native ledger](assets/r05/native-scenes.json).

| State | 360x640 | 390x844 |
| --- | --- | --- |
| Empty name N00 | [203:219](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-219) | [205:135](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=205-135) |
| Name keyboard N01 | [203:243](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-243) | [203:361](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-361) |
| Filled name N02 | [198:193](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=198-193) | [203:422](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-422) |
| Goal G3 | [198:252](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=198-252) | [203:445](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-445) |
| Equipment E2 | [198:291](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=198-291) | [203:483](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-483) |
| Experience X0 | [198:340](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=198-340) | [203:532](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-532) |
| Cycle C0 | [198:373](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=198-373) | [203:565](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-565) |
| Plan entry PLAN | [202:76](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=202-76) | [203:598](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-598) |
| Initial logging W0 | [202:115](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=202-115) | [203:637](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-637) |
| Numeric keyboard WK | [203:91](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-91) | [203:689](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-689) |
| Entered/incomplete WV | [202:166](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=202-166) | [203:731](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-731) |
| One completed W1 | [202:217](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=202-217) | [203:783](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-783) |
| Saving one set WS1 | [203:191](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-191) | [203:835](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-835) |
| Local partial report RP1 | [203:156](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-156) | [203:853](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=203-853) |

Additional360 specimens, not complete branches: N03 validation203:304, W2 two sets202:271,
W3 three sets202:324, WS three-set saving202:378, RP three-set report202:396, WE failure203:133.
PLAN is a core entry fixture, not a replacement approval for the full Cycle screen/R06.
Approval pending. Main walkthrough starts at203:219; the editable approved Status overview stays188:178.

R06 звіряє всі shipped screens/dialogs із [ACTION_INVENTORY R02](ACTION_INVENTORY.md), не лише ці entry points.
State/journey requirements: [UX_CONTRACT](UX_CONTRACT.md). Для action IDs позначити equivalent
destination/node або approved hide; W/U/D/P не змішувати. G01-G06 є implementation gaps,
не створеними Figma actions. Рядки preserve-current не дозволяють пропустити action/state regression.
Кожен secondary рядок отримує `new-approved` (node, variants, approval) або
`preserve-current` (source revision/local diff, опис збережених states/actions,
baseline screenshot якщо доступний, відповідальний R-task). Відсутність screenshot
не є новим макетом; її записують як visual verification gap.
Для `preserve-current` новий node не потрібний: зберегти presentation, перевірити
вплив спільних tokens та навігацію. Інший layout потребує окремого approval.
Hide/defer продуктового scope не дозволяє видалити дані чи загубити доступні дії.

R06 також створює `docs/design/PROTOTYPE_FEEDBACK.md` для core prototype R05;
ця рання перевірка з людьми й owner approval є gate перед R07, не Android QA.
