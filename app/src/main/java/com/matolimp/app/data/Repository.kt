package com.matolimp.app.data

import android.content.Context
import com.matolimp.app.ai.AiClient
import com.matolimp.app.domain.AnswerChecker
import com.matolimp.app.domain.Xp
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class Repository(
    private val contentDao: ContentDao,
    private val progressDao: ProgressDao,
    private val appContext: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val themes: Flow<List<ThemeEntity>> = contentDao.themes()
    val allSubthemes: Flow<List<SubthemeEntity>> = contentDao.allSubthemes()
    val allProblems: Flow<List<ProblemEntity>> = contentDao.allProblems()
    val progress: Flow<List<ProgressEntity>> = progressDao.all()
    val profile: Flow<ProfileEntity?> = progressDao.profileFlow()
    val purchases: Flow<List<String>> = progressDao.purchasedThemeIds()

    fun subthemes(themeId: String): Flow<List<SubthemeEntity>> = contentDao.subthemes(themeId)
    fun problems(subthemeId: String): Flow<List<ProblemEntity>> = contentDao.problems(subthemeId)
    suspend fun problem(id: String): ProblemEntity? = contentDao.problem(id)
    suspend fun theme(id: String): ThemeEntity? = contentDao.theme(id)
    suspend fun progressFor(id: String): ProgressEntity? = progressDao.get(id)

    /**
     * Сид/обновление контента из assets/seed.json.
     *
     * Версия контента (CONTENT_VERSION) разведена с версией схемы Room: при обновлении
     * задач/теории поднимаем ТОЛЬКО CONTENT_VERSION — тогда пересеваются лишь таблицы
     * контента (темы/подтемы/задачи), а прогресс, профиль, серии, покупки и AI-кэш
     * остаются нетронутыми. Версию схемы Room бампим лишь при реальной смене схемы.
     */
    suspend fun seedIfNeeded() {
        if (progressDao.profile() == null) progressDao.upsertProfile(ProfileEntity())

        val prefs = appContext.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE)
        val seededVersion = prefs.getInt(KEY_CONTENT_VERSION, 0)
        val empty = contentDao.themeCount() == 0
        if (!empty && seededVersion == CONTENT_VERSION) return

        val text = appContext.assets.open("seed.json").bufferedReader().use { it.readText() }
        val root = json.decodeFromString<SeedRoot>(text)

        val themes = mutableListOf<ThemeEntity>()
        val subs = mutableListOf<SubthemeEntity>()
        val probs = mutableListOf<ProblemEntity>()

        for (t in root.themes) {
            themes += ThemeEntity(t.id, t.title, t.theory, t.order, t.isFree, t.track)
            for (s in t.subthemes) {
                subs += SubthemeEntity(s.id, t.id, s.title, s.theory, s.order)
                for (p in s.problems) {
                    val answerSpec = p.answer?.let { json.encodeToString(SeedAnswer.serializer(), it) }
                    probs += ProblemEntity(
                        id = p.id,
                        subthemeId = s.id,
                        order = p.order,
                        kind = p.kind,
                        statement = p.statement,
                        answerSpec = answerSpec,
                        referenceSolution = p.solution,
                        difficulty = p.difficulty,
                        pattern = p.pattern
                    )
                }
            }
        }
        // Атомарная замена контента; прогресс/профиль/покупки в других таблицах не затрагиваются.
        contentDao.replaceContent(themes, subs, probs)
        prefs.edit().putInt(KEY_CONTENT_VERSION, CONTENT_VERSION).apply()
    }

    data class ClosedResult(
        val correct: Boolean,
        val attemptsLeft: Int,
        val locked: Boolean,
        val xpEarned: Int
    )

    /** Сдача ответа закрытой задачи. */
    suspend fun submitClosed(problem: ProblemEntity, input: String): ClosedResult {
        val current = progressDao.get(problem.id) ?: ProgressEntity(problemId = problem.id)
        if (current.status == "SOLVED")
            return ClosedResult(correct = true, attemptsLeft = 0, locked = false, xpEarned = 0)
        if (current.status == "LOCKED")
            return ClosedResult(correct = false, attemptsLeft = 0, locked = true, xpEarned = 0)

        val attempt = current.attemptsUsed + 1
        val correct = AnswerChecker.check(problem.answerSpec, input)

        if (correct) {
            val xp = Xp.forClosed(attempt, problem.difficulty)
            progressDao.upsert(
                current.copy(
                    attemptsUsed = attempt,
                    status = "SOLVED",
                    solvedAtAttempt = attempt,
                    earnedXp = xp
                )
            )
            addXp(xp)
            return ClosedResult(true, Xp.MAX_ATTEMPTS - attempt, false, xp)
        }

        val locked = attempt >= Xp.MAX_ATTEMPTS
        progressDao.upsert(
            current.copy(attemptsUsed = attempt, status = if (locked) "LOCKED" else "IN_PROGRESS")
        )
        return ClosedResult(false, Xp.MAX_ATTEMPTS - attempt, locked, 0)
    }

    /** Сдача открытой задачи с самооценкой (мягкий XP, начисляется один раз). */
    suspend fun submitOpen(problem: ProblemEntity, draft: String, assessment: String): Int {
        val current = progressDao.get(problem.id) ?: ProgressEntity(problemId = problem.id)
        // Самооценка фиксируется один раз: иначе можно менять оценку (SOLVED↔FAILED),
        // при этом earnedXp остаётся от первой оценки — рассинхрон. Первую оценку не переписываем.
        if (current.status == "SUBMITTED") return 0
        val xp = Xp.forOpen(assessment)
        progressDao.upsert(
            current.copy(
                status = "SUBMITTED",
                draftSolution = draft,
                selfAssessment = assessment,
                earnedXp = current.earnedXp + xp
            )
        )
        if (xp > 0) addXp(xp)
        return xp
    }

    suspend fun saveDraft(problemId: String, draft: String) {
        val current = progressDao.get(problemId) ?: ProgressEntity(problemId = problemId)
        val status = if (current.status == "NOT_STARTED") "IN_PROGRESS" else current.status
        progressDao.upsert(current.copy(draftSolution = draft, status = status))
    }

    suspend fun purchase(themeId: String) {
        progressDao.insertPurchase(PurchaseEntity(themeId, LocalDate.now().toString()))
    }

    /** AI-объяснение эталона: сначала кэш, иначе запрос к модели и сохранение. */
    suspend fun explain(problem: ProblemEntity): Result<String> {
        progressDao.aiExplanation(problem.id)?.let { return Result.success(it) }
        return runCatching {
            val grade = progressDao.profile()?.grade
            val text = AiClient.explain(problem.statement, problem.referenceSolution, grade)
            progressDao.insertAiExplanation(AiCacheEntity(problem.id, text))
            text
        }
    }

    suspend fun saveOnboarding(name: String?, grade: Int?, experience: String?) {
        val p = progressDao.profile() ?: ProfileEntity()
        progressDao.upsertProfile(
            p.copy(
                name = name?.trim()?.ifBlank { null },
                grade = grade,
                experience = experience,
                onboarded = true
            )
        )
    }

    companion object {
        /**
         * Версия контента (seed.json). Поднимай при КАЖДОМ обновлении задач/теории —
         * тогда контент пересеется, а прогресс/покупки сохранятся. Версию схемы Room
         * (AppDatabase.version) при этом НЕ трогай.
         */
        const val CONTENT_VERSION = 2
        private const val META_PREFS = "matolimp_meta"
        private const val KEY_CONTENT_VERSION = "content_version"
    }

    private suspend fun addXp(delta: Int) {
        val p = progressDao.profile() ?: ProfileEntity()
        val newXp = p.totalXp + delta
        val today = LocalDate.now()
        val last = p.lastActiveDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val newStreak = when {
            last == null -> 1
            last == today -> p.streakDays.coerceAtLeast(1)
            last == today.minusDays(1) -> p.streakDays + 1
            else -> 1
        }
        progressDao.upsertProfile(
            p.copy(
                totalXp = newXp,
                level = Xp.levelFor(newXp),
                streakDays = newStreak,
                lastActiveDate = today.toString()
            )
        )
    }
}
