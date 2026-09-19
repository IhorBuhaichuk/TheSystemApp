# P13. Збереження вибору вправи при поверненні

**Модель: GPT-5.6 Luna; Reasoning: Medium.**
**Телефон: Так, Android-пристрій/емулятор для навігаційного regression.**
**Залежності: P07.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P13**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P13 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Ризик ExercisePicker cancellation**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **09. Справжні integration journeys замість лише test-shell**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Вправи/обладнання** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt)
- [app/src/main/java/com/ihor/thesystem/core/navigation/Routes.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/Routes.kt)
- [app/src/main/java/com/ihor/thesystem/feature/exercise_search/viewmodel/ExerciseSearchViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/exercise_search/viewmodel/ExerciseSearchViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/AnnualProgressionPlanViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Додай тест із delayed repository write: select exercise -> popBackStack. Встанови фактичний lifetime owner ViewModel для cycle/annual picker. Якщо запис губиться, зв'яжи navigation result/completion з коректним owner, не запускай detached GlobalScope coroutine.
Приймання: вибір зберігається один раз, швидкий double tap не дублює вправу, back/cancel не додає нічого, write failure показується без raw exception; обидва source routes працюють. Не переписуй весь NavGraph.
Якщо cancellation risk не підтвердиться, збережи тест і поясни, який owner забезпечує коректність.

### Перевірка

ExerciseSearch VM tests + production-route connected test; scripts/check-quick.cmd; relevant tests.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/feature/exercise_search/viewmodel/ExerciseSearchViewModelTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/exercise_search/viewmodel/ExerciseSearchViewModelTest.kt)

На завершення онови лише рядок P13 у PROGRESS і evidence `docs/implementation/evidence/P13.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
