# P02. Збереження незавершеного тренування

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так, для resume/process-death перевірки; можна емулятор. Лише ізольована тестова установка.**
**Залежності: P01.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P02**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P02 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-02**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **02. Workout draft, що переживає переривання**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Втрата/дублі workout sets** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt](../../../app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/RefreshOnResume.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/RefreshOnResume.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/WorkoutUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/WorkoutUseCases.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabaseMigrations.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabaseMigrations.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Відокрем refresh поточних даних від скидання _userEdits. Додай durable draft із стабільним ID, прив'язкою до дня/плану, inputs і completed flags; domain owns contract, data owns persistence. Збережи контракт для наступного P03. Не записуй draft як завершений workout і не нараховуй XP. SavedStateHandle використовуй тільки для відповідного UI state, не як єдиний захист тренування.
Визнач resume/discard/finish і midnight policy. Не втрачати введення при ON_RESUME, recomposition, зміні конфігурації чи перестворенні процесу. Оновлення plan не має мовчки перезаписувати draft. Не тримай частково збережені edits без відображення помилки.
Приймання: відновлюються значення та flags після background/rotate/process kill; explicit discard очищає тільки draft; після finish він більше не пропонується. Додай unit, persistence і lifecycle regression tests. Нові файли створюй лише у відповідних domain/data/feature ownership boundaries.

### Перевірка

scripts/check-tests.cmd; scripts/check-room.cmd; relevant connected tests, migration/schema checks та manual resume/process-death evidence.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModelMappersTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModelMappersTest.kt)
- [app/src/androidTest/java/com/ihor/thesystem/data/local/room/database/AppDatabaseMigrationTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/data/local/room/database/AppDatabaseMigrationTest.kt)

На завершення онови лише рядок P02 у PROGRESS і evidence `docs/implementation/evidence/P02.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
