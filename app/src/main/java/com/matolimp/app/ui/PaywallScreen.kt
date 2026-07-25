package com.matolimp.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.matolimp.app.App
import kotlinx.coroutines.launch

private val BENEFITS = listOf(
    "Полный доступ ко всем подтемам и задачам темы",
    "Эталонные решения и разборы",
    "Прогресс и XP сохраняются навсегда"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(themeId: String, onPurchased: () -> Unit, onBack: () -> Unit) {
    val repo = App.repo(LocalContext.current)
    val scope = rememberCoroutineScope()
    var buying by remember { mutableStateOf(false) }

    val title by produceState(initialValue = "", themeId) {
        value = repo.theme(themeId)?.title ?: "Курс"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Покупка курса") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Разблокируй тему целиком", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BENEFITS.forEach { b ->
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
                            Text(b, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Text("500 ₽", style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)

            Button(
                onClick = {
                    buying = true
                    scope.launch {
                        // TODO: заменить на реальный RuStore Billing (purchaseProduct + валидация чека).
                        val ok = runCatching { repo.purchase(themeId) }.isSuccess
                        if (ok) onPurchased() else buying = false
                    }
                },
                enabled = !buying,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (buying) "Обработка…" else "Купить за 500 ₽")
            }
            Text("Оплата пока в тестовом режиме (заглушка). Реальная оплата — через RuStore.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}
