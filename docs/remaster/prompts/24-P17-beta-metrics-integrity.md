# P17. Коректні локальні beta metrics

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Для aggregation tests не потрібен; Android-пристрій/емулятор для optional summary export UI.**
**Залежності: P03, P07, P10, P11, P12.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P17**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P17 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-12**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Beta як навчання**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Beta metrics**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/BetaMetricsAggregator.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/BetaMetricsAggregator.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GetBetaMetricsUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetBetaMetricsUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/model/BetaMetrics.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/model/BetaMetrics.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/BetaMetricsRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/BetaMetricsRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Визнач immutable event/snapshot semantics для onboarding completed, first workout, opened day, viewed decision і completed planned action. Historical planned/missed не перераховувати через поточний змінений schedule; manual log не вважати автоматично виконанням конкретного planned order. Зберегти local-first без Firebase/Amplitude/Segment.
Додай timestamps/version/stable IDs і потрібні privacy-minimal snapshots. Розрізняй open/refresh від meaningful action; один refresh не дає багато events. Додай добровільний diagnostic summary export із preview, лише aggregates, без імені, sleep/readiness values, raw workouts, chat або стабільного cross-app identifier.
Приймання: edit schedule не переписує минуле; repeated events і restore не дублюють metrics; timezone та missing history дають чесний unknown/partial; core app не падає через metrics. Додай deletion/reset policy для локальних diagnostics, не чіпаючи workouts без підтвердження.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Partial/full факти та eligible planned-day denominator узгоджені з P10/P12 і effective-plan snapshot R27; будь-який logged set не прирівнювати до completed planned day.
- Repeated finish/retry, history edit, restore, today-only adjustment не дублюють activation/completion events. Порівняй local metrics з Calendar/Statistics на одному fixture dataset.

### Перевірка

BetaMetricsAggregator/event repository tests; scripts/check-tests.cmd; Room migration/backup checks якщо storage зміниться; summary privacy test.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BetaMetricsAggregatorTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/BetaMetricsAggregatorTest.kt)

На завершення онови лише рядок P17 у PROGRESS і evidence `docs/implementation/evidence/P17.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
