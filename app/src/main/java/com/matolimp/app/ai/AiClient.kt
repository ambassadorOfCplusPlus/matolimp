package com.matolimp.app.ai

import com.matolimp.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
private data class ChatMsg(val role: String, val content: String)

@Serializable
private data class ChatReq(
    val model: String,
    val messages: List<ChatMsg>,
    val temperature: Double = 0.3
)

@Serializable
private data class ChatChoice(val message: ChatMsg)

@Serializable
private data class ChatResp(val choices: List<ChatChoice> = emptyList())

/**
 * Клиент AI-объяснения через RouterAI (OpenAI-совместимый API).
 * Конфиг берётся из BuildConfig (значения из local.properties):
 * AI_BASE_URL — адрес прокси или https://routerai.ru/api/v1;
 * AI_API_KEY — ключ (только при прямом обращении; при прокси пустой);
 * AI_MODEL — deepseek/deepseek-v4-flash.
 */
object AiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(75, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    /** Настроено ли: есть базовый URL и либо ключ, либо это прокси (не сам RouterAI). */
    val isConfigured: Boolean
        get() = BuildConfig.AI_BASE_URL.isNotBlank() &&
            (BuildConfig.AI_API_KEY.isNotBlank() || !BuildConfig.AI_BASE_URL.contains("routerai.ru"))

    suspend fun explain(statement: String, solution: String, grade: Int?): String =
        withContext(Dispatchers.IO) {
            val base = BuildConfig.AI_BASE_URL.trimEnd('/')
            val system = "Ты — доброжелательный репетитор по олимпиадной математике. " +
                "Тебе дают условие задачи и ЭТАЛОННОЕ решение. Объясни это решение ученику " +
                "простыми словами, пошагово, сохраняя математику эталона без изменений. " +
                "Пиши по-русски. Формулы оформляй в LaTeX: строчные в \$...\$, выносные в \$\$...\$\$. " +
                "Будь кратким и понятным."
            val gradeLine = grade?.let { "Класс ученика: $it.\n\n" } ?: ""
            val user = gradeLine +
                "Условие задачи:\n$statement\n\nЭталонное решение:\n$solution\n\nОбъясни это решение пошагово."

            val reqBody = ChatReq(
                model = BuildConfig.AI_MODEL,
                messages = listOf(ChatMsg("system", system), ChatMsg("user", user))
            )
            val payload = json.encodeToString(ChatReq.serializer(), reqBody)

            val builder = Request.Builder()
                .url("$base/chat/completions")
                .post(payload.toRequestBody(jsonMedia))
                .addHeader("Content-Type", "application/json")
            if (BuildConfig.AI_API_KEY.isNotBlank()) {
                builder.addHeader("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
            }

            client.newCall(builder.build()).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) error("HTTP ${resp.code}: ${text.take(200)}")
                val parsed = json.decodeFromString(ChatResp.serializer(), text)
                parsed.choices.firstOrNull()?.message?.content?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: error("Пустой ответ модели")
            }
        }
}
