# P16. Перевірений AI gatekeeper і корисний локальний режим

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Для parser/domain tests не потрібен; Android-пристрій/емулятор для release no-AI UI.**
**Залежності: P03, P08, P10, P12.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P16**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P16 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AI safety boundaries; No-AI usefulness; ризики context/fallback**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **AI і monetization, лише за evidence**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **AI/error/fallback; AI mutation**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [domain/src/main/java/com/ihor/thesystem/domain/usecase/ValidateDirectivesUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/ValidateDirectivesUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/ApplyAiRecommendationsUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/ApplyAiRecommendationsUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt)
- [domain/src/main/java/com/ihor/thesystem/domain/usecase/SendArchitectAnalysisUseCase.kt](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/SendArchitectAnalysisUseCase.kt)
- [app/src/main/java/com/ihor/thesystem/data/remote/ai/AiArchitectResponseParser.kt](../../../app/src/main/java/com/ihor/thesystem/data/remote/ai/AiArchitectResponseParser.kt)
- [app/src/main/java/com/ihor/thesystem/data/remote/ai/AiAvailability.kt](../../../app/src/main/java/com/ihor/thesystem/data/remote/ai/AiAvailability.kt)
- [app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModel.kt](../../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModel.kt)
- [app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt](../../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Не переписуй AI v2 заради нового prompt. Перевір усі mutation paths через domain gate: malformed/extra fields, unknown exercise, extreme/negative/non-finite targets, conflicting/stale decision, unavailable validation context, duplicate apply. Якщо потрібного безпечного context немає, не застосовуй неперевірені targets.
Weekly insight короткий,1-3 actionable suggestions, чесна uncertainty/provenance. AI unavailable/rate-limited/malformed дає local insights і system verdict, не raw exceptions. Future/disabled модулі не мають створювати dead-end premium experience; збережи реальні доступні annual/manual дії, зміни copy/state локально без redesign.
Release Gemini лишається disabled, key порожній. Tests використовують fakes, не реальний платний API. Cancellation не перетворювати на фальшивий успіх чи raw error.

### Перевірка

Targeted parser/validation/apply/fallback/VM tests; scripts/check-tests.cmd; release offline/no-AI device smoke; no credential changes.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ValidateDirectivesUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ValidateDirectivesUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ApplyAiRecommendationsUseCaseTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/domain/usecase/ApplyAiRecommendationsUseCaseTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/data/remote/ai/AiArchitectResponseParserTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/data/remote/ai/AiArchitectResponseParserTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModelTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModelTest.kt)

На завершення онови лише рядок P16 у PROGRESS і evidence `docs/implementation/evidence/P16.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
