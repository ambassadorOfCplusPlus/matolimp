# AI-прокси для МатОлимп (RouterAI / DeepSeek V4 Flash)

Зачем: ключ RouterAI **нельзя** класть в APK — его вытащат из приложения и потратят твой баланс.
Прокси держит ключ у себя, приложение ходит на прокси без ключа.

## Быстрый тест БЕЗ прокси (только debug!)
В `local.properties` впиши свой ключ:
```
ai.baseUrl=https://routerai.ru/api/v1
ai.apiKey=ТВОЙ_КЛЮЧ_ROUTERAI
```
Пересобери debug-APK — кнопка «🤖 Объяснить решение (ИИ)» заработает.
⚠️ Ключ окажется внутри APK — так можно только для личного теста, не для публикации.

## Для релиза: Cloudflare Worker (бесплатный тариф)
1. `npm i -g wrangler`
2. `wrangler login`
3. В папке `proxy/` создай `wrangler.toml`:
   ```toml
   name = "matolimp-ai"
   main = "routerai-worker.js"
   compatibility_date = "2024-11-01"
   ```
4. `wrangler secret put ROUTERAI_KEY` → вставь ключ RouterAI
5. (опц.) `wrangler secret put APP_TOKEN` → общий секрет, чтобы воркером не пользовались чужие
   (тогда добавь в приложении заголовок `X-App-Token` — скажи, впишу в AiClient)
6. `wrangler deploy` → получишь URL вида `https://matolimp-ai.<аккаунт>.workers.dev`
7. В `local.properties`:
   ```
   ai.baseUrl=https://matolimp-ai.<аккаунт>.workers.dev/api/v1
   ai.apiKey=
   ```
8. Пересобери — теперь ключ только в воркере, в APK его нет.

## Стоимость
DeepSeek V4 Flash: ~8 ₽/1M входящих, ~17 ₽/1M исходящих токенов. Одно объяснение —
несколько тысяч токенов (доли копейки). Приложение **кэширует** ответ по задаче в Room —
повторные открытия бесплатны.
