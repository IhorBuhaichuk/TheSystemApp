# Прогрес R-пакета

Дата створення: 2026-10-08. Підготовка пакета виконана; фактичний стан кожної задачі в таблиці.
P01-P28 мають власний [реєстр](../implementation/PROGRESS.md), не дублюємо їх статуси.

- [x] Skills/контекст/Figma availability перевірені; стратегія і prompts підготовлені (див. SETUP_VERIFICATION).
- [x] Погоджені правки узгодженості та єдина нумерована папка всіх 54 prompts готові; [докази 2026-10-12](evidence/2026-10-12-plan-alignment.md). Це setup, не виконання R01-R26.
- [ ] R-реалізація завершена. Цей загальний пункт не закривати за сам факт створення MD.
- [x] R05 partial run2026-10-16:12 normal frames styled,13 shared action instances,
 8 secondary edges and actual390 resume/report/details path verified; [checkpoint](evidence/2026-10-16-r05-prototype-checkpoint.md).
  MCP quota and failed script environment leave22 bindings/remaining routes open.
- [x] 2026-10-16: D30 data-flow audit, точкові wiring fixes та55-step dependency order перевірені; [evidence](evidence/2026-10-15-r05-data-flow.md). Це не завершення всіх R05/native gates.

| ID | Scope | Статус | Evidence / blocker |
| --- | --- | --- | --- |
| R01 | Зафіксувати межі продукту | done | [Evidence](evidence/R01.md); strategy/MVP і [SC-R01-v1](FEATURE_SCOPE.md) F01-F53 перевірені, весь пакет A-D погоджено власником 2026-10-12 |
| R02 | Сценарії та карта дій | done | [Evidence](evidence/R02.md); UX_CONTRACT/ACTION_INVENTORY, 130 груп/12 routes/49 source paths перевірені; H01-H06 proposed до R04, G01-G06 open у своїх owners |
| R03 | Figma: файл, tokens і базові компоненти | done | [Evidence](evidence/R03.md); 7 native families/35 variants inspected, shared materials, radius propagation PASS, mobile 360x640 scroll/screenshot PASS; source unchanged, Android comparison deferred to implementation |
| R04 | Три напрями й вибір дизайну | done | [Evidence](evidence/R04.md); [accepted baseline/D29](../design/R04_SELECTED_DESIGN.md#final-acceptance--d29). B after D26-D28 explicitly approved2026-10-14; current titles/copy/header accepted, PNG/phone Present checks recorded. Unchosen artwork outside baseline; native material/accessibility/event proof stays R07/R08/P19. R05 has separate evidence |
| R05 | Прототип першої користі та тренування | in_progress | [Evidence](evidence/R05.md); v2 visual/content accepted D30/2026-10-15. [Data-flow audit](../design/R05_DATA_FLOW.md) and sequential prompt alignment authorized; secondary/360 prototype interactions and shared bindings remain open. Visual approval is not Android/runtime completion |
| R06 | Решта вкладок і системні стани у Figma | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R07 | Нативні primitives за затвердженим дизайном | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R08 | Onboarding і Status: ясна перша дія | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R09 | Зручний logging без втрати чернетки | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R10 | Calendar: план і факти без плутанини | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R11 | System/Cycle і вибір вправ | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R12 | Statistics: зрозумілий доказ прогресу | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R13 | Profile, backup і корисний local Architect | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R14 | Ненав'язливий прогрес і повернення після паузи | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R15 | Рух, який допомагає, а не заважає | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R16 | Free/Pro contracts без продажів | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R17 | Перевірити Pro-цінність і цінову гіпотезу | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R18 | Одна реальна Pro-можливість | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R19 | Вибрати billing provider і межі backend | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R20 | Перевірені права: service або managed provider | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R21 | Android purchase adapter | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R22 | Прозорий paywall і керування підпискою | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R23 | Перевірка повного циклу підписки | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R24 | Paid RC: прозорість, безпека й право продавати | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R25 | Підтримка, beta-фідбек і план інцидентів | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |
| R26 | Опційний експеримент поширення прогресу | pending | Prerequisites у [QUEUE](QUEUE.md); evidence ще немає |

| R27 | Зміни тренування лише на сьогодні | pending | [Контракт DF04](../design/R05_DATA_FLOW.md); prerequisites у QUEUE; runtime evidence ще немає |

Позначати checkbox у prompt разом із цим рядком і evidence. `done` лише за всі applicable gates.
Після approval-defer гілки R17-R24 статус deferred із посиланням на рішення, не done.

