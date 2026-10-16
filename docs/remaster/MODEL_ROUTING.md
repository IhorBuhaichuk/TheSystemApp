# Моделі й економія

Перевірено 2026-10-08: поточний host пропонує `gpt-6-luna` і `gpt-6.1-sol`.
У кожному P/R prompt є повна рекомендована назва і reasoning; це вибір для наступного запуску,
не твердження, якою моделлю колись виконано P01/P02. Модель обирає власник у selector.

- **GPT-6 Luna / Medium**: точкова документація, чіткий UI transfer, локалізація, обмежений fix.
- **GPT-6.1 Sol / Medium**: кілька пов'язаних UI/adapter файлів із уже визначеним контрактом.
- **GPT-6.1 Sol / High**: data integrity, architecture, неоднозначний UX/product scope, billing/security, final review.
- **GPT-6 Astra**: не за замовчуванням; лише окремий складний blocker після короткого handoff, за рішенням власника.

Це інженерна рекомендація, не benchmark цього repo і не гарантія кількості tokens.
Офіційний [model selection guide](https://developers.openai.com/api/docs/guides/model-selection)
описує Luna як економний варіант, а Sol як варіант для складніших задач.
Збільшення effort має ціну; [reasoning guidance](https://developers.openai.com/api/docs/guides/reasoning)
пояснює залежність reasoning від задачі й моделі. Точні account limits не виводимо з API price.

Економія: один ID -> 2-6 context reads -> relevant code -> тест -> evidence -> stop.
Не завантажувати весь audit/усі skills, не повторювати успішний build без зміни inputs,
не переводити всю роботу на High/Max через один складний крок.
Якщо межі задачі виявилися ширшими, записати blocker/наступні файли та перейти на Sol за погодженням,
а не робити багато безрезультатних дешевих спроб. Модель не замінює device tests або independent review.
