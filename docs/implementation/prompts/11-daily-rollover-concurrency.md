# P11. Опівніч, часові пояси та паралельні оновлення

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Для clock/concurrency unit tests не потрібен; Android-пристрій/емулятор для worker/Room integration.**
**Залежності: P05, P10.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P11**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P11 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Ризик Worker/foreground double rollover**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **07. Єдина семантика Today Order, quests і progress**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Неправильний день/опівніч** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/SyncTodayStateUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/SyncTodayStateUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeDayUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeDayUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/ResolveTrainingCycleDayUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/ResolveTrainingCycleDayUseCase.kt)
- [app/src/main/java/com/ihor/thesystem/core/worker/DailyResetWorker.kt](../../../app/src/main/java/com/ihor/thesystem/core/worker/DailyResetWorker.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/SystemConfigRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/SystemConfigRepositoryImpl.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/util/AppClock.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/util/AppClock.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Почни з deterministic race test двох invocations на однаковому clock/DB; ризик ще не був runtime-доведений. Якщо тест підтвердить дефект, забезпеч atomic check-and-advance з повторною перевіркою persisted state у потрібній transaction. Coroutine mutex сам по собі не замінює persisted invariant.
Worker і foreground мусять наздоганяти пропущені дні без подвійних rewards/penalties/advance; не покладайся на виконання рівно опівночі. Cancellation/retry не знищує draft і не архівує дані двічі.
Приймання: same-day concurrent calls, multi-day gap, DST forward/back, timezone change, process restart, failure/retry. Якщо ризик не підтвердився, залиш доказовий regression test і мінімальний diff без непотрібного refactor.

### Перевірка

Clock/concurrency targeted tests; scripts/check-tests.cmd; actual worker/Room integration test. Не змінюй системний час особистого телефона без дозволу; використовуй injected clock.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/SyncTodayStateUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/SyncTodayStateUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/FinalizeDayUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/FinalizeDayUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ResolveTrainingCycleDayUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ResolveTrainingCycleDayUseCaseTest.kt)

На завершення онови лише рядок P11 у PROGRESS і evidence `docs/implementation/evidence/P11.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
