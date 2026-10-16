# Перевірка підготовки

Дата: 2026-10-08. Це документація/налаштування workflow, не Android runtime verification.

Пізніше доповнення 2026-10-12: [узгодження та єдина папка prompts](evidence/2026-10-12-plan-alignment.md).
Нижче збережені результати початкового setup; нові результати не підміняють його дату.

## Skills та інтеграції

- [x] У поточній сесії доступні всі 6 project skills: product-lab, native-ux, quality-review, edge-to-edge, android-profiler, testing-setup.
- [x] check-agent-skills.cmd: 6 skills, 37 vendor files відповідають pinned hashes; сторонні helper scripts не виконувалися.
- [x] Figma whoami успішний, Starter/Full; resource skill index доступний. Design/write skills завантажуються при відповідній задачі.
- [ ] Конкретний Figma file, native nodes, prototype і approval: R03-R06. Authentication не є створеним дизайном.
- [ ] Billing provider/skill/SDK: свідомо R19 після вибору provider. Жодної оплати/credentials/backend зараз не підключено.

Новий generic full-stack/web skill не потрібний для нативного Android. Skill не замінює фахівця,
не доводить якість і не дає дозволу на широкий rewrite. Imagegen доступний для references, не замість Figma layers.

## Створено

- [x] REMASTER_PLAN, послідовність із dependencies, docs ownership та quality gates.
- [x] 26 нових R-промптів, reuse 28 P-промптів; конкретна модель/телефон/checklist у кожному.
- [x] Figma workflow/map, Free/Pro strategy та subscription rules з явними open decisions.
- [x] P01/P02 позначені виконаними за історичними evidence; P03 implementation відмічено, device gate не закритий.

## Межі й фінальна перевірка

- [x] `scripts/check-remaster-plan.cmd`: PASS; 54 prompts, узгоджені model headers/checklists, dependency DAG включно з conditional paid/sharing gates, локальні links та existing source entry paths.
- [x] `git diff --check`: exit 0; лише попередні CRLF warnings у незмінених цією задачею файлах.
- [x] SHA-256 усіх 472 Kotlin/KTS файлів збігаються з початком сесії, жодного доданого/видаленого source файла.
- [x] Ярлик `C:/Users/gesha/OneDrive/Робочий стіл/THE SYSTEM - Prompts.lnk` створений; повторне читання .lnk підтвердило target `docs/remaster/prompts`.
- [ ] `scripts/check-doc-only.cmd`: exit 1 через попередні P02/P03 Kotlin/KTS changes у dirty tree; нових source changes немає за hash comparison. Це не зелений gate і не причина відкотити чужу роботу.

Перевірку плану можна повторити через `scripts/check-remaster-plan.cmd`; вона не запускає Gradle,
не перевіряє runtime correctness і не підтверджує зовнішні URLs/Figma node access.
Нових Gradle/device/performance/Play tests у цій документаційній задачі не заявляємо.
Нової незалежної команди reviewer не запускали; P26 залишається окремим independent gate.
