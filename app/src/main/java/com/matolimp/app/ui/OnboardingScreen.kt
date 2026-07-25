package com.matolimp.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matolimp.app.App
import kotlinx.coroutines.launch

private val EXPERIENCE = listOf(
    "Новичок",
    "Школьные олимпиады",
    "Муниципальный / региональный этап",
    "Призёр региона и выше"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val repo = App.repo(LocalContext.current)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf<Int?>(null) }
    var experience by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Добро пожаловать в МатОлимп!",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Пара вопросов, чтобы подобрать сложность.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Как тебя зовут? (необязательно)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("В каком ты классе?", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (5..11).forEach { g ->
                FilterChip(
                    selected = grade == g,
                    onClick = { grade = g },
                    label = { Text("$g") }
                )
            }
        }

        Text("Опыт в олимпиадах?", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EXPERIENCE.forEach { exp ->
                FilterChip(
                    selected = experience == exp,
                    onClick = { experience = exp },
                    label = { Text(exp) }
                )
            }
        }

        Button(
            onClick = {
                scope.launch {
                    repo.saveOnboarding(name, grade, experience)
                    onDone()
                }
            },
            enabled = grade != null && experience != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать")
        }
    }
}
