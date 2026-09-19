# P23. Приватність, права на assets і чесні обіцянки

**Модель: GPT-5.6 Luna; Reasoning: Medium.**
**Телефон: Для документів/asset inventory не потрібен; лише для звірки реальних privacy screens за потреби.**
**Залежності: P04, P05, P07, P09, P15, P16, P17, P19.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P23**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P23 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Стандарти й release стан; ризик Avatar/asset portability/licensing; product/content risks**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **10. Верифікований release candidate і beta пакет**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Profile/edit; Backup; Health Connect**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [PRIVACY_POLICY.md](../../../PRIVACY_POLICY.md)
- [STORE_LISTING.md](../../../STORE_LISTING.md)
- [README.md](../../../README.md)
- [docs/HEALTH_CONNECT_RATIONALE.md](../../../docs/HEALTH_CONNECT_RATIONALE.md)
- [docs/SCREENSHOTS_CHECKLIST.md](../../../docs/SCREENSHOTS_CHECKLIST.md)
- [PRODUCT_STRATEGY.md](../../../PRODUCT_STRATEGY.md)
- [MVP_DEFINITION.md](../../../MVP_DEFINITION.md)
- [app/src/main/java/com/ihor/thesystem/data/repository_impl/AvatarRepositoryImpl.kt](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/AvatarRepositoryImpl.kt)
- [app/src/main/res](../../../app/src/main/res)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

На основі реалізованої поведінки створи data-flow inventory: local Room/prefs/files, HC, explicit exports, network/SDKs, optional internal AI. Узгодь privacy/store docs, backup retention/deletion, manual readiness/nutrition, no-AI availability; не називай local processing автоматично off-device collection. Актуальні Google Play/HC вимоги перевір за офіційними джерелами на дату виконання.
Перевір avatar URI lifecycle/portability та offline loading; код змінюй лише при підтвердженій вузькій помилці з тестом. Склади inventory graphic/font/audio assets та походження/license; невідомі rights познач needs_owner, не оголошуй їх вільними й не видаляй assets мовчки.
Створи HUMAN_REVIEW.md зі starter templates/readiness copy для review фахівця з тренувань і unresolved owner policy URL/contact. Не видавай AI review за медичне/юридичне схвалення. Hosted policy, account-specific Console answers і права на assets потребують справжніх даних власника.
Приймання: жодних unsupported medical claims; немає готових features лише на папері; policy й native screens узгоджені. Нові документи створюй у docs/release/.

### Перевірка

Для docs-only check-doc-only; для вузького runtime fix relevant tests/compile; перевір усі links/claims; не publish/submit без запиту.

Наявні тести для старту:
- [app/src/test/kotlin/com/ihor/thesystem/security/SensitiveDataTest.kt](../../../app/src/test/kotlin/com/ihor/thesystem/security/SensitiveDataTest.kt)

На завершення онови лише рядок P23 у PROGRESS і evidence `docs/implementation/evidence/P23.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
