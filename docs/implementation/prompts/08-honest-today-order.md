# P08. Чесні пояснення Today Order

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Для domain/mapper tests не потрібен; Android-пристрій/емулятор для final UI states.**
**Залежності: P07.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P08**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P08 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-06; ризик Stale Today після HC/check-in**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **07. Єдина семантика Today Order, quests і progress**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Неправильне Today рішення; Текст/CTA не відповідає рішенню** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapper.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapper.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusUiState.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusUiState.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Додай typed reason/source/freshness до decision/UI mapping без дублювання business rules у Compose. Unknown/default/stale score не показувати як виміряну готовність чи гарантію безпеки; відображати коротке чесне пояснення і корисну дію. Позбудься semantic string matching на кшталт contains("missed").
Збережи training/recovery/deload/no-excuse/rest, одну primary action, reward/consequence та fallback. Planned rest не пояснюй вигаданою втомою. Розрізняй день відпочинку, відсутність програми, loading та missing signals. Не переписуй thresholds без окремого доказу необхідності.
Приймання: mapper tests для кожного decision/source/freshness, empty fallback і доступності CTA. Оголоси явні reactive inputs, які підключить P09/P15. Nutrition без write UI не позиціонувати як готову персоналізацію; її disposition завершити в P09.

### Перевірка

Targeted decision/TodayOrderUiMapper tests; scripts/check-tests.cmd; scripts/check-quick.cmd; screenshots representative states.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapperTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapperTest.kt)

На завершення онови лише рядок P08 у PROGRESS і evidence `docs/implementation/evidence/P08.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
