# P06. Lint і правильна локалізація форматування

**Модель: GPT-5.6 Luna; Reasoning: Medium.**
**Телефон: Для виправлення й lint не потрібен; runtime locale check на Android-пристрої/емуляторі наприкінці.**
**Залежності: немає.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P06**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P06 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-08**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **05. Повернути зелений lint без suppression**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Build/release/lint; Statistics/chart** у «Маршрути задач»; для P26/P28 потрібні тільки relevant routes.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/feature/statistics/ui/components/TonnageChartCanvas.kt](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/components/TonnageChartCanvas.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModelMappers.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModelMappers.kt)
- [app/build.gradle.kts](../../../app/build.gradle.kts)
- [app/src/test/kotlin/com/ihor/thesystem/quality/ReleaseLintGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/ReleaseLintGuardTest.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Відтвори :app:lintDebug і виправ NonObservableLocale через observable locale API, сумісний з установленою Compose-версією. Розділяй user-facing locale formatting та стабільні machine/storage formats. Виправ actual lint errors; triage StringFormatCount, ConfigurationScreenWidthHeight, ModifierParameter та інші warnings лише у відповідних файлах, які покаже lint.
Не suppress, не створюй baseline для приховування цього defect, не оновлюй усі dependencies. Для широкої API migration поза цим scope залиш окремий bounded finding. Не ламай введення десяткової коми/крапки.
Приймання: lint errors=0, locale change оновлює chart labels, uk/en values коректні, storage/protocol не залежать від UI locale. Для Android-only перевірки без пристрою постав needs_device, а не вигадуй screenshots.

### Перевірка

scripts/check-quick.cmd; targeted formatting tests; :app:lintDebug; :app:bundleRelease. Зафіксуй warnings окремо від errors.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/quality/LocalizedResourceCoverageTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/LocalizedResourceCoverageTest.kt)

На завершення онови лише рядок P06 у PROGRESS і evidence `docs/implementation/evidence/P06.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
