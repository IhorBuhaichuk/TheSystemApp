# P25. Зрозумілий план beta для власника

**Модель: GPT-5.6 Luna; Reasoning: Medium.**
**Телефон: Для написання плану не потрібен; testers використовуватимуть свої Android-пристрої.**
**Залежності: P17, P23, P24.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P25**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P25 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Стратегія, метрики й монетизація; Продукт, новизна і конкуренція**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Beta як навчання; Beta gates**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Beta metrics**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [PRODUCT_STRATEGY.md](../../../PRODUCT_STRATEGY.md)
- [MVP_DEFINITION.md](../../../MVP_DEFINITION.md)
- [DEVELOPMENT_ROADMAP.md](../../../DEVELOPMENT_ROADMAP.md)
- [STORE_LISTING.md](../../../STORE_LISTING.md)
- [docs/implementation/PROGRESS.md](../../../docs/implementation/PROGRESS.md)
- `docs/release/RELEASE_CANDIDATE.md` (має створити P24; не вигадуй його вміст).

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Створи docs/beta/BETA_PLAYBOOK.md для власника без технічної підготовки: кого запросити, інсталяція, consent/приватність,5-7 чітких сценаріїв, як надіслати versioned bug report без health content, stop conditions при data-loss/safety issue. Окремо discovery usability та формальні account-specific Google Play closed-test вимоги; правила перевір актуально, account details не вигадуй.
Додай feedback template із task success/time/confusion, second-day/second-workout/W2/W4 counts і optional willingness-to-pay questions. Визнач знаменники, small-sample limits; не обіцяй гарантований retention або дохід.
Монетизація лише як дослідження recurring value/програм/тем; не додавай Billing, рекламу, paywall власних logs або paid AI. Підготуй docs/beta/FEEDBACK_TEMPLATE.md та OWNER_CHECKLIST.md: policy URL/contact, asset rights, expert content review, Console account, signing. Можливість роботи offline і backup protection поясни просто.
Не розсилай запрошення, не створи automation і не завантажуй build без окремого дозволу.

### Перевірка

scripts/check-doc-only.cmd; links/path validation; strategy/MVP не суперечать implementation. Заповнені приклади познач fictional, не tester evidence.

На завершення онови лише рядок P25 у PROGRESS і evidence `docs/implementation/evidence/P25.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
