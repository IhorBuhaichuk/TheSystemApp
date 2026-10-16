# P10. Узгоджені REST, квести та нагороди

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Для domain tests не потрібен; Android-пристрій/емулятор для Room cross-flow підтвердження.**
**Залежності: P03, P08, P09, R01, R27.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P10**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P10 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-07; completed quest regeneration risk**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **07. Єдина семантика Today Order, quests і progress**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **REST, але є workout quest; XP/ранг/серія** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/AdjustWorkoutRecommendationUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/AdjustWorkoutRecommendationUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/CompleteQuestUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CompleteQuestUseCase.kt)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/QuestRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/QuestRepositoryImpl.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Відтвори calendar OFF + workout schedule -> phantom zero-target MAIN. Зроби decision semantics узгодженими для task generation, CTA, execution й reward. REST не створює силовий MAIN із нульовими targets; recovery/no-excuse залишаються корисними й логуються у правильному tracking mode.
Окремим regression перевір completed quest regeneration після зміни matrix/recommendations/config. Не видаляй історично завершений quest і не створюй новий reward за ту саму виконану дію. Збережи можливість реального наступного workout, side/promotion quests і чинні logging actions.
Приймання: всі day types, completed main + refresh, зміна readiness після завершення, repeated sync, зміна equipment. Rest/recovery не повинні помилково карати streak. Не вимикай усе XP як обхід проблеми.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- R27 уже забезпечує effective plan. Визнач partial/full completion від його зафіксованого snapshot, а не поточного зміненого schedule.
- Перевір completeWorkoutQuestIfPossible: зараз будь-який completed set кожної вправи може задовольнити всі quest tasks. Це не доказ повного виконання всіх planned sets.
- Зафіксуй одне domain outcome/reward правило для partial, full, zero, rest/recovery. UI не має вираховувати його самостійно. Partial facts зберігаються чесно; не віднімати XP мовчки й не нараховувати full reward без визначеної eligibility.
- Тести:1 з8 sets; по1 set кожної вправи з рештою incomplete; adjusted shorter plan; повторне завершення/refresh. P12/P17 використовують те саме outcome. Confirmation cancel нічого не комітить; форму confirmation реалізує R09.

### Перевірка

GenerateDailyQuests/CompleteQuest/decision tests; scripts/check-tests.cmd; transaction-level cross-flow test на Room.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/model/QuestCompletionPolicyTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/model/QuestCompletionPolicyTest.kt)

На завершення онови лише рядок P10 у PROGRESS і evidence `docs/implementation/evidence/P10.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
