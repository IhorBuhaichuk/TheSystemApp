# P21. Зменшення складності без великого переписування

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Не потрібен для основного refactor; Android-пристрій/емулятор для regression smoke після нього.**
**Залежності: P18, P20.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P21**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P21 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Performance і maintainability; parallel session tables risk**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Вибірковий architectural cleanup, M-L**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Втрата/дублі workout sets; Backup; Build/release/lint**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/WorkoutUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/WorkoutUseCases.kt)
- [app/src/main/java/com/ihor/thesystem/core/di/RepositoryModule.kt](../../../app/src/main/java/com/ihor/thesystem/core/di/RepositoryModule.kt)
- [domain/build.gradle.kts](../../../domain/build.gradle.kts)
- [app/build.gradle.kts](../../../app/build.gradle.kts)
- [app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Після green regressions відокрем backup/settings coordination від WorkoutViewModel, якщо після попередніх tasks це ще спільний великий owner. Збережи lifecycle/event/state contracts, не додавай нові abstractions заради назв. Не змінюй поведінку або дизайн.
Перевір value перенесення pure domain tests з app до domain; перенеси відповідний вузький набір, збережи coverage й CI execution, Android/source guards залиш у app. Виміряй feedback task time, не вважай переміщення автоматично швидшим.
Простеж усі read/write/backup consumers паралельних session tables: не видаляй таблиці тільки через схожі назви. Якщо вони потрібні, документуй ownership; якщо доведено зайві, запропонуй окрему explicit migration, не виконуй destructive removal у цьому task.
Приймання: менші ownership boundaries, ті самі screens/actions/logging, domain без Android, тести не видалені й не ослаблені. Файли, що вже перейменовані попередніми tasks, знайди через map/symbol.

### Перевірка

Targeted/full tests після move; guards; compile; relevant UI smoke; GRAPH_REPORT/FEATURE_MAP update тільки за реальної зміни ownership.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/quality/ViewModelStructureGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/ViewModelStructureGuardTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/quality/DomainModuleBoundaryGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/DomainModuleBoundaryGuardTest.kt)

На завершення онови лише рядок P21 у PROGRESS і evidence `docs/implementation/evidence/P21.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
