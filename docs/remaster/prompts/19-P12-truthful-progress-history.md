# P12. Правдивий прогрес і повнота історії

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Android-пристрій/емулятор для SQL integration; для aggregation unit tests не потрібен.**
**Залежності: P01, P03, P10.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P12**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P12 та його залежності; це заяви, не заміна evidence.
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

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Partial/full/planned/off/unknown проєкції беруть outcome/snapshot P10/R27, а не вгадують виконання з наявності будь-якого session row.
- Report details читає точний committed sessionId; history edit оновлює ті самі факти, які бачать Calendar і Statistics, без повторного reward.
- Regression: partial session і shortened effective plan дають однакові counts у report/history/week summary; D27 progress переноситься в Statistics/R12 без видалення даних.

### Перевірка

Targeted query/progress/recommendation tests; scripts/check-tests.cmd; scripts/check-room.cmd; Room instrumentation.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/WorkoutAnalyticsQueryGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/WorkoutAnalyticsQueryGuardTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BuildProgressProofsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BuildProgressProofsUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt)

На завершення онови лише рядок P12 у PROGRESS і evidence `docs/implementation/evidence/P12.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
