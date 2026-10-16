# Права доступу і підписка

Дата: 2026-10-08. **Проєктований контракт, billing ще не реалізований.**
R16 робить лише мінімальний FeatureAccess contract і production Free provider, з fake у tests.
Наведена нижче paid lifecycle policy не є scope R16: R19 затверджує її та integration ADR
після R17, R20/R21 реалізують, R23 перевіряє повний цикл. D14 не блокує Free beta.
Цифрова subscription у Play
за базовим сценарієм використовує Google Play Billing; регіональні правила перевірити на момент продажів.

## Ownership

- Domain: FeatureAccess/Entitlement policy, feature IDs, allow/deny/reason, clock abstraction, unit tests.
- App/data: store/provider SDK, verified snapshot cache, відновлення покупки, refresh, lifecycle mapping.
- App/feature: UI state, ціна й action results, ніяких прямих перевірок purchase token у Compose.
- Verification service або managed provider: перевірка прав і захист серверних ресурсів; secrets не в APK.
- Room workout data, billing cache й purchase identity мають різні owners; restore backup не видає Pro.

## Поведінка

| Стан | Правило |
| --- | --- |
| Free | Core доступний, лише погоджені додаткові capability gated |
| Unknown/loading | Не показувати хибне expired і не заважати Free/save; використати останній valid snapshot за policy |
| Pending purchase | Не видавати нове Pro до підтвердження; показати pending без повторного списання |
| Verified active | Погоджені entitlement доступні до verified expiry/оновлення |
| Auto-renew canceled | Доступ до кінця вже оплаченого періоду, не миттєве блокування |
| Grace | Права зберігаються відповідно до verified store state |
| Account hold / expired / revoked | Немає нового paid access; Free та власні дані залишаються |
| Offline | Обмежена кешована policy з expiry і clock tests; TTL ще open D14, не безстроковий локальний Boolean |
| Restore / account mismatch | Повторна verified звірка; не зливати чужі покупки/локальні профілі автоматично |

Під час active workout зміна entitlement не перериває logging, draft або finish. Не видаляти
створені дані й не блокувати export при downgrade. Відновлення покупки не є відновленням workout backup.
Власний mandatory login не вводимо автоматично: R19 визначає мінімальну identity policy і trade-offs.

## Безпека

Production права не виводити з `BuildConfig.DEBUG`, UI toggle або неперевіреного callback.
Verified purchase -> grant once -> acknowledge за вимогами актуального provider. Pending не grant.
Повторні та переставлені RTDN/webhooks обробляти idempotently з повторною звіркою authoritative state.
Refund/revoke, expiry, account switch, network error мають тести. Не логувати purchase tokens/health content.
Клієнтські local locks не є DRM; серверні AI/cloud ресурси перевіряють права на сервері.

## Обов'язкові gates

1. R17: owner-approved цінність/price hypothesis; R18: capability працює, Free baseline збережений.
2. R19: ADR direct Play + service або managed provider, identity/offline/privacy, budget і operational owner.
3. R20/R21: verification та adapter; sandbox credentials поза repo; Play products/console setup потребують власника.
4. R22/R23: transparent paywall, restore/manage, pending/cancel/refund/offline tests на license tester.
5. R24/P28: release variant перевірений, fake unlock відсутній, support/refund/privacy і explicit publish approval.

Не обмежуємо безкоштовні функції у поточній сесії. Не додаємо Billing SDK, cloud акаунт або paid API автоматично.
Офіційні джерела, перевірено 2026-10-08; перед імплементацією перевірити знову:
[lifecycle](https://developer.android.com/google/play/billing/lifecycle/subscriptions),
[verification security](https://developer.android.com/google/play/billing/security),
[Play payments](https://support.google.com/googleplay/android-developer/answer/10281818?hl=en).
