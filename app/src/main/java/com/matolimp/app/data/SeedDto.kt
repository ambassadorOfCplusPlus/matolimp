package com.matolimp.app.data

import kotlinx.serialization.Serializable

/** DTO для парсинга assets/seed.json. */
@Serializable
data class SeedRoot(val themes: List<SeedTheme>)

@Serializable
data class SeedTheme(
    val id: String,
    val title: String,
    val order: Int,
    val isFree: Boolean = false,
    val track: String = "course",
    val theory: String,
    val subthemes: List<SeedSubtheme>
)

@Serializable
data class SeedSubtheme(
    val id: String,
    val title: String,
    val order: Int,
    val theory: String,
    val problems: List<SeedProblem>
)

@Serializable
data class SeedProblem(
    val id: String,
    val order: Int,
    val kind: String,              // CLOSED | OPEN
    val statement: String,
    val answer: SeedAnswer? = null, // только для CLOSED
    val solution: String,
    val difficulty: Int = 1,
    val pattern: String? = null
)

@Serializable
data class SeedAnswer(
    val type: String,                     // INTEGER | RATIONAL | DECIMAL | SET | STRING
    val accepted: List<String>,
    val tolerance: Double = 1e-9,
    val orderMatters: Boolean = false,
    val caseSensitive: Boolean = false
)
