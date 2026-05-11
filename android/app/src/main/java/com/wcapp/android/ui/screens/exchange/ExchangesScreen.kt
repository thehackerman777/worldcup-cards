package com.wcapp.android.ui.screens.exchange
import org.koin.java.KoinJavaComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangesScreen(
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = KoinJavaComponent.get(ExchangeViewModel::class.java)
    val uiState = viewModel.uiState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intercambios") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Mis Intercambios") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Disponibles (${uiState.availableExchanges.size})") }
                )
            }

            Spacer(Modifier.height(8.dp))

            when (uiState.selectedTab) {
                0 -> MyExchangesTab(
                    exchanges = uiState.myExchanges,
                    isLoading = uiState.isLoading,
                    onExchangeClick = onNavigateToDetail
                )
                1 -> AvailableExchangesTab(
                    exchanges = uiState.availableExchanges,
                    isLoading = uiState.isLoading,
                    onExchangeClick = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
private fun MyExchangesTab(
    exchanges: List<com.wcapp.android.data.remote.ExchangeResponse>,
    isLoading: Boolean,
    onExchangeClick: (String) -> Unit
) {
    if (isLoading && exchanges.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (exchanges.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No tienes intercambios aún")
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exchanges) { exchange ->
            ExchangeCard(exchange = exchange, onClick = { onExchangeClick(exchange.id) })
        }
    }
}

@Composable
private fun AvailableExchangesTab(
    exchanges: List<com.wcapp.android.data.remote.ExchangeResponse>,
    isLoading: Boolean,
    onExchangeClick: (String) -> Unit
) {
    if (isLoading && exchanges.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (exchanges.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No hay intercambios disponibles")
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exchanges) { exchange ->
            ExchangeCard(exchange = exchange, onClick = { onExchangeClick(exchange.id) })
        }
    }
}

@Composable
private fun ExchangeCard(
    exchange: com.wcapp.android.data.remote.ExchangeResponse,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exchange.requester.displayName ?: exchange.requester.username} ↔ ${exchange.receiver.displayName ?: exchange.receiver.username}",
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(exchange.status)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${exchange.offeredCards.size} cartas ofrecidas • ${exchange.requestedCards.size} cartas solicitadas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
