# P09. Короткий check-in і актуальність сигналів

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Так, для save -> immediate Today refresh -> restart; Android-пристрій або емулятор.**
**Залежності: P08, R02.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P09**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P09 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-06; ризик Stale Today після HC/check-in; Nutrition partial feature**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **07. Єдина семантика Today Order, quests і progress**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Неправильне Today рішення; Profile/edit** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/model/Readiness.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/model/Readiness.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/ReadinessRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/ReadinessRepository.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/NutritionUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/NutritionUseCases.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/ReadinessRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/ReadinessRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/dao/ReadinessDao.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/ReadinessDao.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Підключи невеликий optional daily check-in для вже наявних readiness inputs. Domain owns validation/date/freshness, ViewModel owns events/state, Compose лише shared controls. Явно відрізняй unknown від введеного neutral. Дозволь edit сьогоднішнього check-in без duplicate rows.
Today Order, quests і відповідні summaries мають оновлюватись після save/refresh signals без restart, з контрольованими distinct inputs, без write-refresh loop.
Nutrition: якщо наявні поля реально відображаються або впливають на shipped decision, підключи мінімальний input до існуючого SaveNutritionEntryUseCase; не додавай calorie tracker. Інакше зафіксуй deferred/not-shipped і прибери неправдиві claims, не видаляючи дані/дії мовчки.
Приймання: skip, save, edit, stale entry, timezone boundary, persistence, concurrent refresh; check-in не змінює workout draft або reward. Screen geometry не перебудовуй заради цього.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Явно опиши read/invalidation dependencies: current date, schedule/config, equipment, setup constraints, execution facts, check-in/HC freshness. Перевір GetStatusScreenDataUseCase та displayedWorkoutFlow у WorkoutViewModel: snapshot .first()/suspend decision усередині іншого Flow не забезпечує повну реактивність.
- Після save кожного supported input Today і рекомендації відкритого workout узгоджені без restart. Не перестворюй completed quests і не переписуй введені sets при кожному emission.
- R27 підключає persisted adjustment до цього graph; P15 підключає реальне HC refresh. Тести мають змінювати кожне джерело окремо, включно з no-op, rapid updates і cancellation, та виключати write-refresh loop.

### Перевірка

Readiness/nutrition/decision targeted tests, VM reactivity tests, scripts/check-tests.cmd; connected save/refresh/relaunch journey.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetNutritionFloorStatusUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetNutritionFloorStatusUseCaseTest.kt)

На завершення онови лише рядок P09 у PROGRESS і evidence `docs/implementation/evidence/P09.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
