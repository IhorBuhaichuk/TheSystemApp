# P04. Безпечний backup і відновлення

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так, Android-пристрій або емулятор для Room round-trip і SAF; не імпортувати fixtures у твою основну DB.**
**Залежності: P01, P02, P03.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P04**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P04 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-04, частина import/export**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **03. Єдиний контракт backup/restore і setup state**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Backup** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/BackupUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/BackupUseCases.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/model/BackupPayload.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/model/BackupPayload.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/BackupRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/BackupRepository.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupImportPolicy.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupImportPolicy.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Реалізуй описаний у roadmap явний full-snapshot restore з preview/confirmation замість неозначеного REPLACE-merge. Якщо поточний UI явно обіцяє merge, спочатку узгодь зміну контракту з користувачем, не підмінюй її мовчки. Export має бути consistent snapshot; lastExported оновлюй тільки після успішного destination write. Parsing/validation не виконувати на Main.
Валідуй format/schema compatibility, повноту й унікальність таблиць, required fields, references, finite numbers/domain bounds, розмір файла. Partial або unknown incompatible payload відхиляй без mutations. У snapshot врахуй durable drafts; для старих developer exports передбач явну підтримку або зрозуміле відхилення, не автоматичний wipe.
Приймання: export/restore round-trip, повтор restore, cancel, malformed/oversized/subset, SQL failure, interrupted SAF write, parent-child CASCADE. Відновлення з помилкою не змінює DB. Визнач recovery/setup reconciliation contract для P05; private backup content не потрапляє в logs.

### Перевірка

Backup unit tests + нові real Room/SAF integration tests; scripts/check-tests.cmd; scripts/check-room.cmd. Узгодити backup paragraphs PRIVACY_POLICY.md з реальною поведінкою.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BackupUseCasesTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BackupUseCasesTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/data/repository_impl/BackupImportPolicyTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/repository_impl/BackupImportPolicyTest.kt)

На завершення онови лише рядок P04 у PROGRESS і evidence `docs/implementation/evidence/P04.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
