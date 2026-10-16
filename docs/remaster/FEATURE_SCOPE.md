# R01: межі продукту та 53 функції

Версія: **SC-R01-v1**, 2026-10-12. Статус: **approved**.
Власник погодив усі чотири частини A-D після пояснення, що вони утворюють один пакет.
Kotlin, навігація, Room та дані цією документаційною задачею не змінені.
Загальний напрям D02-D09 та конкретні межі цього пакета погоджені; implementation виконується поетапно.

## Намір і джерела

THE SYSTEM допомагає обрати посильну дію, виконати її, зберегти результат і побачити розвиток.
Аудиторія широка, знайомство з аніме не потрібне. Найглибше ядро MVP: тренування;
прості особисті справи/звички підтримують ширшу ідею персональної системи.

F-ID та попередні оцінки взяті з [датованого огляду](../product-review/2026-10-08/03-FEATURE-VALUE.md).
Фактичний стан і залежності: [FEATURE_MAP](../architecture/FEATURE_MAP.md), потім точковий код.
Цей документ задає бажані межі, не підтверджує виконання P/R-задач або користь у beta.
Черга: [QUEUE](QUEUE.md); продукт: [стратегія](../../PRODUCT_STRATEGY.md), [MVP](../../MVP_DEFINITION.md).

## Як читати рішення

- `keep`: зберегти користь і чинні контракти; це не доказ відсутності bugs.
- `improve`: доробити наявну можливість у названих P/R-задачах.
- `defer`: не розширювати й не робити умовою першої користі; збережені дані й потрібні дії захищені.
- `consider-hide`: погоджене спрощення presentation у межах A-D; точне зіставлення дій у R02, storage/domain rules зберігаються.

Усі рішення таблиці є погодженим продуктовим scope SC-R01-v1. Незмінні safety/Free інваріанти збережені;
пакет не є дозволом автоматично виконати всі перелічені P/R-задачі. Позначення `Free`
описує доступ до функції, якщо вона включена; не зобов'язує добудувати deferred модуль до beta.

## Початок і щоденне рішення

| ID | Функція | Рішення | Межа та причина | Owner tasks |
| --- | --- | --- | --- | --- |
| F01 | Ім'я/профіль | keep | Free; коротке знайомство, редагування пізніше | P07, R08, R13 |
| F02 | Ціль | improve | Free; відповідь реально визначає стартовий план | P07, R08 |
| F03 | Досвід | improve | Free; складність плану/пояснень, RPG level не вимірює підготовку | P07, R08 |
| F04 | Обладнання/обмеження | keep | Free; план має бути виконуваним удома/в залі | P07, P13, R11 |
| F05 | Стартовий цикл | improve | Free; готовий schedule і перша дія після setup | P07, R08 |
| F06 | Today Order | improve | Free; що, чому, одна дія, результат; no-plan/unknown окремо | P08, R08 |
| F07 | Типи дня | keep | Free; training/reduced/deload/recovery/rest, проста мова без нових правил | P08, P10, R08 |
| F08 | Короткий варіант повернення | improve | Free; добровільний, не обходить rest/recovery заради streak | P08-P11, R14 |
| F09 | Readiness | improve | Free; джерела/свіжість/невідомість, без хибної точності | P08, P09, R08 |
| F10 | Ручний check-in | improve | Free; короткий і необов'язковий, лише релевантні рішенням поля | P09, R08 |
| F11 | Health Connect sleep | improve | Free, optional; denied/unavailable не блокує план; лише потрібні дозволи | P15, R13 |

## Виконання і планування

| ID | Функція | Рішення | Межа та причина | Owner tasks |
| --- | --- | --- | --- | --- |
| F12 | Logging підходів | improve | Free; швидкий ввід, явні completed/incomplete, виправлення | P02, P03, R09 |
| F13 | Weight/bodyweight/time | keep | Free; одиниці й результат відповідають вправі | R09, P18 |
| F14 | Durable draft | keep | Free; не втрачати введене при lifecycle/process recreation | P02, R09, P18 |
| F15 | Finish/report | improve | Free; один commit/reward, короткий локальний результат | P03, R09 |
| F16 | History edit | keep | Free; змінює тільки цільові session/exercise sets | P01, P12, R12 |
| F17 | Каталог/пошук/власні вправи | keep | Free; придатний starter набір, решта за потребою | P07, P13, R11 |
| F18 | Technique/help | improve | Free; контекстна допомога, перевірені тексти й права на assets | P23, R09, P26 |
| F19 | Заміна вправ | improve | Free; equipment-aware, причина зрозуміла, введені sets захищені | P13, R09, R11 |
| F20 | Базовий редактор циклу | improve | Free; доступний після першого плану; складні поля за потребою | P07, P13, R11 |
| F21 | Calendar plan/history | improve | Free; майбутній план не переписує фактичну історію | P11, P12, R10 |
| F22 | OFF/recovery overrides | improve | Free; узгодити Today, quest, Calendar та streak | P10, P11, R10, R14 |
| F23 | Annual plan/manual matrix | defer | Не просувати до beta; existing plans/details/edit/export доступні, P14 safety обов'язковий | P14, R11, R12 |
| F24 | Recovery debt/window | improve | Free; пояснювана евристика, не виміряне одужання | P08, P09, R08, P26 |

