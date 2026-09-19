# P26. Незалежний GO/NO-GO перед beta

**Модель: GPT-5.6 Sol; Reasoning: High.**
**Телефон: Так, для фінального candidate smoke; phone/емулятори мають покривати потрібні API cases.**
**Залежності: P01, P02, P03, P04, P05, P06, P07, P08, P09, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22, P23, P24, P25.**

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **P26**, не весь пакет.
Спочатку прочитай GRAPH_REPORT.md, AGENTS.md і [EXECUTION_RULES](../EXECUTION_RULES.md).
У [PROGRESS](../PROGRESS.md) прочитай лише P26 та його залежності; це заяви, не заміна evidence.
Контекст витягни точково, за заголовком/ID, не читай усі великі документи:
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): **Усі AUD-01..12; Ризики, які ще треба довести; Стандарти й release стан**.
- [DEVELOPMENT_ROADMAP](../../../DEVELOPMENT_ROADMAP.md): **До зовнішньої beta**.
- [FEATURE_MAP](../../architecture/FEATURE_MAP.md): **Критичні послідовності**. Використовуй відповідні routing rows або названий розділ.
Прочитай відповідний playbook за AGENTS; для UI також UI_UX_GUIDELINES.md.

### Початкові файли

- [GRAPH_REPORT.md](../../../GRAPH_REPORT.md)
- [docs/architecture/FEATURE_MAP.md](../../../docs/architecture/FEATURE_MAP.md)
- [PROJECT_AUDIT.md](../../../PROJECT_AUDIT.md)
- [DEVELOPMENT_ROADMAP.md](../../../DEVELOPMENT_ROADMAP.md)
- [docs/implementation/PROGRESS.md](../../../docs/implementation/PROGRESS.md)
- [docs/implementation/COVERAGE.md](../../../docs/implementation/COVERAGE.md)
- `docs/release/RELEASE_CANDIDATE.md` (має створити P24; не вигадуй його вміст).

Це entry points, не дозвіл переписати кожен файл. Якщо файл уже перенесено, знайди symbol через graph/rg; розширюй scope лише за прямою залежністю.

### Завдання й приймання

Ти незалежний reviewer. Не вір статусам done без перевірки code path, тесту й artifact revision. Не змінюй production code у цьому task. Читай evidence по AUD-ID та тільки відповідні changed contracts; не повторюй безцільне whole-repo сканування.
Зістав усі12 findings,8 додаткових ризиків і cross-cutting gaps із COVERAGE.md. Для кожного: fixed+verified / risk disproved+test / open / needs_device / needs_owner. Відкладена функція не є виправленою; перевір чесність shipped docs і обґрунтування disposition.
Запусти full checks і critical real journey: fresh setup, draft resume, duplicate finish, edit A/B/C, backup failure/restore, no-AI, unknown readiness, HC deny, five tabs. Історичні звіти не замінюють current candidate tests.
Створи docs/release/BETA_AUDIT.md із P0/P1/P2, exact files, evidence, device/build identifiers і GO/NO-GO. Missing critical evidence -> NO-GO, а не умовна зелена позначка. Для відкритого defect дай вузький repair prompt з регресійним тестом; не переписуй весь план.

### Перевірка

check-tests/check-room/check-web-ui-guard, lintDebug, bundleRelease, relevant connected tests; no code edits/push/merge. Окремо owner/expert gates.

На завершення онови лише рядок P26 у PROGRESS і evidence `docs/implementation/evidence/P26.md` за EXECUTION_RULES. Не познач done без потрібних перевірок; вкажи точні blockers. Не commit/push/merge та не запускай наступний prompt.
