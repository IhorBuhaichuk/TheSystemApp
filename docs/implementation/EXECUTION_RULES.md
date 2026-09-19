# Execution Rules For P01-P28

Ці правила доповнюють AGENTS.md тільки для цього пакета задач. Прочитай їх перед одним обраним prompt.
Репозиторій: `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`.

## Контекст і економія

1. Почни з GRAPH_REPORT.md, AGENTS.md, свого prompt і його рядка PROGRESS.
2. Витягни лише вказані AUD-ID/roadmap sections та relevant FEATURE_MAP rows. Не читай усі28 prompt files або весь audit кожного разу.
3. Спочатку `git status --short` і scope diff: карта описує ревізію аудиту, код міг змінитися. Чужі зміни не скасовуй.
4. Читай entry files і прямі dependencies. `rg` за symbol/ID краще за повторне сканування всього app.
5. Для bug/risk: відтворення/тест -> найменша коректна зміна -> tests -> короткий evidence. Непідтверджений ризик не є автоматичним дозволом на refactor.
6. Не перечитуй історичні source dumps і не пиши великий план перед однорядковим fix. Не запускай однаковий build повторно без причини.
7. Model choice задає користувач у UI; не змінюй глобальні налаштування моделей. Якщо вузька задача виявилась суттєво складнішою, дай короткий handoff для сильнішої моделі, не крути нескінченні невдалі спроби.

## Межі змін

- Виконуй лише обраний P-ID. Не починай наступну задачу автоматично. Спільні файли не редагувати паралельно іншими задачами без узгодження.
- Прочитай BUGFIX/ROOM_CHANGE/NEW_FEATURE/UI_POLISH за типом зміни. Domain rules не переносити у Compose; ViewModel не звертається напряму до DAO.
- UI тільки native Kotlin/Jetpack Compose, shared tokens/primitives; жодних web prototypes/dependencies.
- Зберігати функціональні блоки, logging paths, dialogs, navigation, ownership і design identity. Не прибирати функцію для проходження тесту.
- Room зміна включає entity/DAO/database version/migration/exported schema/backup compatibility/tests. Відсутність публічних користувачів не дозволяє знищити developer data.
- AI suggests, system decides. Public Gemini disabled до окремого погодження. Не використовувати реальні платні API для regression tests.
- Ніяких автоматичних commit/push/merge, destructive reset/checkout, production data clearing, secrets у repo/logs або Play submission.

## Пристрій і дані

- Телефон не потрібен для читання коду, JVM tests, compile/lint/docs. Android instrumentation потребує Android-пристрою або емулятора; для performance потрібен однаковий фізичний reference device.
- Перед device tests перевір `adb devices -l`, serial/API/package variant. Кілька пристроїв -> явно вибраний serial; unauthorized -> попроси підтвердження на телефоні.
- Для clean install, migration, restore, process-death і benchmarks використовуй isolated test DB/package/profile/device. Не виконувати `pm clear`, uninstall, downgrade або destructive import на особистій установці без окремого дозволу й перевіреної recovery-копії.
- Macrobenchmark може reinstall/clear target. Спочатку перевір variant/applicationId і безпечність target; не вважай сам напис debug гарантією ізоляції.
- Не змінюй особисті health records чи системний час. Для time cases використовуй AppClock/fakes. Тимчасові display/font settings запиши й віднови.
- Відсутність пристрою не блокує доступні implementation/unit/compile steps. Але потрібний runtime gate познач `implemented_needs_device`, не `done`.
- Один Realme не доводить API36/16KB/older-HC compatibility. Для непокритих середовищ потрібні окремі результати або чесний pending.
- Screenshots/traces можуть містити особисті дані. Використовуй synthetic test profile; не коміть exports, ключі або raw health logs.

## Перевірки

- Kotlin: `scripts/check-quick.cmd` і вузькі relevant tests. Behavior/persistence: повний `scripts/check-tests.cmd` перед закриттям milestone.
- Targeted JVM приклад: `.\gradlew.bat :app:testDebugUnitTest --tests "повна.НазваTest"`; для тестів, перенесених до domain, використовуй відповідний domain task.
- Room: `scripts/check-room.cmd` плюс реальні instrumented Room/migration tests. Source guards і fake repositories не замінюють SQLite behavior.
- UI: `scripts/check-web-ui-guard.cmd`, connected tests/screenshot evidence там, де потрібні. Компіляція AndroidTest не дорівнює виконанню.
- Docs-only: `scripts/check-doc-only.cmd`, `git diff --check`, link/path checks; не запускай повний Gradle без причини.
- Fresh test run відрізняй від UP-TO-DATE. Зберігай full-suite counts/report evidence до filtered run, який може перезаписати XML.
- Lint/release checks запускай саме там, де вказано; build failure не маскуй suppress/baseline. Запиши непройдені checks, не вигадуй green CI.

## Завершення і передача контексту

Онови лише свій рядок PROGRESS. Створи/онови `docs/implementation/evidence/Pxx.md`, бажано до40 рядків:
- дата, branch/HEAD і опис dirty diff, scope та AUD/risk IDs;
- що відтворено/змінено або чому ризик спростовано;
- ключові files/symbols і змінені contracts для наступної задачі;
- точні commands, exit results/test counts, fresh/cached; посилання на локальні reports без sensitive data;
- device/variant/API/font/dataset, якщо застосовно;
- acceptance checklist, missing gates і next action.

Статуси: `pending`, `in_progress`, `implemented_needs_device`, `needs_owner`, `needs_beta_data`,
`blocked_dependency`, `done`, `not_reproduced_verified`.
`done` тільки після всіх applicable checks. `not_reproduced_verified` вимагає конкретного regression proof,
не просто відсутності помилки на одному запуску. Owner/beta gates не позначаються завершеними за відсутності даних.

За зміни контракту/flow/schema онови лише relevant GRAPH_REPORT/FEATURE_MAP sections.
Не переписуй історичний аудит так, ніби дефекту ніколи не було; посилайся на evidence його закриття.
Фінал задачі: коротко результат, checks, blockers і recommended next P-ID. Не виконуй його сам.
