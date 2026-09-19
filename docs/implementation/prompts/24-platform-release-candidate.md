# P24. Кандидат релізу, Android16 і16KB

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Так, для release install/smoke; додатково API36 та16KB середовище, звичайний Realme не покриває все.**
**Залежності: P20, P22, P23.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P24**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P24 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Стандарти й release стан; Виконані перевірки**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **10. Верифікований release candidate і beta пакет; До public release**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Build/release/lint**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/build.gradle.kts](../../../app/build.gradle.kts)
- [app/src/main/AndroidManifest.xml](../../../app/src/main/AndroidManifest.xml)
- [app/proguard-rules.pro](../../../app/proguard-rules.pro)
- [gradle/libs.versions.toml](../../../gradle/libs.versions.toml)
- [app/src/main/java/com/ihor/thesystem/MainActivity.kt](../../../app/src/main/java/com/ihor/thesystem/MainActivity.kt)
- [app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt)
- [docs/SCREENSHOTS_CHECKLIST.md](../../../docs/SCREENSHOTS_CHECKLIST.md)
- [README.md](../../../README.md)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Побудуй verifiable release candidate без зміни scope. Перевір actual merged manifest/BuildConfig/AAB, R8, permissions і key absence без виведення секретів. Target/API вимоги повторно перевір в офіційних джерелах.
Оглянь packaged .so:16KB ELF segment alignment, APK packaging alignment і запуск у16KB environment. Не прирівнюй успішний bundle до runtime compatibility. На API36 перевір edge-to-edge, predictive back/adaptive sizing; runtime fixes лише відтворені й scoped.
Підготуй version/checksum/build-commands/test-evidence manifest у docs/release/RELEASE_CANDIDATE.md. Unsigned artifact чітко назви unsigned. Signing лише ключем/налаштуванням власника за його дозволом; не створюй, не ротуй, не коміть secrets. Для перевірки signed release request потрібні owner credentials, не доступ до їхнього вмісту в чаті.
Приймання: verified candidate offline/no-AI працює, restore/draft/logging intact; неперевірені API/ABI/signed gates явно pending. Не публікуй у Play та не пуш/мердж.

### Перевірка

lintDebug + release-relevant lint; check-tests/check-room/check-web-ui-guard; assembleRelease/bundleRelease; alignment tools; device release smoke; artifact manifest.

Наявні тести для старту:
- [app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/quality/AiReleaseConfigurationGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/AiReleaseConfigurationGuardTest.kt)

На завершення онови лише рядок P24 у PROGRESS і evidence `docs/implementation/evidence/P24.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
