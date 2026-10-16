# THE SYSTEM: мінімальна корисна версія

Оновлено: 2026-10-12, R01. Погоджений напрям: broad-audience personal system,
local-first, useful no-AI Free. **Конкретний scope SC-R01-v1 A-D погоджено**:
[FEATURE_SCOPE](docs/remaster/FEATURE_SCOPE.md), [DECISIONS](docs/remaster/DECISIONS.md).
Це критерії приймання, не заява про їхню поточну реалізацію. Статуси P/R живуть у [QUEUE](docs/remaster/QUEUE.md).

## Мета і мінімум

Людина отримує посильний план, розуміє сьогоднішню дію, легко її виконує/записує
і бачить правдивий результат. Найглибше ядро першого релізу — тренування.
Прості справи/звички необов'язкові; знайомство з аніме або RPG не потрібне.

Мінімальний Free шлях:
setup -> готовий цикл -> Today -> workout або належний rest/recovery ->
canonical запис/report -> власна історія/weekly proof -> наступний крок.
XP не потрібний для розуміння або виконання цього шляху.

## Must-have

| Можливість | Перевірюваний результат | Власник |
| --- | --- | --- |
| Короткий onboarding | Ціль/досвід/equipment/ритм дають schedule і конкретну дію; завершений setup не повторюється, відновлення узгоджене | P05, P07, R08 |
| Today Order | Тип дня, причина, одна дія, результат; training/rest/recovery/reduced/deload/no-plan/unknown/completed відрізняються | P08-P10, R08 |
| Missing-data fallback | Джерела/свіжість зрозумілі; немає впевненого readiness із neutral fallback; no-HC/no-AI корисні | P08, P09, P15, P16 |
| Set logging | Weight/bodyweight/time, completed/incomplete, швидке редагування; клавіатура не ховає дію | R09, P18 |
| Draft і finish | Lifecycle restore, один commit/reward, failure не губить input; saved state відрізняється від pending analysis | P02, P03, R09 |
| Короткий report | Виконане, реальний прогрес або insufficient data, local verdict, наступний крок | P03, P12, P16, R09 |
| History і correction | Власні записи доступні; target edit не змінює інші; chart має units/period/sample та empty state | P01, P12, R12 |
| План/Calendar | Inspect/edit базового циклу, заміна вправ, план vs факти, OFF/recovery узгоджені між екранами | P10-P14, R10, R11 |
| Короткий weekly/local brief | Один базовий висновок і до 1-3 релевантних дій; без fake AI або платної заглушки | P12, P16, R12, R13 |
| Прості справи | Наявні create/edit/check/reorder paths доступні; необов'язкові, не впливають на силові показники | R02, R08, R10, P18 |
| Ненав'язливий прогрес | Real proof головний; XP/ранги другорядні; rest і повернення без нав'язаного додаткового workout | P10, P11, R14 |
| Backup/restore | Оголошений payload, export, validation, preview/confirm, atomic/non-destructive failure; DB/setup/route цілісні | P04, P05, R13 |
| Health Connect refusal | Лише sleep permission, пояснення; unavailable/denied/revoked не блокує Free | P15, R13 |
| Offline/no-AI/validation | Повний цикл без акаунта/ключа/покупки; AI mutations тільки через ValidateDirectives | P16, R16, P18, P24 |
| Daily consistency | Timezone/midnight/foreground/worker узгоджені, без duplicate progression | P10, P11, P18 |
| Native accessible UI | Shared tokens/rounded panels, critical text/actions доступні, жодних утрачених dialogs чи web UI | R07-R15, P18-P20 |
| Release trust | Реальні checks/artifact, asset/content review, privacy/support/Console evidence для обраного scope | P22-P26, R25, P28 |

Manual check-in короткий і optional, але incomplete write/read path закривається P09.
Health Connect не потрібний для першого workout; його shipped інтеграція все одно має бути завершеною.
Existing annual data не можна зламати через deferred redesign: P14 та restore/history gates залишаються.

## Should-have після must-have

- Додаткові пояснення/персоналізація старту, якщо вони змінюють придатність плану.
- Optional weight/avatar/деталі RPG за потребою, без розростання onboarding.
- Невеликі purposeful transitions після реальних accessibility/performance checks.
- Зручний presentation локальної діагностики без витіснення основних дій.
- Покращення secondary surfaces за R06 disposition; дизайн має явні approval або preserved baseline.

