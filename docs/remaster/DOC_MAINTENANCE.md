# Контекст, який підтримуємо

Мета: менше повторного читання, не більше копій однієї правди.

| Подія | Оновити | Не робити |
| --- | --- | --- |
| Новий contract/route/persistence owner | relevant GRAPH_REPORT + FEATURE_MAP row + tests link | Не переписувати весь audit |
| Room change | entity/DAO/version/migration/schema/backup, P/R evidence | Не вважати schema guard доказом SQL behavior |
| Новий Figma component/approved variant | FIGMA_MAP: file/node/token/Kotlin/state/revision | Не зберігати лише PNG як design contract |
| Free/Pro рішення | MONETIZATION_STRATEGY + SUBSCRIPTION_RULES + affected gate | Не плодити `isPro` у Compose |
| Новий bug | AUD/F/R ID, reproduction, owner prompt, severity, check | Не закривати дефект тільки новим документом |
| Completed task / частина | Один рядок P або R PROGRESS + evidence + checkbox того самого prompt; оновити «Де ми зараз» у QUEUE | Не ставити галочку до перевірки; не дублювати повний реєстр статусів у handoff |
| Старт/зупинка/новий чат | QUEUE: етап, Active task/next pointers; SESSION_HANDOFF: рішення, 2-6 reads, pending checks | Не копіювати transcript; поточне місце в роботі живе в QUEUE |
| Зміна scope/dependency | DECISIONS + QUEUE + конкретний prompt | Не залишати суперечливі execution orders |

У GRAPH_REPORT тримати приблизно <=200 рядків. Детальні сценарії у FEATURE_MAP або окремих
contract docs. Код має пріоритет при розбіжності; позначити stale section і перевірити її точково.
Не оновлювати audited revision усього графа після перевірки лише одного billing-контракту.

Формат нового рішення: дата, ID, accepted/proposed/deferred/superseded, причина, файли, перевірка,
що ще невідомо. Зовнішні стандарти/ціни/API перевіряти на момент залежного task, датувати джерело.

## Єдина папка та завершення

Усі повні prompts у `docs/remaster/prompts/`, файли `NN-ID-назва.md`.
NN задає dependency-safe порядок, незмінний P/R ID зберігає evidence та історію.
Новий prompt/зміна порядку: одночасно оновити QUEUE, посилання й перевірити порядок;
старий шлях знайти за ID, не створювати дублікати. Historical пакети не є execution queue.

`done` (для P також `not_reproduced_verified`) потребує закритих checkbox, непорожнього
`evidence/ID.md`, посилання на нього з PROGRESS і завершених prerequisites із QUEUE.
Частково checked prompt теж має evidence; `implemented_needs_device` не є done.
Evidence пояснює факт перевірки, а не лише містить слово PASS. Guard перевіряє структуру,
не достовірність тестів/approval. Не створюй пустий доказ для обходу gate.
P28 заповнює actual candidate scope/sharing у QUEUE: deferred paid гілка не блокує Free,
але paid/sharing candidate вимагає відповідних завершених задач.

Після правок плану: `scripts/check-remaster-plan.cmd`; після змін guard також
`scripts/test-remaster-plan.cmd` (ізольовані positive/negative fixtures, без app/device).
Архівні документи позначати historical/pending reconciliation з current entry,
не переписувати давні результати перевірок. Повну ревізію product scope виконує R01.

Якщо виявлена робота не вміщується в prompt, додати короткий repair prompt з новим стабільним ID,
dependencies та acceptance. Оновити coverage, але не перенумеровувати старі P/R IDs і не заявляти
наперед повне покриття невідомих bugs beta.
