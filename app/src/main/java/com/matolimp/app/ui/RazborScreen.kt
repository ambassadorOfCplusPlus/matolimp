package com.matolimp.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matolimp.app.App

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RazborContent(padding: PaddingValues, onOpenProblem: (String) -> Unit) {
    val repo = App.repo(LocalContext.current)
    val vm: RazborViewModel = viewModel(factory = razborVmFactory(repo))
    val ui by vm.ui.collectAsState()

    if (ui.stages.isEmpty()) {
        Text(
            if (ui.loading) "Загрузка…" else "Материалы разбора скоро появятся.",
            modifier = Modifier.padding(padding).padding(16.dp)
        )
        return
    }

    var stageIdx by remember(ui.stages.size) { mutableIntStateOf(0) }
    val stage = ui.stages.getOrElse(stageIdx) { ui.stages.first() }
    var yearIdx by remember(stage.id) { mutableIntStateOf(0) }
    val year = stage.years.getOrElse(yearIdx) { stage.years.first() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Выбери этап и год — каждая задача с разбором и приёмом (паттерном), которым решается.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Text("Этап", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.stages.forEachIndexed { i, st ->
                    FilterChip(
                        selected = i == stageIdx,
                        onClick = { stageIdx = i },
                        label = { Text(st.title) }
                    )
                }
            }
        }
        item {
            Text("Год", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stage.years.forEachIndexed { i, y ->
                    FilterChip(
                        selected = i == yearIdx,
                        onClick = { yearIdx = i },
                        label = { Text(y.year) }
                    )
                }
            }
        }
        item {
            Text(
                "${stage.title} этап · ${year.year} · задач: ${year.problems.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(year.problems, key = { it.id }) { p ->
            RazborProblemCard(p) { onOpenProblem(p.id) }
        }
    }
}

@Composable
private fun RazborProblemCard(p: RazborProblem, onClick: () -> Unit) {
    val solved = p.status == "SOLVED"
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                p.pattern?.let { pat ->
                    AssistChip(
                        onClick = onClick,
                        label = { Text(pat, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    "сложность ${p.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (solved) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null,
                        tint = Color(0xFF00A36C), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MathText(
                    text = p.statement,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
