package com.matolimp.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matolimp.app.App

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemScreen(problemId: String, onBack: () -> Unit) {
    val repo = App.repo(LocalContext.current)
    val vm: ProblemViewModel = viewModel(
        factory = problemVmFactory(repo, problemId),
        key = "problem-$problemId"
    )
    val s by vm.state.collectAsState()
    val problem = s.problem

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задача") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (problem == null) {
            Text(
                if (s.loading) "Загрузка…" else "Задача не найдена",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Условие
            Card {
                MathText(
                    text = problem.statement,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Паттерн (приём) — для рубрики «Разбор олимпиады»
            problem.pattern?.let { pat ->
                androidx.compose.material3.AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Приём: $pat") },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            // Обратная связь
            s.feedback?.let { fb ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (s.feedbackPositive) Color(0xFFDFF5EA) else Color(0xFFFDE7E9)
                    )
                ) {
                    Text(
                        fb,
                        modifier = Modifier.padding(14.dp),
                        color = if (s.feedbackPositive) Color(0xFF00794F) else Color(0xFFB00020),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (problem.kind == "OPEN") {
                OpenProblemBlock(s, vm)
            } else {
                ClosedProblemBlock(s, vm)
            }

            // Эталонное решение
            if (s.showSolution) {
                Text("Эталонное решение", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Card {
                    MathText(
                        text = problem.referenceSolution,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                AiExplanationBlock(s, vm)
            }
        }
    }
}

@Composable
private fun AiExplanationBlock(s: ProblemViewModel.State, vm: ProblemViewModel) {
    if (!com.matolimp.app.ai.AiClient.isConfigured) {
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("🤖 ИИ-объяснение пока не настроено")
        }
        return
    }
    when {
        s.aiText != null -> {
            Text("🤖 Объяснение от ИИ", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )) {
                MathText(
                    text = s.aiText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        s.aiLoading -> {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                )
                Text("ИИ разбирает решение…", style = MaterialTheme.typography.bodyMedium)
            }
        }
        else -> {
            OutlinedButton(onClick = vm::explain, modifier = Modifier.fillMaxWidth()) {
                Text("🤖 Объяснить решение (ИИ)")
            }
            s.aiError?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ClosedProblemBlock(s: ProblemViewModel.State, vm: ProblemViewModel) {
    val done = s.solved || s.locked
    if (!done) {
        OutlinedTextField(
            value = s.input,
            onValueChange = vm::onInput,
            label = { Text("Ваш ответ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Осталось попыток: ${s.attemptsLeft}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = vm::submitClosed,
            modifier = Modifier.fillMaxWidth(),
            enabled = s.attemptsLeft > 0 && !s.submitting
        ) {
            Text("Проверить")
        }
    }
}

@Composable
private fun OpenProblemBlock(s: ProblemViewModel.State, vm: ProblemViewModel) {
    OutlinedTextField(
        value = s.draft,
        onValueChange = vm::onDraft,
        label = { Text("Ваше решение") },
        minLines = 4,
        modifier = Modifier.fillMaxWidth()
    )
    FilledTonalButton(onClick = vm::saveDraft, modifier = Modifier.fillMaxWidth()) {
        Text("Сохранить черновик")
    }
    if (!s.showSolution) {
        Button(onClick = vm::revealSolution, modifier = Modifier.fillMaxWidth()) {
            Text("Сдать и посмотреть эталон")
        }
    }
    if (s.showSolution) {
        Text(
            if (s.submitted) "Самооценка засчитана." else "Самооценка после сравнения с эталоном:",
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssessBtn("Решил", "SOLVED", vm, Modifier.weight(1f), !s.submitted)
            AssessBtn("Частично", "PARTIAL", vm, Modifier.weight(1f), !s.submitted)
            AssessBtn("Не решил", "FAILED", vm, Modifier.weight(1f), !s.submitted)
        }
    }
    Spacer(Modifier.size(2.dp))
}

@Composable
private fun AssessBtn(label: String, code: String, vm: ProblemViewModel, modifier: Modifier, enabled: Boolean) {
    OutlinedButton(onClick = { vm.submitOpen(code) }, modifier = modifier, enabled = enabled) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
