// Cloudflare Worker — прокси для AI-объяснения (RouterAI / DeepSeek V4 Flash).
// Держит ключ RouterAI в секрете, приложение ходит СЮДА (без ключа в APK).
//
// Деплой:
//   1) npm i -g wrangler
//   2) wrangler login
//   3) wrangler secret put ROUTERAI_KEY   (вставь ключ RouterAI)
//   4) (опц.) wrangler secret put APP_TOKEN   (общий секрет приложения)
//   5) wrangler deploy
// Затем в local.properties приложения:
//   ai.baseUrl=https://<имя>.<аккаунт>.workers.dev/api/v1
//   ai.apiKey=            (оставить пустым — ключ теперь в воркере)

const UPSTREAM = "https://routerai.ru/api/v1/chat/completions";

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "POST only" }, 405);
    }
    const url = new URL(request.url);
    if (!url.pathname.endsWith("/chat/completions")) {
      return json({ error: "not found" }, 404);
    }

    // Необязательная защита: общий секрет приложения, чтобы воркером не пользовались чужие.
    if (env.APP_TOKEN) {
      const auth = request.headers.get("X-App-Token");
      if (auth !== env.APP_TOKEN) return json({ error: "unauthorized" }, 401);
    }

    const body = await request.text();
    if (body.length > 30000) return json({ error: "payload too large" }, 413);

    const upstream = await fetch(UPSTREAM, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${env.ROUTERAI_KEY}`,
      },
      body,
    });

    return new Response(upstream.body, {
      status: upstream.status,
      headers: { "Content-Type": "application/json" },
    });
  },
};

function json(obj, status) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
