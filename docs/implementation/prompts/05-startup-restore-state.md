# P05. Узгоджений запуск, seed і відновлений профіль

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: На фініші Android-пристрій/емулятор для first launch та process interruption.**
**Залежності: P04.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P05**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P05 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-04 prefs/DB; ризик Seed/onboarding race**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **03. Єдиний контракт backup/restore і setup state; 06. Onboarding створює першу корисну дію**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Не той перший route; Seed/startup** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/OnboardingRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/OnboardingRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/core/di/DatabaseModule.kt](../../../app/src/main/java/com/ihor/thesystem/core/di/DatabaseModule.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/DatabaseReadinessRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/DatabaseReadinessRepositoryImpl.kt)
- [app/src/main/res/xml/full_backup_content.xml](../../../app/src/main/res/xml/full_backup_content.xml)
- [app/src/main/res/xml/data_extraction_rules.xml](../../../app/src/main/res/xml/data_extraction_rules.xml)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Спочатку тестом перевір race slow/failing seed + fast onboarding completion. Route визначай за узгодженим setup/readiness state, не тільки preference boolean. Виріши DB/prefs restoration contract: prefs-only restore не обходить setup; повний JSON restore не перезапускає setup із втратою XP/profile. Android cloud/device-transfer policy узгодь з цим контрактом і privacy.
Збережи versioned/idempotent metadata seeding: незмінні rows не переписуються, user data не скидаються, повтор seed не дублює дані. Setup retry після часткового failure має бути безпечним. Не переміщуй всю ініціалізацію на Main і не приховуй race довільним delay.
Приймання: clean launch, completed launch, prefs-only restore, повний restore, seed failure/retry, process kill між writes. Немає route flash, default-player overwrite або повторного скидання прогресу. Starter program content реалізовується окремо в P07.

### Перевірка

Onboarding/seed unit tests + integration failure-injection; scripts/check-tests.cmd; scripts/check-room.cmd; device route smoke.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/OnboardingUseCasesTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/OnboardingUseCasesTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/DatabasePopulatorCoreMetadataTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/DatabasePopulatorCoreMetadataTest.kt)

На завершення онови лише рядок P05 у PROGRESS і evidence `docs/implementation/evidence/P05.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
