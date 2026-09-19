# P19. HUD polish, доступність і зрозумілі тексти

**Модель: GPT-5.6 Terra; Reasoning: High.**
**Телефон: Так, для5 tabs, TalkBack, font2.0, IME, motion/contrast перевірок.**
**Залежності: P18.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P19**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P19 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **UX, естетика і поведінка; Localization/content clarity; Visual system consistency**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **UI consistency/accessibility pass, M**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Колір/матеріал; Insets/swipes**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [UI_UX_GUIDELINES.md](../../../UI_UX_GUIDELINES.md)
- [app/src/main/java/com/ihor/thesystem/core/theme/SystemTokens.kt](../../../app/src/main/java/com/ihor/thesystem/core/theme/SystemTokens.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemGlassComponents.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemGlassComponents.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemDialogComponents.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemDialogComponents.kt)
- [app/src/main/res/values/strings.xml](../../../app/src/main/res/values/strings.xml)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Виконай consistency/accessibility pass, не redesign. Збережи ієрархію, функціональні блоки, icons meaning, navigation й actions. Visual effects тільки tokens/shared primitives; великі панелі мають спільну форму. Не вводь web UI, нові heavy shaders/blur або decorative animation.
Перевір touch targets, TalkBack names/state/order, contrast, non-color status cues, font scaling до2.0, IME, system-disabled animation. Мінімальна адаптація для читабельності дозволена як bugfix з before/after evidence; не зменшуй текст до нечитабельного заради fit.
У видимих змінених компонентах винеси hardcoded copy до resources; uk/en, plurals і decimal/date formatting коректні. Важливі дії доступні без swipe. Прибери неправдиві «точні» readiness/medical promises; не змінюй алгоритми.
Приймання: усі5 tabs і ключові dialogs читаються, CTA reachable, gestures працюють, logging не уповільнився. Зафіксуй конкретні screenshots і manual accessibility results, не оголошуй accessibility certified.
Українська вже є default resource language у values/strings.xml; не вимагай неіснуючого values-uk. Інші locale packs додавай лише для погоджених supported locales, без фальшивого completion localization guard.

### Перевірка

Compile, localization/copy/Compose guards, relevant connected tests; screenshots1.0/1.3/2.0; manual TalkBack/system motion check.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/quality/LocalizedResourceCoverageTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/LocalizedResourceCoverageTest.kt)
- [app/src/test/kotlin/com/ihor/thesystem/quality/ProductionUiCopyGuardTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/quality/ProductionUiCopyGuardTest.kt)
- [app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt)

На завершення онови лише рядок P19 у PROGRESS і evidence `docs/implementation/evidence/P19.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
