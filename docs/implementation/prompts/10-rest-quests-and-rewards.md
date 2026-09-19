# P10. Узгоджені REST, квести та нагороди

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Для domain tests не потрібен; Android-пристрій/емулятор для Room cross-flow підтвердження.**
**Залежності: P03, P08, P09.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P10**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P10 та його залежності; це заяви, не заміна evidence.
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

### Перевірка

GenerateDailyQuests/CompleteQuest/decision tests; scripts/check-tests.cmd; transaction-level cross-flow test на Room.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/CompleteQuestUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/model/QuestCompletionPolicyTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/model/QuestCompletionPolicyTest.kt)

На завершення онови лише рядок P10 у PROGRESS і evidence `docs/implementation/evidence/P10.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
