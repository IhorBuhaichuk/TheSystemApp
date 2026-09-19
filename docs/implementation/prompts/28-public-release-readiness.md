# P28. Фінальна готовність до публічного запуску

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так, фінальний signed candidate на фізичному телефоні та потрібних API/16KB середовищах.**
**Залежності: P27.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P28**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P28 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Всі відкриті P0/P1; Стандарти й release стан; product evidence**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **До public release; Практичне визначення 10/10**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Лише affected features після beta**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- `docs/beta/BETA_FINDINGS.md` (має створити P27; не вигадуй його вміст).
- `docs/release/BETA_AUDIT.md` (має створити P26; не вигадуй його вміст).
- `docs/release/RELEASE_CANDIDATE.md` (має створити P24; не вигадуй його вміст).
- [docs/implementation/PROGRESS.md](../../../docs/implementation/PROGRESS.md)
- [docs/implementation/COVERAGE.md](../../../docs/implementation/COVERAGE.md)
- [PRIVACY_POLICY.md](../../../PRIVACY_POLICY.md)
- [STORE_LISTING.md](../../../STORE_LISTING.md)
- [MVP_DEFINITION.md](../../../MVP_DEFINITION.md)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Проведи public-readiness acceptance review актуального candidate після beta fixes. Не оголошуй10/10 за кількістю закритих tasks. Перевір незакриті data-loss/safety bugs, current tests/device evidence, beta feedback, expert content review, hosted privacy/contact, asset rights, screenshots, signing/versioning, account-specific Console requirements.
Правила Google Play перевір заново за офіційними джерелами на дату submission; не покладайся на вересневі deadlines. Payments gates лише якщо платежі справді додано окремим approved scope; зараз їхня відсутність не defect.
Створи docs/release/PUBLIC_RELEASE_READINESS.md: GO/NO-GO, exact artifact checksum, checked/unchecked gates, manual owner actions, rollout/support/rollback plan без втрати локальних даних. Відокрем technical readiness від market fit та прогнозів доходу.
Якщо залишилися blockers, сформуй адресні repair prompts і не став milestone complete. Не підписуй невідомим ключем, не submit/publish, не push/merge без окремого запиту.

### Перевірка

Fresh full unit/Room/Compose guards + lint + actual release build; candidate end-to-end/device compatibility smoke; check docs consistency; відсутні owner/beta докази явно познач.

На завершення онови лише рядок P28 у PROGRESS і evidence `docs/implementation/evidence/P28.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