## Прогрес і особисті справи

| ID | Функція | Рішення | Межа та причина | Owner tasks |
| --- | --- | --- | --- | --- |
| F25 | History/charts/proof | improve | Free; власна історія, units/period/sample, тільки підтверджені записи | P12, R12 |
| F26 | Weekly summary/next step | improve | Free; один короткий висновок із доказом, не кілька копій | P12, P16, R12, R13 |
| F27 | Вага тіла/історія | keep | Free, optional; не є воротами onboarding | R12, R13 |
| F28 | Зріст/вік | keep | Free, optional; запитувати лише з поясненою потребою формули | R08, R13 |
| F29 | XP/level | keep | Free; компактний другорядний прогрес, без нового mandatory RPG mode | P10, R14 |
| F30 | Ранги | keep | Free; додаткові деталі за інтересом, не умова виконати дію | R02, R14 |
| F31 | Серія | improve | Free; регулярність за планом, rest не є провалом | P10, P11, R14 |
| F32 | Main quest | improve | Free; подача реальної Today/workout дії, не другий планувальник | P08, P10, R08, R14 |
| F33 | Promotion/boss challenge | defer | Не обов'язковий MVP; existing milestones зберегти, майбутній opt-in без примусу до максимумів | R02, R14 |
| F34 | Прості todos/звички | keep | Free, optional; читання/справи без окремої предметної платформи | R02, R08, R10 |
| F35 | Nested microtasks/складний organizer | consider-hide | Пропозиція B: не розвивати; advanced доступ до чинних tasks зберегти | R02, R08, R10 |
| F36 | Penalty presentation | consider-hide | Пропозиція A: підтримувальне повернення; domain/XP policy змінювати тільки з тестами P10/R14 | P10, P11, R14 |
| F37 | Числові RPG-атрибути | consider-hide | Пропозиція A: прибрати з основного огляду, деталі/дані зберегти; не видавати за виміряне fitness | R02, R11, R14 |
| F38 | Motivation Level | consider-hide | Пропозиція A: приховати окрему оцінку, не називати її психологічним вимірюванням | R02, R11, R14 |
| F39 | Bonus panel | consider-hide | Пропозиція A: об'єднати повторені лічильники; реальні bonus effects/actions залишити | R02, R11, R14 |
| F40 | Avatar/level-up | keep | Free; нейтральний аватар рівноцінний, привітання коротке й необов'язкове | R13-R15 |

## AI, довіра і приховані механізми

| ID | Функція | Рішення | Межа та причина | Owner tasks |
| --- | --- | --- | --- | --- |
| F41 | Local Architect brief | improve | Free; перевикористати F26 summary, до 1-3 корисних дій без мережі | P16, R13 |
| F42 | Remote generative analysis | defer | Не умова beta/Pro; release Gemini disabled, порівняти користь з local brief | P16, R17, R19 |
| F43 | Live Coach/chat | defer | Пропозиція C: не включати в public MVP promotion; history зберегти, платний AI не обіцяти | P16, R02, R13 |
| F44 | ValidateDirectives | keep | Gatekeeper; mutation лише після domain validation | P16, P18 |
| F45 | Disabled cycle-correction card | consider-hide | Пропозиція A: прибрати порожній entry; реальні cycle edit/return actions зберегти | R02, R13, R14 |
| F46 | Disabled tomorrow-plan card | consider-hide | Пропозиція A: прибрати порожній entry; наступний день у Calendar/report | R02, R10, R13 |
| F47 | Backup export/import | improve | Free; повний оголошений payload, preview/confirm, безпечний failure/restore | P04, P05, R13 |
| F48 | Local beta metrics | keep | Локальна діагностика; не в основній користувацькій ієрархії, recorder не видаляти | P17, R12 |
| F49 | Offline/no-AI core | keep | Повний Free досвід без ключа, акаунта чи покупки | P16, R08-R13, P24 |
| F50 | Daily sync/worker | improve | Надійність Free; midnight/timezone/concurrency/foreground catch-up | P11, P18 |
| F51 | Nutrition floor/UI | defer | Пропозиція C: не добудовувати food tracker; наявні дані/читачі зберегти | R02, P09, P16 |
| F52 | Два swipe Status modes | consider-hide | Пропозиція B: не приховувати потрібні дії за жестом; точний merge/контроль визначає R02 | R02, R05, R08 |
| F53 | HUD/shared tokens/motion | improve | Native Compose; характер, спільні округлення, читабельність і purposeful motion | R03-R07, R15, P19, P20 |

