# Рішення та відкриті питання

Створено: 2026-10-08; оновлено R01: 2026-10-12. `accepted-direction` не дорівнює затвердженому pixel-perfect макету чи дозволу видалити дані.

| ID | Статус | Рішення / наступний доказ |
| --- | --- | --- |
| D01 | accepted | Kotlin + Compose + Room + Hilt, поточні модулі. Переписування на web/іншу мову не планується |
| D02 | accepted | Широка аудиторія, персональна система; тренування як найглибше ядро, прості звички допоміжні |
| D03 | accepted | Надійність і повний no-AI core loop перед новими великими функціями |
| D04 | accepted-direction | Гейміфікація ненав'язлива, реальний прогрес головний; конкретні спрощення затверджує R01/R14 |
| D05 | accepted-direction | Editable Figma -> прототип -> Compose; растровий концепт не є макетом із точними розмірами |
| D06 | accepted | Повторюваний стиль у shared tokens/components; великі округлені панелі зберігаються до окремого рішення |
| D07 | accepted-direction | Free + один Pro; monthly/yearly з однаковими правами. Продажі не є prerequisite для Free beta |
| D08 | accepted | Safety, власна історія, локальний backup, виправлення помилок і базове виконання не платні |
| D09 | accepted | AI suggests, system decides; ValidateDirectivesUseCase лишається gatekeeper |
| D10 | accepted | По одному prompt за раз; MD актуалізуються за доказами, completed робота не перезапускається |
| D11 | accepted / R01 / 2026-10-12 | Усі F01-F53 у [SC-R01-v1](FEATURE_SCOPE.md) погоджені як продуктовий scope; конкретні A-D схвалені після пояснення, що це один пакет. Код/дії/storage ще не змінені |
| D12 | open / R02-R06 | Назви вкладок, видимість RPG, конкретна композиція, Figma file/node URLs і owner approval |
| D13 | open / R17-R19 | Одна регулярна Pro-цінність, ціна, trial, billing provider, ідентичність, юрисдикції продажів |
| D14 | open / R19-R21 | Paid lifecycle/TTL/downgrade/restore policy затверджує R19 після R17, реалізує R20/R21; не блокує мінімальний Free-only R16 |
| D15 | open / P25-P28 | Tester cohort, права на assets, expert review, privacy contact, signing і Console gates |
| D16 | accepted / 2026-10-08 | Після consistency audit погоджено historical/current маркери, R09 -> R11, повний secondary handoff, evidence/dependency guard, мінімальний R16 і ранній prototype UX-check у R06 перед R07; це правка плану, не виконання R-задач |
| D17 | open / P28 | Actual public candidate: Free/Paid і shipped sharing; effective fields у QUEUE лишаються undecided до artifact/owner decision. Не блокувати Free release deferred paid гілкою |
| D18 | accepted / 2026-10-12 | На запит власника всі 54 повні P/R prompts в одній папці docs/remaster/prompts із NN-ID порядком. QUEUE є джерелом current-stage/active/next pointers; P/R progress та evidence не дублювати й не втрачати |
| D19 | accepted / R01 / 2026-10-12 | [SC-R01-v1 A-D](FEATURE_SCOPE.md#погоджений-пакет-a-d): прибрати dead F45/F46 cards, окрему F38/основну F37, merge F39, supportive F36; спростити доступ F35/F52 та diagnostics F48; defer expansion F23/F33/F42/F43/F51; зберегти весь Free/data core. Exact approval у [evidence](evidence/R01.md) |
| D20 | proposed / P10-P12/P17 | Тренувальний north-star: committed planned workout days / eligible historical planned days; OFF/recovery окремо, no-plan/unknown не completion, нуль denominator -> N/A. Замінює старий mixed mission rate; це product definition, aggregation ще needs contract/tests |
| D21 | proposed / R04 / 2026-10-12 | [UX_CONTRACT H01-H06](../design/UX_CONTRACT.md#пропозиція-зрозумілої-ієрархії): plain-language priority із current route names/order. Конкретні composition/locations чекають frame/hierarchy approval до R04; R02 static docs не змінюють layout |
| D22 | recorded / R02 / 2026-10-12 | [ACTION_INVENTORY](../design/ACTION_INVENTORY.md): current callbacks/states/sources vs planned P07-P17. UX-R02-G01..G06 (history entry, date reset, error consumer, no-op bulk contract, todo count-as-questId, dormant microtasks) відкриті, assigned existing P/R owners; не оголошені виправленими |
| D23 | rejected / R04 / 2026-10-13 | [R04-v1 A/B/C](../design/R04_DIRECTIONS.md) відхилено власником (1/10); рекомендація старого B відкликана. Не використовувати ці frames як approval |
| D24 | accepted-placement / R04 / 2026-10-13 | Власник обрав новий B для розміщення елементів і дозволив прибрати A/C. [B refinement checklist](../design/R04_B_REFINEMENT_CHECKLIST.md) фіксує всі уточнення: 60 альтернатив, copy, кнопки, menu/reorder, shared-master safety. Деталі ще потребують вибору; R04 in_progress, не final design approval |
| D25 | accepted-label / R04 / 2026-10-13 | Після перевірки CycleScreen власник обрав «Тренування» замість «Система». Оновити shared Figma nav; внутрішній Cycle route і NAV-SYSTEM IDs не змінювати. [Round 2](../design/R04_B_REVIEW_ROUND_2.md) зберігає матрицю кандидатів; це не approval фінальної іконки чи дозвіл змінювати Kotlin |
| D26 | accepted-details / R04 / 2026-10-14 | Власник обрав10 іконок і W02, текст винагороди75 без повторного XP; дозволив застосувати у Figma, показати повний вміст на полотні й прибрати допоміжне. [Selected design](../design/R04_SELECTED_DESIGN.md) фіксує точні IDs та safe cleanup: необхідні masters зберегти, header/title/progress disposition не вважати погодженими |
| D27 | accepted-placement / R04 / 2026-10-14 | Власник прибирає «Деталі прогресу» зі Status, включно з Figma-макетом; інформацію блоку надалі показати у вкладці «Статистика». Прибрати зайвий нижній порожній простір, залишивши лише потрібний відступ/резерв фіксованої навігації. [Selected design](../design/R04_SELECTED_DESIGN.md#progress-placement--d27) визначає межі: зараз Figma/MD; R06 проєктує Statistics, R08/R12 реалізують, дані/розрахунки не видаляти |
| D28 | accepted-copy-removal / R04 / 2026-10-14 | Власник прибирає «1 / 3 виконаних тренувань» зі Status як зайву візуальну інформацію; залишити дати й позначки тренувань у W02. Зараз прибрати лише текст із3 Figma frames, зберегти weekly metrics/дані; майбутня native відповідність R08. [Selected design](../design/R04_SELECTED_DESIGN.md#weekly-counter--d28) |
| D29 | accepted-final-status / R04 / 2026-10-14 | Власник прямо підтвердив, що поточний макет початкового Status повністю влаштовує. Прийнято B після D26-D28:188:178/39:683/39:1084, поточні «Статус»/«Справи»/recommendation copy, іконки/кнопки/матеріал, без додавання H01-H05. [Final acceptance](../design/R04_SELECTED_DESIGN.md#final-acceptance--d29) закриває R04 design-selection scope; prototype R05, інші екрани R06, Compose/contrast/insets R07-R08/P19 не оголошені готовими. R05 окремим запуском |
| D30 | accepted-visual-and-flow-audit / R05 / 2026-10-15 | Власник: «макет ... повністю мене влаштовує», просить перевірити controls/data flow, додати необхідні зв'язки й упорядкувати prompts. Прийняті поточні17 v2 states/34 frames; попередній hold code/prompts скасований. [Контракт](../design/R05_DATA_FLOW.md) і [evidence](evidence/2026-10-15-r05-data-flow.md). Локально виправлено todo add та foreground errors; нові domain flows виконуються поетапно. R27 inserted as16 для today-only persistence;55 prompts, старі IDs незмінні. Visual approval не закриває prototype/runtime/clinical gates |

Початкова авторизація: створити стратегію, правила й повний поетапний пакет; вона сама по собі не
затверджувала макет чи тарифи. Пізніше D29 явно затвердив початковий Status; це не approval інших
екранів, production behavior або виробничих тарифів. Загальна довіра не замінює окремі gates.
2026-10-12 власник окремо запустив R01: документаційна ревізія strategy/MVP/F01-F53 виконана.
Спочатку SC-R01-v1 був proposed; після пояснення A-D власник відповів:
«Тоді я погоджуюся, рішення мене все влаштовує». Це схвалення всього пакета SC-R01-v1 A-D.
Approval записаний у FEATURE_SCOPE/evidence, R01 закритий. R02/реальні UI зміни окремим запуском;
решта екранів/Pro залишаються open; Status прийнятий D29. D20 є metric proposal для контрактів P10-P12/P17, не готовою aggregation.
Новий факт: додай дату, ID, rationale, impacted tasks і evidence; попереднє рішення познач superseded,
а не переписуй історію так, ніби невизначеності не існувало.
