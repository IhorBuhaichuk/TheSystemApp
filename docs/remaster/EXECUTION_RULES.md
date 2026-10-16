# Виконання одного кроку

Ці правила доповнюють AGENTS.md і [правила P-пакета](../implementation/EXECUTION_RULES.md).
Планування санкціоновано; кожен implementation prompt запускається окремо.

## Мінімальний контекст

1. GRAPH_REPORT.md, AGENTS.md, один prompt, його рядок QUEUE/PROGRESS і evidence prerequisites.
2. Один relevant FEATURE_MAP row, потрібний playbook/skill і перелічені entry files.
3. Для R UI також DESIGN_WORKFLOW, disposition з FIGMA_MAP та action inventory з R02: approved node для нового presentation, baseline для `preserve-current`. R07 вимагає ранню перевірку прототипу з людьми та owner approval із R06.
4. `git status --short` + targeted diff: source of truth є код, не дата старого аудиту.

Не читати всі промпти, всю історію чату чи всі skills. `A`, `D`, `T`, `I` у промптах означають
префікси з FEATURE_MAP; це точні repository-relative paths, не приблизні пошукові підказки.
Нові шляхи позначено NEW. Не створювати всі перелічені кандидати, якщо вистачає наявного компонента.
Усі P/R prompts у `docs/remaster/prompts/`; перший номер сортує, ID не змінюється.
На старті онови Active task/етап у блоці «Де ми зараз» QUEUE; після паузи/завершення
зафіксуй там next pointers і невиконані gates. Детальний статус не дублюй із PROGRESS.

## Границі

- Один ID, одна завершувана зміна. Нову велику знахідку записати як follow-up, не поглинати весь backlog.
- Не видаляти дії/схему/дані через recommendation hide/defer. Для видалення потрібна окрема конкретна згода.
- Дизайн не переписує Today/XP/readiness/finish правила. Mapper змінюється лише разом із тестом контракту.
- Зміна layout дозволена тільки в рамках схваленого R-макета; surface-only tasks зберігають layout.
- Нові бібліотеки: причина, сумісність, ліцензія, ціна підтримки; жодних blind latest upgrades.
- Жодних автоматичних commits/push/merge, account purchase, production credentials, deploy або Play publication.
- Незалежний review означає реального другого агента/людину. Не називати зміну ролі тим самим агентом незалежністю.

## Перевірки

Docs: `scripts/check-doc-only.cmd`, `git diff --check`, посилання/шляхи; у dirty tree порівняти SHA-256
Kotlin/KTS із початком задачі, не скасовувати попередні зміни заради зеленого wrapper.
Code: `scripts/check-quick.cmd`, targeted tests; behavior -> `scripts/check-tests.cmd`.
Room: `scripts/check-room.cmd` + schema/migration і реальний Android Room test.
UI: `scripts/check-web-ui-guard.cmd`, focused connected tests і візуальна перевірка affected flow.

Для R07-R15 UI matrix: 360x640 dp і звичайний телефон, fontScale 1.0/1.3; для P19 додатково2.0,
TalkBack, довгі українські рядки, темні поверхні, reduced/disabled motion, навігація/IME.
Вміст може прокручуватися; primary action має бути досяжною без прихованого жесту/обрізання.
Не використовувати viewport-scaled font або ellipsis для критичного змісту.

Телефон не потрібний для MD/Figma/JVM/compile. Runtime gate вимагає Android device/emulator,
performance gate фізичного reference device. Без нього продовжити host checks, але не ставити done.
Не виконувати clear/uninstall/restore/reinstall над особистою базою без окремого дозволу й backup.
Вибрати serial, synthetic profile; тимчасові settings повернути. Play-покупки лише license tester/sandbox.

## Залежності та завершення

Prerequisite для початку code task: його потрібний контракт реалізований і host-перевірений.
Якщо залишився лише device gate, можна продовжити незалежні host-частини із записаним ризиком;
закривати downstream milestone як done або видавати RC до проходження критичного gate не можна.
Owner/design/value gates не обходяться цим правилом. Не підставляти вигаданий Figma node/ціну/покупку.

Оновити один статус: P у ../implementation/PROGRESS.md; R у PROGRESS.md. Одночасно оновити checkbox
реально завершених частин у тому самому prompt; джерело доказів завжди evidence, не галочка. Evidence у відповідній папці:
дата/HEAD/dirty scope, зміни, exact checks/results, runtime environment, unresolved gates, наступний ID.
R статуси: pending, in_progress, needs_owner, implemented_needs_device, blocked_dependency,
needs_beta_data, deferred, done. `deferred` не означає виконано; записати вплив на release scope.
Завершені prerequisites, evidence links і checkbox перевіряє `scripts/check-remaster-plan.cmd`.
Наявність evidence-файла не доводить правдивість його змісту: результати й approvals звіряй вручну.
Створення цього пакета не закриває R01-R27. Зупинитися після одного prompt.

## Forward-only ownership / D30

Use NN in QUEUE, not numerical P/R IDs. On "next", resolve the current checkpoint;
never ask the owner to jump to a later consumer and back. New persisted fields include
migration, backup/import defaults, projections and tests in their introducing task.
Earlier safety tasks are regression baselines, not tasks the owner must rerun.
Keep deferred device checks in evidence and the device-gate pointer. Incomplete
dependencies cannot be marked done. Optional branches require explicit recorded defer.
Do not launch the next prompt automatically.
