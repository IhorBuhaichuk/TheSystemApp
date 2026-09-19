# P09. Короткий check-in і актуальність сигналів

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Так, для save -> immediate Today refresh -> restart; Android-пристрій або емулятор.**
**Залежності: P08.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P09**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P09 та його залежності; це заяви, не заміна evidence.
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

### Перевірка

Readiness/nutrition/decision targeted tests, VM reactivity tests, scripts/check-tests.cmd; connected save/refresh/relaunch journey.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetNutritionFloorStatusUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetNutritionFloorStatusUseCaseTest.kt)

На завершення онови лише рядок P09 у PROGRESS і evidence `docs/implementation/evidence/P09.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
