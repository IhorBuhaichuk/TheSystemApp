# P15. Завершена інтеграція Health Connect

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Так, бажано фізичний телефон з Health Connect; старі/new API permission paths також перевірити на відповідному емуляторі.**
**Залежності: P08, P09.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P15**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P15 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-09; ризик HC sleep double count/window**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **08. Health Connect та privacy як завершена інтеграція**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Health Connect**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/AndroidManifest.xml](../../../app/src/main/AndroidManifest.xml)
- [app/src/main/java/com/ihor/thesystem/health/HealthConnectPermissions.kt](../../../app/src/main/java/com/ihor/thesystem/health/HealthConnectPermissions.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/HealthConnectSignalsRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/HealthConnectSignalsRepositoryImpl.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/HealthSignalsRepository.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/HealthSignalsRepository.kt)
- [docs/HEALTH_CONNECT_RATIONALE.md](../../HEALTH_CONNECT_RATIONALE.md)
- [PRIVACY_POLICY.md](../../../PRIVACY_POLICY.md)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Звір актуальний офіційний Android HC setup із installed SDK. Додай native privacy/rationale entry та Android14+ permission usage alias, потрібну package visibility для supported older API. Повідомлення й docs мають відповідати actual shipped READ_SLEEP; інших permissions не додавати.
Протестуй sleep aggregation: overlapping sources, overnight, timezone/DST, pagination, missing/stale data. Обери API/aggregation semantics за офіційною документацією, не сумуй overlap наосліп. Grant/deny/revoke/provider missing не блокують core loop; дані оновлюються без restart через контракт P09.
Приймання: privacy screen відкривається із системного HC permission UI, unavailable корисний, raw records не логуються й не надсилаються AI без явного дозволеного контракту. Не заповнюй телефон тестовими health records у основному профілі. Public policy URL/contact отримай від власника або лиш needs_owner, не вигадуй.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Сон optional, має source/freshness і unknown state. Setup/перший workout не вимагають Health Connect або guessed sleep.
- Refresh/revoke/empty/permission denied оновлюють P09 decision consumers без restart і не перетворюють відсутні дані на погану готовність. Показувати тільки реально використані signals.

### Перевірка

Repository sleep/time/permission tests; scripts/check-tests.cmd; merged manifests; real system-intent/permission lifecycle evidence.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCaseTest.kt)

На завершення онови лише рядок P15 у PROGRESS і evidence `docs/implementation/evidence/P15.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
