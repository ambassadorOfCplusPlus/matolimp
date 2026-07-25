package com.matolimp.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matolimp.app.App

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(themeId: String, onOpenProblem: (String) -> Unit, onBack: () -> Unit) {
    val repo = App.repo(LocalContext.current)
    val vm: ThemeViewModel = viewModel(factory = themeVmFactory(repo, themeId), key = "theme-$themeId")
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui.title.ifEmpty { "Тема" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.theory.isNotBlank()) {
                item { TheoryCard(ui.theory) }
            }
            ui.sections.forEach { section ->
                item {
                    Text(
                        section.subtheme.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (section.subtheme.theory.isNotBlank()) {
                    item { TheoryCard(section.subtheme.theory) }
                }
                items(section.problems, key = { it.problem.id }) { row ->
                    ProblemCard(row) { onOpenProblem(row.problem.id) }
                }
            }
        }
    }
}

@Composable
fun TheoryCard(text: String) {
    var expanded by remember { mutableStateOf(false) }
    val preview = remember(text) { plainPreview(text) }
    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.padding(16.dp)) {
            if (expanded) {
                MathText(text = text, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(text = preview, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                if (expanded) "Свернуть" else "Читать теорию",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProblemCard(row: ProblemRow, onClick: () -> Unit) {
    val (icon, tint) = statusIcon(row.status)
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                MathText(text = row.problem.statement, style = MaterialTheme.typography.bodyLarge)
                Text(
                    kindLabel(row.problem.kind) + " · сложность " + row.problem.difficulty +
                        if (row.earnedXp > 0) " · +${row.earnedXp} XP" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

private fun kindLabel(kind: String) = if (kind == "OPEN") "Задача с решением" else "Числовой ответ"

private fun statusIcon(status: String): Pair<ImageVector, Color> = when (status) {
    "SOLVED" -> Icons.Filled.CheckCircle to Color(0xFF00A36C)
    "LOCKED" -> Icons.Filled.Lock to Color(0xFFB00020)
    "SUBMITTED" -> Icons.Filled.EditNote to Color(0xFF2E5BFF)
    else -> Icons.Outlined.Circle to Color(0xFF9E9E9E)
}

/** Чистое текстовое превью теории: убираем выносные формулы и маркеры $, схлопываем пробелы. */
private fun plainPreview(text: String, max: Int = 110): String {
    val noFig = text.replace(Regex("""\[\[fig:[a-z0-9_]+\]\]"""), " ")
    val noDisplay = noFig.replace(Regex("""\$\$.+?\$\$""", RegexOption.DOT_MATCHES_ALL), " ")
    val noInline = noDisplay.replace(Regex("""\$(.+?)\$""", RegexOption.DOT_MATCHES_ALL), "$1")
    val flat = noInline.replace(Regex("""\s+"""), " ").trim()
    return if (flat.length > max) flat.take(max).trimEnd() + "…" else flat
}
