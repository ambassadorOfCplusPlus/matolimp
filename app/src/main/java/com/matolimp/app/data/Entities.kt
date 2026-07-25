package com.matolimp.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Большая тема с основной теорией. Первая тема бесплатна (isFree). */
@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val theory: String,
    val order: Int,
    val isFree: Boolean,
    val track: String = "course"   // course | prep (подготовка к этапам)
)

/** Подтема с короткой теорией. */
@Entity(tableName = "subthemes")
data class SubthemeEntity(
    @PrimaryKey val id: String,
    val themeId: String,
    val title: String,
    val theory: String,
    val order: Int
)

/**
 * Задача. kind = CLOSED (числовой ответ, авто-проверка) | OPEN (пишет решение, самооценка).
 * answerSpec — JSON правил проверки, только для CLOSED.
 */
@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey val id: String,
    val subthemeId: String,
    val order: Int,
    val kind: String,
    val statement: String,
    val answerSpec: String?,
    val referenceSolution: String,
    val difficulty: Int,
    val pattern: String? = null   // приём/тема для «Разбора олимпиады»
)

/** Прогресс по задаче (локально). */
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val problemId: String,
    val attemptsUsed: Int = 0,
    val status: String = "NOT_STARTED",
    val solvedAtAttempt: Int? = null,
    val earnedXp: Int = 0,
    val draftSolution: String? = null,
    val selfAssessment: String? = null
)

/** Кэш AI-объяснения по задаче (запрашивается один раз). */
@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey val problemId: String,
    val text: String
)

/** Факт покупки темы (курса). */
@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val themeId: String,
    val purchasedAt: String? = null
)

/** Профиль пользователя (одна строка). */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val name: String? = null,
    val grade: Int? = null,
    val experience: String? = null,
    val onboarded: Boolean = false,
    val totalXp: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 0,
    val lastActiveDate: String? = null
)
