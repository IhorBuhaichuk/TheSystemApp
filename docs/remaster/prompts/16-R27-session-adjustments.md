# R27. Зміни тренування лише на сьогодні

**Модель: GPT-6.1 Sol; Reasoning: High.** Власник задає модель у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Так, для process-death/restore і переходів між вкладками; host-частина можлива без нього.**
**Залежності: P02, P03, P07, P08, P09, R05.**

## Прогрес

- [ ] Domain contract і consumer map узгоджені з R05_DATA_FLOW.
- [ ] Persistence, effective-plan calculation та ViewModel events реалізовані.
- [ ] Unit/Room/backup regression перевірки пройдені.
- [ ] Device lifecycle/restore gates пройдені з evidence.
- [ ] GRAPH_REPORT, FEATURE_MAP, PROGRESS, QUEUE та evidence оновлені.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки R27.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій
рядок [PROGRESS](../PROGRESS.md) та prerequisites у [QUEUE](../QUEUE.md).
Read [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): A/EW/D/PL, DF03-DF04 та інваріанти.
Не перечитуй весь audit чи всі промпти. Це реалізація вже схваленого сценарію,
не новий планувальник, не AI-generator, не повний UI-remaster (UI належить R09).

### Початкові файли

- `domain/src/main/java/com/ihor/thesystem/domain/model/WorkoutDraft.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/model/Onboarding.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/WorkoutDraftUseCases.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCases.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutUiState.kt`
- `app/src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutDraftRepositoryImpl.kt`
- `app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupImportPolicy.kt`
- `app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt`
- `app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabaseMigrations.kt`

Далі йди лише за repository contracts/DAO/Hilt і новими P07/P09 symbols через FEATURE_MAP.
Нові domain model/use cases/repository і Room entity/DAO додавай в існуючих шарах.
Не створюй додатковий Gradle module або дубльовану систему програм.

### Реалізація

1. Введи явний session adjustment із датою, identity/revision базового плану,
   time budget, equipment overrides і стабільними exercise substitutions. Typed unknown
   для capacity; жодних вигаданих24kg і жодного висновку про силу з максимальної ваги інвентарю.
2. Розділи edit draft та applied override: Apply validates/persists, Cancel/Back не
   пишуть дані. Не викликай глобальні equipment/config setters для today-only зміни.
3. Domain обчислює один effective plan для Status, preview і workout. Правила скорочення
   сесії та substitution перевикористовують підтримані exercise metadata і P07 policy.
   Менший time budget не означає довільне пропорційне зменшення кожного параметра;
   непідтриману зміну чесно відхиляти/пропонувати доступну альтернативу.
4. При активному draft не втрачати введені/виконані sets. Несумісна заміна потребує
   explicit conflict resolution. Plan identity і draft мають відновлюватися узгоджено
   після process death, без transient показу базового workout замість adjusted.
5. Новий день не успадковує вчорашню зміну; незавершений попередній draft не видаляти
   мовчки. Зміна базового циклу/restore викликає явне revalidation, не застосування
   override до іншого template. P11 надалі розширює clock/timezone boundary tests.
6. Додай explicit invalidation до P09 flow: apply/revert оновлює всіх current-day
   consumers без restart, не запускає write-refresh loop, не міняє завершені сесії.
7. Не змінюй completionId/atomic commit P03. Записаний session snapshot фіксує фактичний
   effective plan, щоб P10/P12 визначали partial/full від правильного denominator.
8. Якщо Room змінюється: версія від ПОТОЧНОЇ + migration/schema/tests одним diff;
   backup/export/import policy для нових полів у цьому ж кроці, defaults для старих
   backup без стирання даних. Не відсилати користувача повторно виконувати P04.

### Перевірка

- Unit: Apply/Cancel, unknown capacity, invalid replacement, stable exercise IDs,
  shorter session policy, base-plan unchanged, draft conflict, next-day expiry.
- Repository/Room: atomic apply + draft identity, rollback on error, process restart,
  migration і backup round trip без duplicates/orphan rows.
- ViewModel: rapid edits/save, cancel during pending action, restored adjustment,
  Status/workout same effective plan, no stale snapshot after update.
- `scripts/check-tests.cmd`, `scripts/check-room.cmd`, `scripts/check-quick.cmd`.
- Device: apply -> other tabs -> kill/relaunch -> resume -> finish; next-day base
  unchanged. Без пристрою не ставити runtime checkbox і done.

Онови лише R27 у PROGRESS, evidence `docs/remaster/evidence/R27.md`, affected map
sections і QUEUE. R09 отримує готовий contract, не повторно створює persistence.
Не commit/push/merge, не запускати наступний prompt автоматично.
