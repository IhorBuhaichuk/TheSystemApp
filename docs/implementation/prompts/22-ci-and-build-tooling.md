# P22. Надійні quality gates і підтримуваний build

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Не потрібен для host CI checks; Android smoke виконує test emulator/runner, не обов'язково особистий телефон.**
**Залежності: P06, P18, P21.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P22**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P22 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-08/11; Performance і maintainability build warnings**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **09. Справжні integration journeys замість лише test-shell; Вибірковий architectural cleanup, M-L**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Build/release/lint**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [.github/workflows/android-ci.yml](../../../.github/workflows/android-ci.yml)
- [app/build.gradle.kts](../../../app/build.gradle.kts)
- [domain/build.gradle.kts](../../../domain/build.gradle.kts)
- [baselineprofile/build.gradle.kts](../../../baselineprofile/build.gradle.kts)
- [gradle.properties](../../../gradle.properties)
- [gradle/libs.versions.toml](../../../gradle/libs.versions.toml)
- [gradle/wrapper/gradle-wrapper.properties](../../../gradle/wrapper/gradle-wrapper.properties)
- [scripts/check-tests.ps1](../../../scripts/check-tests.ps1)
- [scripts/check-room.ps1](../../../scripts/check-room.ps1)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Залиш зрозумілий inexpensive CI: compile, повні unit tests/guards, Room checks, lint, unsigned release bundle; Linux не запускає .cmd. Усунь дублювання filtered test runs або збережи full-suite reports до перезапису. Always upload reports; чітко розрізняй failed/skipped/missing artifact.
Додай один bounded emulator smoke job для реальних Room/migration і навігаційних tests із P18, без великої матриці. Performance device suite окремо від кожного push. Cache trust boundaries, minimal permissions, cancel superseded branch runs; credentials не друкувати і не потрібні для compile.
Звір official Gradle/AGP/Kotlin/KSP/Compose/Baseline Profile compatibility; прибери deprecated flags лише підтримуваним шляхом. Не оновлюй весь стек до latest без необхідності; unexplained warning не приховуй suppression.
Приймання: fresh green checks локально наскільки доступно; на GitHub workflow лише після окремого authorized push. Не називай remote CI green, якщо не було remote run.

### Перевірка

check-quick/check-tests/check-room/check-web-ui-guard; lintDebug; bundleRelease; compileDebugAndroidTestKotlin; validate workflow/config; smoke device/runner evidence.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/quality/DependencyCatalogGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/DependencyCatalogGuardTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/quality/AiReleaseConfigurationGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/AiReleaseConfigurationGuardTest.kt)

На завершення онови лише рядок P22 у PROGRESS і evidence `docs/implementation/evidence/P22.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
