# P12. Правдивий прогрес і повнота історії

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Android-пристрій/емулятор для SQL integration; для aggregation unit tests не потрібен.**
**Залежності: P01, P03, P10.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P12**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P12 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-10; ризик Ліміти history**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **07. Єдина семантика Today Order, quests і progress**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Statistics/chart; Annual plan** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateRecommendedSetUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateRecommendedSetUseCase.kt)
- [app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Установи й протестуй completed-work semantics для charts, annual history, recommendations і progress proof. Збережи incomplete inputs як incomplete, але не зараховуй planned100kg як completed PR, коли виконано40kg. Розрізняй weighted/bodyweight/time/distance, не підмінюй всі метрики тоннажем.
Перевір LIMIT100/getAll та bounded LIMIT200: контракти мають явно обіцяти window/scope. Для all-time/щільної історії використовуй відповідний aggregate/query або pagination; не прибирай limits шляхом повернення безмежних join у головний UI flow. Baseline до annual plan зберегти.
Приймання: SQL fixtures completed/incomplete, manual edits, multiple sessions/day, dense >200 sessions/window, long history, нульовий workout. Реальна SQL перевірка, не лише пошук MAX у source.

### Перевірка

Targeted query/progress/recommendation tests; scripts/check-tests.cmd; scripts/check-room.cmd; Room instrumentation.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/WorkoutAnalyticsQueryGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/WorkoutAnalyticsQueryGuardTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BuildProgressProofsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BuildProgressProofsUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt)

На завершення онови лише рядок P12 у PROGRESS і evidence `docs/implementation/evidence/P12.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
