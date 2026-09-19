# P18. Справжні UI-сценарії всього застосунку

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так. Реальний телефон для normal size; емулятор або тимчасова test конфігурація для360x640/font variants.**
**Залежності: P01, P02, P03, P04, P05, P06, P07, P08, P09, P10, P11, P12, P13, P14, P15, P16, P17.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P18**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P18 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **AUD-11; responsiveness/navigation risks**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **09. Справжні integration journeys замість лише test-shell**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Insets/swipes**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt](../../../app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt)
- [app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt](../../../app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt)
- [app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt)
- [app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt)
- [app/src/androidTest/java/com/ihor/thesystem/data/local/room/database/AppDatabaseMigrationTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/data/local/room/database/AppDatabaseMigrationTest.kt)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Заміни/доповни test-shell checks реальними AppNavGraph journeys із контрольованою test DB/Hilt dependencies. Onboarding -> Today -> Cycle -> log -> finish -> Statistics -> restart. Додай stable semantic tags, які не змінюють контент.
Протестуй реальні Status, Calendar, Cycle, Statistics, Profile на360x640dp і normal phone, font1.0/1.3; primary CTA, IME, insets, back, horizontal mode/tab ownership та vertical scroll. UI state tests не підміняють screenshot glyph-clipping check. Виправ тільки відтворені regressions із regression test, не redesign.
Додатково interruption/draft recovery, repeat finish, backup confirm/cancel, no-AI/offline, denied permissions. Використовуй records після P01-P17, не вимагай старих symbols, якщо контракти змінились.
Не вважай усі tabs перевіреними за одним bottom-nav test. Відсутній API/device case лиш pending із конкретною командою/умовою.

### Перевірка

scripts/check-quick.cmd; relevant connectedAndroidTest classes; scripts/check-web-ui-guard.cmd; screenshots+device/build/font metadata; bounded unit regressions.

Наявні тести для старту:
- [app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt](../../../app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt)

На завершення онови лише рядок P18 у PROGRESS і evidence `docs/implementation/evidence/P18.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
