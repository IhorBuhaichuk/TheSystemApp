# P20. Вимірювана швидкість запуску й плавність

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так, той самий фізичний Realme C55 бажаний для before/after. Benchmark тільки в безпечній test installation.**
**Залежності: P18, P19.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P20**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P20 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Performance і maintainability**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Performance pass, M-L**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Seed/startup; Statistics/chart; Profile/edit**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [docs/PERFORMANCE_OPTIMIZATION_STATUS.md](../../../docs/PERFORMANCE_OPTIMIZATION_STATUS.md)
- [app/src/main/java/com/ihor/thesystem/TheSystemApp.kt](../../../app/src/main/java/com/ihor/thesystem/TheSystemApp.kt)
- [app/src/main/java/com/ihor/thesystem/core/di/DatabaseModule.kt](../../../app/src/main/java/com/ihor/thesystem/core/di/DatabaseModule.kt)
- [app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt)
- [app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt](../../../app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt)
- [baselineprofile/src/main/java/com/ihor/thesystem/baselineprofile/StartupBenchmarks.kt](../../../baselineprofile/src/main/java/com/ihor/thesystem/baselineprofile/StartupBenchmarks.kt)
- [baselineprofile/build.gradle.kts](../../../baselineprofile/build.gradle.kts)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Спочатку виміряй current baseline, потім редагуй тільки trace-proven hotspots. Розділи first install/cold/warm і time-to-usable-Today, frame metrics для Statistics/Profile/logging. Не використовуй липневі numbers як свіжий before; зафіксуй device/API/refresh rate/thermal/build/minify/dataset/compilation mode.
Використай існуючий :baselineprofile, не створи дубль. Мінімум10 валідних startup samples; для frames повторювані journeys, порівнюй однакові метрики. Перевір main-thread, allocation/measure/draw, memory, зайві wakeups/IO; за відсутності battery evidence не обіцяй energy improvement.
Оптимізації не гублять дані, не затримують correctness Today, не ховають контент і не додають splash delay. Budgets у roadmap є цілями, не guaranteed thresholds. Доведи material gain або відхили невдалий експеримент; не переписуй результати заднім числом.
Benchmark може reinstall/wipe пакет: без дозволу та перевіреної копії не запускати на особистій DB. Створи isolated target або використовуй окремий test device.

### Перевірка

Before/after tables + traces/report paths; startup/seed regressions; check-tests; selected macrobenchmarks; онови performance checkpoint із limitations.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/DatabasePopulatorCoreMetadataTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/local/room/database/DatabasePopulatorCoreMetadataTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/feature/profile/ui/ProfilePerformanceGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/profile/ui/ProfilePerformanceGuardTest.kt)

На завершення онови лише рядок P20 у PROGRESS і evidence `docs/implementation/evidence/P20.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
