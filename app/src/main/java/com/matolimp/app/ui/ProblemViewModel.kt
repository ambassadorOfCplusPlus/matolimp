package com.matolimp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matolimp.app.data.ProblemEntity
import com.matolimp.app.data.Repository
import com.matolimp.app.domain.Xp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProblemViewModel(
    private val repo: Repository,
    private val problemId: String
) : ViewModel() {

    data class State(
        val loading: Boolean = true,
        val problem: ProblemEntity? = null,
        val status: String = "NOT_STARTED",
        val attemptsLeft: Int = Xp.MAX_ATTEMPTS,
        val input: String = "",
        val draft: String = "",
        val feedback: String? = null,
        val feedbackPositive: Boolean = false,
        val showSolution: Boolean = false,
        val solved: Boolean = false,
        val locked: Boolean = false,
        val submitted: Boolean = false,
        val submitting: Boolean = false,
        val aiLoading: Boolean = false,
        val aiText: String? = null,
        val aiError: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        val p = repo.problem(problemId)
        val pr = repo.progressFor(problemId)
        val status = pr?.status ?: "NOT_STARTED"
        _state.update {
            it.copy(
                loading = false,
                problem = p,
                status = status,
                attemptsLeft = (Xp.MAX_ATTEMPTS - (pr?.attemptsUsed ?: 0)).coerceAtLeast(0),
                draft = pr?.draftSolution ?: "",
                solved = status == "SOLVED",
                locked = status == "LOCKED",
                submitted = status == "SUBMITTED",
                showSolution = status == "SOLVED" || status == "LOCKED" || status == "SUBMITTED"
            )
        }
    }

    fun onInput(s: String) = _state.update { it.copy(input = s) }
    fun onDraft(s: String) = _state.update { it.copy(draft = s) }

    fun submitClosed() = viewModelScope.launch {
        val p = _state.value.problem ?: return@launch
        // Защита от повторного/двойного нажатия «Проверить» до завершения проверки:
        // иначе две корутины прочитают одинаковый attemptsUsed и спишут одну попытку вместо двух.
        if (_state.value.submitting) return@launch
        if (_state.value.input.isBlank()) {
            _state.update { it.copy(feedback = "Введите ответ", feedbackPositive = false) }
            return@launch
        }
        _state.update { it.copy(submitting = true) }
        val res = repo.submitClosed(p, _state.value.input)
        _state.update {
            it.copy(
                submitting = false,
                attemptsLeft = res.attemptsLeft,
                solved = res.correct,
                locked = res.locked,
                status = when { res.correct -> "SOLVED"; res.locked -> "LOCKED"; else -> "IN_PROGRESS" },
                showSolution = res.correct || res.locked,
                feedbackPositive = res.correct,
                feedback = when {
                    res.correct -> "Верно! +${res.xpEarned} XP"
                    res.locked -> "Попытки закончились. Ниже — эталонное решение."
                    else -> "Неверно. Осталось попыток: ${res.attemptsLeft}"
                }
            )
        }
    }

    /** Открытая задача: показать эталон, не начисляя балл (балл — при самооценке). */
    fun revealSolution() = viewModelScope.launch {
        if (_state.value.draft.isNotBlank()) repo.saveDraft(problemId, _state.value.draft)
        _state.update { it.copy(showSolution = true) }
    }

    fun submitOpen(assessment: String) = viewModelScope.launch {
        val p = _state.value.problem ?: return@launch
        if (_state.value.submitted) return@launch  // самооценка фиксируется один раз
        val xp = repo.submitOpen(p, _state.value.draft, assessment)
        _state.update {
            it.copy(
                status = "SUBMITTED",
                submitted = true,
                showSolution = true,
                feedbackPositive = true,
                feedback = if (xp > 0) "Засчитано: +$xp XP" else "Оценка обновлена"
            )
        }
    }

    fun saveDraft() = viewModelScope.launch {
        if (_state.value.draft.isNotBlank()) repo.saveDraft(problemId, _state.value.draft)
    }

    /** Запрос AI-объяснения эталона (с кэшем в БД). */
    fun explain() = viewModelScope.launch {
        val p = _state.value.problem ?: return@launch
        if (_state.value.aiLoading || _state.value.aiText != null) return@launch
        _state.update { it.copy(aiLoading = true, aiError = null) }
        val result = repo.explain(p)
        _state.update {
            it.copy(
                aiLoading = false,
                aiText = result.getOrNull(),
                aiError = if (result.isFailure) "Не удалось получить объяснение. Попробуйте ещё раз." else null
            )
        }
    }
}
