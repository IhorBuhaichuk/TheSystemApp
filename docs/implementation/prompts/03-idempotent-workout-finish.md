# P03. Одноразове завершення й надійний звіт

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Android-пристрій/емулятор для Room і interruption tests; для unit-частини не потрібен.**
**Залежності: P02.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P03**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P03 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-03**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **04. Ідемпотентне завершення й незалежний report**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Втрата/дублі workout sets** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/CompleteQuestUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CompleteQuestUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/WorkoutAnalyticsRepository.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/entity/WorkoutSessionLogEntity.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/entity/WorkoutSessionLogEntity.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Використай draft/session identity з P02. Забезпеч idempotency не лише кнопкою isFinishing, а storage contract/uniqueness: той самий finish operation створює один session і одну допустиму reward. Розділи canonical completion та optional report/AI enrichment. Error після commit не має пропонувати повторне створення workout; віддай локальний report зі збереженим session ID. AI directives лишаються за ValidateDirectivesUseCase, network не тримати всередині DB transaction.
Приймання: double event, concurrent finish, retry, crash після commit і report failure не дублюють logs/XP; нове справжнє тренування дозволене; cancellation не маскується generic failure. Після success draft закритий атомарно або відновлюється однозначно. Збережи всі чинні logging/action paths і incomplete semantics.

### Перевірка

Targeted finalize/quest/draft tests + реальна DB duplicate/retry перевірка; scripts/check-tests.cmd; scripts/check-room.cmd.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCaseFallbackTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCaseFallbackTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt)

На завершення онови лише рядок P03 у PROGRESS і evidence `docs/implementation/evidence/P03.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
