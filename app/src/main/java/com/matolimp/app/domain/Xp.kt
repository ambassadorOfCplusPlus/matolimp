package com.matolimp.app.domain

/** Правила начисления XP (см. дизайн §6). */
object Xp {
    /** Закрытая задача: база по номеру попытки (1..3) + бонус за сложность. */
    fun forClosed(attempt: Int, difficulty: Int): Int {
        val base = when (attempt) {
            1 -> 10
            2 -> 6
            3 -> 3
            else -> 0
        }
        if (base == 0) return 0
        return base + (difficulty - 1) * 2
    }

    /** Открытая задача: мягкий XP по самооценке. */
    fun forOpen(selfAssessment: String): Int = when (selfAssessment) {
        "SOLVED" -> 8
        "PARTIAL" -> 4
        "FAILED" -> 1
        else -> 0
    }

    /** Максимум попыток для закрытой задачи. */
    const val MAX_ATTEMPTS = 3

    fun levelFor(totalXp: Int): Int = 1 + totalXp / 100
}
