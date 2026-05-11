package com.wcapp.android.ui.screens.exchange
import org.koin.java.KoinJavaComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.ExchangeResponse
import com.wcapp.android.ui.theme.RarityColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeDetailScreen(
    exchangeId: String,
    onBack: () -> Unit
) {
    val apiService = KoinJavaComponent.get(ApiService::class.java)
    val viewModel = KoinJavaComponent.get(ExchangeViewModel::class.java)
    val uiState = viewModel.uiState.collectAsState().value
    val scope = rememberCoroutineScope()

    var exchange by remember { mutableStateOf<ExchangeResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(exchangeId) {
        scope.launch {
            apiService.getExchanges().onSuccess { response ->
                exchange = response.exchanges.find { it.id == exchangeId }
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Intercambio") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                exchange == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Intercambio no encontrado")
                }
                else -> ExchangeDetailContent(exchange!!, viewModel, uiState)
            }
        }
    }
}

@Composable
private fun ExchangeDetailContent(
    exchange: ExchangeResponse,
    viewModel: ExchangeViewModel,
    uiState: com.wcapp.android.ui.screens.exchange.ExchangeUiState
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Participants
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Participantes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ParticipantRow("Solicitante", exchange.requester.displayName ?: exchange.requester.username)
                Spacer(Modifier.height(4.dp))
                ParticipantRow("Destinatario", exchange.receiver.displayName ?: exchange.receiver.username)
            }
        }

        // Status
        StatusChip(exchange.status)

        // Message
        exchange.message?.let { msg ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mensaje", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(msg, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Offered cards
        Text("Ofrecidas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(exchange.offeredCards) { item ->
                Card {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("#${item.card.cardNumber}", fontWeight = FontWeight.Bold,
                            color = RarityColors.forRarity(item.card.rarity))
                        Text(item.card.name, style = MaterialTheme.typography.labelSmall)
                        Text("x${item.quantity}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Requested cards
        Text("Solicitadas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(exchange.requestedCards) { item ->
                Card {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("#${item.card.cardNumber}", fontWeight = FontWeight.Bold,
                            color = RarityColors.forRarity(item.card.rarity))
                        Text(item.card.name, style = MaterialTheme.typography.labelSmall)
                        Text("x${item.quantity}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Action buttons based on status
        when (exchange.status.uppercase()) {
            "PENDING" -> {
                Button(
                    onClick = { viewModel.acceptExchange(exchange.id) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Aceptar Intercambio") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.rejectExchange(exchange.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Rechazar") }
            }
            "ACCEPTED" -> {
                Button(
                    onClick = { viewModel.completeExchange(exchange.id) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Completar Intercambio") }
            }
        }
    }
}

@Composable
private fun ParticipantRow(label: String, name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(name, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, label) = when (status.uppercase()) {
        "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer to "Pendiente"
        "ACCEPTED" -> MaterialTheme.colorScheme.primaryContainer to "Aceptado"
        "COMPLETED" -> MaterialTheme.colorScheme.secondaryContainer to "Completado"
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer to "Rechazado"
        "CANCELLED" -> MaterialTheme.colorScheme.surfaceVariant to "Cancelado"
        else -> MaterialTheme.colorScheme.surfaceVariant to status
    }

    Surface(color = color, shape = MaterialTheme.shapes.medium) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