## Погоджений пакет A-D

A, B, C, D є чотирма сумісними частинами одного пакета, а не альтернативами для вибору.
Усі чотири погоджені власником 2026-10-12.

**A. Спростити головну подачу.** F45/F46: прибрати лише недоступні картки; F38:
приховати окремий Motivation Level; F37: прибрати абстрактні числові атрибути з основного огляду;
F39: об'єднати дубльовані лічильники з реальним прогресом. F36: прибрати каральні тексти/центральну
penalty zone, зберегти правдиві пропуски та шлях повернення. Якщо це потребує зміни streak/XP,
P10/R14 спочатку формалізують правило й тести; approval presentation не дозволяє обнулити дані.

**B. Зберегти дії, спростити доступ.** F35: звичайний список задач головний, nested/advanced
можливості не розширюємо; existing tasks/edit/reorder не зникають. F52: потрібні дії обох
режимів мають видимий доступ; конкретна композиція/merge ще open R02-R06. F29/F30: XP/ранги
другорядні, без окремого нового game-mode toggle. F48: діагностика поза основною ієрархією,
у доступному diagnostics section; дані/recorder зберігаються. Місця переходів затверджує R02.

**C. Звузити запуск.** F23/F33/F42/F43/F51: не добудовувати й не активно просувати до beta.
Річні плани лишаються advanced з доступом до збереженого; chat/nutrition не є gate core loop.
Дані та діючі читачі не видаляються. F23 все одно проходить P14 data safety; R06 явно позначає
secondary surfaces `preserve-current` або `new-approved`. Deferred module не отримує fake paywall.

**D. Зберегти повний Free.** F01-F22, F24-F32, F34/F40/F41/F44/F47/F49/F50/F53 залишаються
безкоштовними в погодженому обсязі. F33 та інші deferred не стають платними автоматично.
History, corrections, draft, finish, backup/restore, permission refusal, validation та
доступність не скорочуються. Звички не змінюють силові/health показники.

Approval SC-R01-v1 A-D отримано 2026-10-12. Точна відповідь власника:
> Тоді я погоджуюся, рішення мене все влаштовує

Контекст: підтвердження всього пакета після пояснення A-D; доказ у [R01 evidence](evidence/R01.md).
Окремо open: фактичне розміщення/назви/merge у R02-R06; ціна/trial/provider/Pro у R17-R19;
зміни business policy мають власні контракти й тести. Цей approval не схвалює кінцевий дизайн.

## Free baseline і Pro кандидати

Мінімум Free: стартовий план -> Today -> workout або дозволений rest -> запис/report ->
history/weekly proof -> наступний крок; optional прості справи/check-in/HC; local backup.
Читати власні існуючі annual artifacts можна без покупки, навіть коли їхній redesign deferred.

Pro кандидат: одне корисне поглиблення понад базові F25/F26, наприклад порівняння періодів
із поясненим трендом та дією. Автоматизація понад F20 є альтернативою для перевірки, не другою
обов'язковою можливістю. R17 спочатку перевіряє recurring value; R18 реалізує одну погоджену
capability. AI, annual tools і cosmetics не стають Pro лише через складність реалізації.
Ціна, provider і запуск продажів open; [MONETIZATION_STRATEGY](../../MONETIZATION_STRATEGY.md).

## Як перевірити рішення

R02 зіставляє кожну shipped дію з новим місцем або явним погодженим disposition, особливо A-C.
R05/R06 перевіряють зрозумілість прототипу без аніме-контексту; це не замінює P18 Android tests.
P25-P27 збирають реальний beta feedback: корисна перша дія, логування, повернення, зайві блоки.
Якщо deferred можливість регулярно потрібна для основної дії, переглянути тільки її рядок із доказом,
оновити DECISIONS/MVP/QUEUE за DOC_MAINTENANCE. Не робити висновків про retention із самого документа.
