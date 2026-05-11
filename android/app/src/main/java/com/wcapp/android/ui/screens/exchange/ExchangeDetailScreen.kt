package com.wcapp.android.ui.screens.exchange

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ExchangeDetailScreen(exchangeId: String, onBack: () -> Unit) {
    val apiService: ApiService = org.koin.java.KoinJavaComponent.get(ApiService::class.java)
    val viewModel: ExchangeViewModel = org.koin.java.KoinJavaComponent.get(ExchangeViewModel::class.java)
    val uiState = viewModel.uiState.value
    val scope = rememberCoroutineScope()
    var exchange by remember { mutableStateOf<ExchangeResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(exchangeId) {
        scope.launch {
            isLoading = true
            try { val r = apiService.getExchanges(); exchange = r.exchanges.find { it.id == exchangeId } } catch (_: Exception) {}
            isLoading = false
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Detalle") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when { isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                exchange == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No encontrado") }
                else -> ExchangeDetailContent(exchange!!, viewModel, uiState)
            }
        }
    }
}

@Composable
private fun ExchangeDetailContent(exchange: ExchangeResponse, viewModel: ExchangeViewModel, uiState: ExchangeUiState) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Participantes", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Solicitante"); Text(exchange.requester.displayName ?: exchange.requester.username, fontWeight = FontWeight.Medium) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Destinatario"); Text(exchange.receiver.displayName ?: exchange.receiver.username, fontWeight = FontWeight.Medium) } } }
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) { Text(exchange.status, modifier = Modifier.padding(16.dp)) }
        Text("Ofrecidas:", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(exchange.offeredCards) { Card(Modifier.padding(8.dp)) { Text("#${it.card.cardNumber}", color = RarityColors.forRarity(it.card.rarity), fontWeight = FontWeight.Bold); Text(it.card.name, style = MaterialTheme.typography.labelSmall); Text("x${it.quantity}") } } }
        Text("Solicitadas:", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(exchange.requestedCards) { Card(Modifier.padding(8.dp)) { Text("#${it.card.cardNumber}", color = RarityColors.forRarity(it.card.rarity), fontWeight = FontWeight.Bold); Text(it.card.name, style = MaterialTheme.typography.labelSmall); Text("x${it.quantity}") } } }
        Spacer(Modifier.weight(1f))
        when (exchange.status.uppercase()) { "PENDING" -> { Button(onClick = { viewModel.acceptExchange(exchange.id) }, modifier = Modifier.fillMaxWidth()) { Text("Aceptar") }; Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { viewModel.rejectExchange(exchange.id) }, modifier = Modifier.fillMaxWidth()) { Text("Rechazar") } }
            "ACCEPTED" -> { Button(onClick = { viewModel.completeExchange(exchange.id) }, modifier = Modifier.fillMaxWidth()) { Text("Completar") } } }
    }
}
