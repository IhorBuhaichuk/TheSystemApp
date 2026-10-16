# Узгодження плану та єдина папка

Дата: 2026-10-12, після відновлення перерваної сесії.
HEAD: `36d6c480da90438c1b5adaf363de7cfb6986f9d8`, branch `codex/overnight-premium-hud-polish`.
Dirty scope: збережені попередні P02/P03 Kotlin/KTS, Room schemas/tests та planning docs.
Ця робота: лише документація, перенесення prompt-файлів і PowerShell/CMD guards.

## Виконано

- [x] Historical/current/pending reconciliation позначки; повний product scope залишається R01.
- [x] R09 доданий до prerequisites R11 у prompt і QUEUE.
- [x] Secondary screen/dialog handoff: approved node або preserve-current baseline із owner task.
- [x] R06 отримав ранній 3-5-person prototype UX gate перед R07; сам research не проводився.
- [x] R16 обмежений мінімальним Free contract/provider; paid lifecycle перенесено до R19/R21/R23.
- [x] Усі 54 повні P/R prompts перенесено в `docs/remaster/prompts/NN-ID-*.md`, без робочих дублікатів; виправлено відносні посилання. P/R IDs, виконані checkbox та evidence збережені.
- [x] QUEUE містить єдину dependency-safe послідовність, current-stage/active/next pointers та conditional Free/Paid/sharing gates. Правила зобов'язують оновлювати checkpoint після кожної задачі.
- [x] Guard відхиляє missing/empty/unlinked evidence, хибні checkbox/status, незавершені prerequisites, неправильний порядок/дублікати й застарілі next pointers.

## Перевірки

- `scripts/check-remaster-plan.cmd`: exit 0; 54 prompts, порядок/DAG, models, progress/evidence, pointers, local links та existing source paths.
- `scripts/test-remaster-plan.cmd`: exit 0; 29 ізольованих positive/negative scenarios, включно з Free без paid гілки, paid/sharing gates і transitive dependency. Fixtures у власній temp-папці, видалені з перевіркою меж; app/data не змінювалися.
- `scripts/check-agent-skills.cmd`: exit 0; 6 skills, 37 vendor hashes.
- `git diff --check`: exit 0; попередні CRLF warnings у source залишені без змін.
- Local-link pass по кореневих MD та docs: missing targets не знайдено; зовнішні URLs/anchors/Figma access не перевірялися.
- SHA-256 до/після відновленої сесії: 472 Kotlin/KTS файли, 0 змінених/доданих/видалених. Це порівняння з working tree на старті відновлення, не з HEAD.
- Desktop shortcut `THE SYSTEM - Prompts.lnk`: target `docs/remaster/prompts` існує, переналаштування не потрібне.
- `scripts/check-doc-only.cmd`: **exit 1** через попередні Kotlin/KTS зміни. Це не PASS; їх збережено, нових source edits цієї роботи немає.

## Передача

Етап 0 готовий. Next без телефона: R01 (03-й файл); device gate: P03 (09-й файл).
Жоден R-task не позначено done. Немає нових Gradle/device/Play tests, user-study results,
незалежного review, commit/push/merge або запуску наступного prompt.
Поточне місце в роботі: [QUEUE](../QUEUE.md); правила: [DOC_MAINTENANCE](../DOC_MAINTENANCE.md).
