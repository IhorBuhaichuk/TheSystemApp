# P07. Готовий перший план після onboarding

**Модель: GPT-6.1 Sol; Reasoning: High.**
**Телефон: Так, для first-install journey; Android-пристрій або емулятор, ізольована установка.**
**Залежності: P05, R02, R05.**

## Прогрес

- [ ] Завдання реалізовано або ризик спростовано відтворюваним тестом.
- [ ] Host-перевірки виконані, точні результати записані.
- [ ] Потрібні device/owner/beta gates виконані або явно не застосовуються.
- [ ] Evidence і PROGRESS оновлені; крок завершено.

Позначки відображають evidence, не новий запуск tests сьогодні. При виконанні оновлюй цей checklist
разом із рядком [PROGRESS](../../implementation/PROGRESS.md); не став галочки лише за написаний код.
Для ремастеру перевір додаткові R-gates у [QUEUE](../QUEUE.md) і [model policy](../MODEL_ROUTING.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P07**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../../implementation/EXECUTION_RULES.md).
У [PROGRESS](../../implementation/PROGRESS.md) прочитай лише P07 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-05**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **06. Onboarding створює першу корисну дію**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Після setup немає тренування** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/model/Onboarding.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/model/Onboarding.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/repository/Repositories.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/repository/Repositories.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCases.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCases.kt)
- [app/src/main/java/com/ihor/thesystem/feature/onboarding/viewmodel/OnboardingViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/onboarding/viewmodel/OnboardingViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/onboarding/ui/OnboardingScreen.kt](../../../app/src/main/java/com/ihor/thesystem/feature/onboarding/ui/OnboardingScreen.kt)
- [app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppEntryViewModel.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

На узгодженому setup contract P05 створи невеликий deterministic starter cycle: schedule, exercise assignments, rest days, equipment matching. Перевикористай каталог/існуючі тренувальні правила. Не будуй AI program generator. Зберігай goal/experience явно, якщо подальші рішення їх потребують; не виводь experience назад із XP.
Completion має означати готовність профілю/config/program; retry не створює duplicates і не скидає прогрес. Після завершення користувач потрапляє на Status із доречною actionable Today Order, далі може виконати й редагувати workout.
Приймання: beginner/returning, наявне/обмежене обладнання, відсутня substitution, offline, repeated launch, failure/retry. Немає необхідності спочатку вручну конструювати програму.
Стартовий контент не називай медично/професійно валідованим. У handoff зафіксуй конкретний пакет шаблонів для людського review в P23/P25.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Реалізуй прийняті G/H/S/E/B/P/SB: ціль, недавній досвід/повернення, weekday choices і time budget, location/equipment/typed unknown capacity, suitability, preview. Ім'я optional/пізніше у Profile; не gate першого плану.
- Weekly frequency = кількість вибраних weekday. Старий cycleDays (довжина ротації) не використовувати як кількість тренувань на тиждень. Зберегти явний calendar anchor.
- Досвід впливає на starter policy, не визначається RPG level. Не вигадувати24kg і не вважати capacity оцінкою сили. Unsupported suitability не веде до автоматичного стандартного плану.
- Back/редагування зберігає draft, upstream changes invalidates preview. Manual path видимий, але incomplete manual setup не означає готовий program.
- Save preview пише узгоджені profile/config/schedule/template/completion; failure/retry не лишає completed flag без програми. Persist inputs потрібні для повторного редагування; backup/schema/readers/tests оновити тут, не повторно P04.
- Тести: усі нові поля -> preview -> commit -> Status/Calendar; rapid edit/save, failure у кожній persistence boundary, unknown capacity, no compatible exercise, manual pending, restore/relaunch. Конкретні exercise prescriptions потребують qualified review у P23/P25.

### Перевірка

Onboarding/equipment/decision targeted tests; scripts/check-tests.cmd; schema checks за потреби; clean-install device journey.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/OnboardingUseCasesTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/OnboardingUseCasesTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCasesTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCasesTest.kt)

На завершення онови лише рядок P07 у PROGRESS і evidence `docs/implementation/evidence/P07.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
