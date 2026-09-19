# P01. Безпечне редагування історії

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: На фініші потрібен Android-пристрій або емулятор для справжніх Room tests; писати код можна без телефона.**
**Залежності: немає.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P01**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P01 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-01**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **01. Безпечне редагування workout history**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Редагування однієї вправи** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/StatisticsUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/StatisticsUseCases.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Виправ обидва шляхи редагування сетів. Перед fix додай regression: session з A/B/C, редагування B не видаляє A/C. Передавай explicit sessionId + exerciseId замість довільного першого log дня. Parent row оновлюй без INSERT OR REPLACE; змінюй лише сети цільової вправи в transaction. Зберігай session identity, quest linkage, дату й cycleDay; aggregate tonnage перераховуй за всією виконаною роботою session. Не зламай bodyweight/time/distance та наявні UI handlers.
Приймання: multi-exercise session, два workouts одного дня, повтор edit, incomplete sets і transaction rollback мають визначений результат без втрат/дублів. Додай реальний Room integration test, а не лише fake/regex. Для вже некоректних записів не вигадуй відсутні сети; не роби silent destructive repair.

### Перевірка

Targeted LogWorkoutSetsUseCaseTest + новий Room test; scripts/check-tests.cmd; scripts/check-room.cmd. Якщо зміниться schema, виконати migration tests.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCaseTest.kt)

На завершення онови лише рядок P01 у PROGRESS і evidence `docs/implementation/evidence/P01.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
