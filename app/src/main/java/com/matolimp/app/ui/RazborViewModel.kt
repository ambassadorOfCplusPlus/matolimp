package com.matolimp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matolimp.app.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RazborProblem(
    val id: String,
    val statement: String,
    val pattern: String?,
    val status: String,
    val difficulty: Int
)

data class RazborYear(val year: String, val problems: List<RazborProblem>)
data class RazborStage(val id: String, val title: String, val years: List<RazborYear>)
data class RazborUi(val loading: Boolean = true, val stages: List<RazborStage> = emptyList())

/** Данные для рубрики «Разбор олимпиады»: этап → год → задачи с паттернами. */
class RazborViewModel(repo: Repository) : ViewModel() {

    private val yearRegex = Regex(".*-20\\d\\d")

    val ui: StateFlow<RazborUi> = combine(
        repo.themes,
        repo.allSubthemes,
        repo.allProblems,
        repo.progress
    ) { themes, subs, probs, progress ->
        val prog = progress.associateBy { it.problemId }
        val prep = themes.filter { it.track == "prep" }.sortedBy { it.order }

        val stages = prep.map { theme ->
            val years = subs.filter { it.themeId == theme.id && yearRegex.matches(it.id) }
                .sortedByDescending { it.title }
                .map { sub ->
                    val ps = probs.filter { it.subthemeId == sub.id }.sortedBy { it.order }.map { p ->
                        RazborProblem(
                            id = p.id,
                            statement = p.statement,
                            pattern = p.pattern,
                            status = prog[p.id]?.status ?: "NOT_STARTED",
                            difficulty = p.difficulty
                        )
                    }
                    RazborYear(sub.title, ps)
                }
            RazborStage(theme.id, stageTitle(theme.id), years)
        }.filter { it.years.isNotEmpty() }

        RazborUi(loading = false, stages = stages)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RazborUi())

    private fun stageTitle(id: String) = when (id) {
        "prep-muni" -> "Муниципальный"
        "prep-region" -> "Региональный"
        else -> id
    }
}
