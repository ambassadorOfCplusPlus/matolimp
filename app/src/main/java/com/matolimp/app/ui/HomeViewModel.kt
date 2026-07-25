package com.matolimp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matolimp.app.data.ProblemEntity
import com.matolimp.app.data.Repository
import com.matolimp.app.data.SubthemeEntity
import com.matolimp.app.data.ThemeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProblemRow(val problem: ProblemEntity, val status: String, val earnedXp: Int)
data class Section(val subtheme: SubthemeEntity, val problems: List<ProblemRow>)
data class ThemeCardUi(val theme: ThemeEntity, val solved: Int, val total: Int, val locked: Boolean)

data class CourseUi(
    val loading: Boolean = true,
    val name: String? = null,
    val grade: Int? = null,
    val experience: String? = null,
    val level: Int = 1,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val solvedTotal: Int = 0,
    val problemsTotal: Int = 0,
    val themes: List<ThemeCardUi> = emptyList()
)

/** Карта курса: список тем со статусами (свободная / платная-заблокирована). */
class CourseViewModel(repo: Repository) : ViewModel() {

    private val profileAndPurchases =
        repo.profile.combine(repo.purchases) { p, pur -> p to pur }

    val ui: StateFlow<CourseUi> = combine(
        repo.themes,
        repo.allSubthemes,
        repo.allProblems,
        repo.progress,
        profileAndPurchases
    ) { themes, subs, probs, progress, pp ->
        val profile = pp.first
        val purchases = pp.second
        val progressMap = progress.associateBy { it.problemId }
        val solvedTotal = probs.count { progressMap[it.id]?.status == "SOLVED" }

        val subToTheme = subs.associate { it.id to it.themeId }
        val probsByTheme = probs.groupBy { subToTheme[it.subthemeId] }

        val cards = themes.sortedBy { it.order }.map { theme ->
            val themeProbs = probsByTheme[theme.id].orEmpty()
            val solved = themeProbs.count { progressMap[it.id]?.status == "SOLVED" }
            val locked = !theme.isFree && !purchases.contains(theme.id)
            ThemeCardUi(theme, solved, themeProbs.size, locked)
        }

        CourseUi(
            loading = false,
            name = profile?.name,
            grade = profile?.grade,
            experience = profile?.experience,
            level = profile?.level ?: 1,
            totalXp = profile?.totalXp ?: 0,
            streakDays = profile?.streakDays ?: 0,
            solvedTotal = solvedTotal,
            problemsTotal = probs.size,
            themes = cards
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourseUi())
}

data class ThemeUi(
    val loading: Boolean = true,
    val title: String = "",
    val theory: String = "",
    val sections: List<Section> = emptyList()
)

/** Содержимое одной темы: теория + подтемы с задачами. */
class ThemeViewModel(repo: Repository, private val themeId: String) : ViewModel() {

    val ui: StateFlow<ThemeUi> = combine(
        repo.themes,
        repo.allSubthemes,
        repo.allProblems,
        repo.progress
    ) { themes, subs, probs, progress ->
        val theme = themes.firstOrNull { it.id == themeId } ?: return@combine ThemeUi(loading = true)
        val progressMap = progress.associateBy { it.problemId }
        val sections = subs.filter { it.themeId == themeId }.sortedBy { it.order }.map { sub ->
            val rows = probs.filter { it.subthemeId == sub.id }.sortedBy { it.order }.map { p ->
                val pr = progressMap[p.id]
                ProblemRow(p, pr?.status ?: "NOT_STARTED", pr?.earnedXp ?: 0)
            }
            Section(sub, rows)
        }
        ThemeUi(false, theme.title, theme.theory, sections)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeUi())
}
