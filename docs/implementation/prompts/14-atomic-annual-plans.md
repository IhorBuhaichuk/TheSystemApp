# P14. Атомарне збереження річних планів

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Android-пристрій/емулятор для failure-injection Room test.**
**Залежності: P12.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P14**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P14 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Ризик Annual plan save partial; maintainability encoded plan note**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Вибірковий architectural cleanup, M-L**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Annual plan** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/SaveAnnualProgressionPlanUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/SaveAnnualProgressionPlanUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateAnnualProgressionPlanUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateAnnualProgressionPlanUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/util/AnnualProgressionPlanNoteParser.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/util/AnnualProgressionPlanNoteParser.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/ProgressionMatrixRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/ProgressionMatrixRepository.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Відтвори failure під час збереження другої вправи multi-exercise plan. Забезпеч all-or-nothing plan persistence через існуючу transaction boundary, не Android dependency у domain. Повтор save не дублює план і не стирає unrelated history.
Перевір encoded plan note parsing: malformed/unknown version, locale-independent numeric/date representation, старі developer notes. Не змінюй формат і schema лише заради елегантності: якщо чинний формат стабільний, додай tests/documented contract; якщо доведено defect, зроби bounded compatible fix.
Приймання: rollback при failure, повторне збереження, edit/manual plan, invalid numeric targets, cancel і restore round-trip. Не називай local plan generator віддаленим AI.

### Перевірка

Targeted annual plan/parser tests + Room rollback; scripts/check-tests.cmd; scripts/check-room.cmd за зміни persistence.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModelTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModelTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCaseTest.kt)

На завершення онови лише рядок P14 у PROGRESS і evidence `docs/implementation/evidence/P14.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
