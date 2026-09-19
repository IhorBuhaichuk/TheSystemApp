# P27. Робота зі справжнім beta-фідбеком

**Модель: GPT-5.6 Terra; Reasoning: Medium.**
**Телефон: Для аналізу feedback не потрібен; для відтворення UI/performance bugs потрібен відповідний Android-пристрій.**
**Залежності: P26.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P27**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P27 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Retention/market fit/revenue: немає даних; Стратегія, метрики й монетизація**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **Beta як навчання**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Лише feature rows за конкретними bug reports**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- `docs/beta/BETA_PLAYBOOK.md` (має створити P25; не вигадуй його вміст).
- `docs/beta/FEEDBACK_TEMPLATE.md` (має створити P25; не вигадуй його вміст).
- `docs/release/BETA_AUDIT.md` (має створити P26; не вигадуй його вміст).
- [docs/implementation/PROGRESS.md](../../../docs/implementation/PROGRESS.md)
- [PRODUCT_STRATEGY.md](../../../PRODUCT_STRATEGY.md)
- [MVP_DEFINITION.md](../../../MVP_DEFINITION.md)

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Цей крок виконується після справжньої beta, не відразу після P26. Попроси доступні анонімізовані reports/metrics і фактичні експертні review results. Якщо їх немає, підготуй коротку форму збору й постав needs_beta_data; не генеруй вигаданий retention або market-fit висновок.
Створи docs/beta/BETA_FINDINGS.md: observed bug vs preference vs hypothesis, affected build, frequency/denominator, impact, exact routing files, рекомендація. Не переоцінюй малу вибірку. Перевір time-to-value, повернення в інший день, logging friction, зрозумілість Today reason й safety flags.
Відтворені P0/P1 виправляй по одному bounded task з regression evidence; якщо потрібно міняти scope/дані/платежі, спочатку узгодь. Не виконуй broad redesign за одним коментарем. Зроби конкретні пропозиції value/monetization experiment без впровадження Billing.
Приймання: кожен actionable feedback має disposition і owner; документи відображають actual findings. Нові невідомі наперед bugs не приховуй під старим загальним done.

### Перевірка

Для analysis/docs check-doc-only; для реалізованих вузьких fixes відповідний playbook/tests/device reproduction. Немає даних -> чесний blocked milestone, не GO.

На завершення онови лише рядок P27 у PROGRESS і evidence `docs/implementation/evidence/P27.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