Існуючі optional дії не видаляти через цей список. Must-have accessibility всіх shipped
surfaces не можна перевести в should-have. Новий таймер або інша фіча потребує beta-потреби.

## Скорочення, погоджені SC-R01-v1

A: прибрати disabled F45/F46 cards, окремий F38, абстрактний F37 із основного огляду;
об'єднати duplicate F39, замінити каральну presentation F36 підтримкою повернення.
B: спростити доступ до F35/F52, зберігаючи дії/дані; XP/ранги другорядні, diagnostics окремо.
C: не розширювати/просувати F23/F33/F42/F43/F51 до beta; existing annual/history routes доступні.
D: зберегти весь Free core, safety, corrections та доступ до власних даних.

Точні причини, owners і межі — лише у FEATURE_SCOPE. Approval всіх частин A-D отримано 2026-10-12.
R02 визначає конкретний action inventory/hierarchy, R04-R06 затверджують дизайн.
Скорочення presentation не дозволяє відключити underlying checks чи змінити XP/recovery без тестів.

## Not-now

Live subscriptions до R17-R24; новий AI/backend заради маркетингу; social/leaderboards,
mandatory account/cloud sync, nutrition platform, broad task-manager expansion,
професійний annual planning, багато тематичних модулів та web UI.
Растрові концепти не замінюють editable Figma і native Compose handoff.

Pro candidate лише додає регулярну користь понад Free. Новий FeatureAccess contract R16
не блокує поточні можливості; TTL/provider/paid lifecycle не є gate Free beta.

## Готовність beta

- R01 конкретний scope схвалений; R02 всі дії простежені; R06 реальні prototype observations/approval.
- Clean-install setup створює usable schedule; Today/refusal/no-plan/unknown пояснення відповідають дії.
- Реальний workout logging/draft/process restore/idempotent finish/history edit пройдені на Android.
- Backup round-trip/import failure/restore route мають докази без втрати даних.
- Rest/quests/streak, календар та daily sync мають cross-flow tests.
- No-AI/offline, denied/unavailable HC працюють у candidate; permissions/data use пояснені.
- 5 tabs та secondary actions reachable; 360x640/normal, fontScale 1.0/1.3, P19 додатково 2.0/TalkBack.
- Compile, unit tests, Room/Compose guards, lint, release artifact та потрібні runtime checks пройдені.
- Expert review тренувального контенту/правил, assets/privacy/support/manual owner gates записані.
- P26 незалежний GO/NO-GO за actual candidate; непройдені критичні gates не маскуються зеленим JVM run.

Докладна оркестрація: QUEUE -> P18-P26. Наявність цих MD не доводить проходження gates.
P03 still needs device; документ не змінює цей факт. Телефон для R01 не потрібний.

Beta proposal: 8-12 добровільних дорослих, приблизно два тижні; P25 уточнює план.
Early prototype R06 має окремі 3-5 людей до Compose, це не та сама перевірка.
Спостерігати перший самостійний план/workout, розуміння Today, logging friction,
повернення після паузи, реальний прогрес і зайві блоки. Показувати абсолютні counts і limits.

## Готовність public release

P27 findings опрацьовані; core/data-safety/scope blockers закриті; beta evidence не вигадане.
P28 фіксує artifact checksum, актуальні Android/device/ABI перевірки, privacy/contact/store
assets/signing/Console та support/rollout/rollback. R25 підтримка доступна до launch.

Free candidate не потребує paid гілки. Paid candidate додатково потребує R17-R24,
pricing/provider/privacy/owner approvals і sandbox lifecycle evidence. Shipped sharing потребує R26.
Scope/sharing fields QUEUE мають відповідати artifact. Публікацію власник запускає окремо.

Release/privacy тексти не обіцяють лікування, виміряну readiness або гарантовану трансформацію.
Фактичні public requirements перевіряються на дату відповідної release задачі.
Artifacts: README, PRIVACY_POLICY, STORE_LISTING, HC rationale, screenshot checklist,
actual screenshots і release notes з evidence, а не лише шаблони.

## Межа скорочення

За нестачі часу відкладаємо додаткові effects, remote AI, нові annual/challenge/nutrition
можливості й розширення кастомізації. Чинні дані та доступні потрібні дії зберігаємо.

Не скорочуємо: usable first plan, Today clarity, completed/incomplete, durable draft,
одноразовий finish, target correction, history, no-AI fallback, validation, backup/restore,
пояснення unknown/refusal, accessibility та release checks. Підсумкова якість визначається
реальними результатами користувача й перевірками, а не кількістю функцій або документів.
