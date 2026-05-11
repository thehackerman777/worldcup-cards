package com.wcapp.android.ui.screens.exchange

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wcapp.android.data.remote.*
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExchangeScreen(
    receiverId: String,
    onExchangeCreated: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel = get(ExchangeViewModel::class.java)
    val uiState by viewModel.uiState.collectAsState()

    var message by remember { mutableStateOf("") }
    // Simplified: in a real app you'd select cards from a list
    var offeredCardId by remember { mutableStateOf("") }
    var requestedCardId by remember { mutableStateOf("") }
    var offeredQty by remember { mutableIntStateOf(1) }
    var requestedQty by remember { mutableIntStateOf(1) }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess != null) onExchangeCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Intercambio") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Crear solicitud de intercambio",
                style = MaterialTheme.typography.titleLarge
            )

            // Error
            uiState.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(err, modifier = Modifier.padding(16.dp))
                }
            }

            // Message
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Mensaje (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Offered cards section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cartas que ofreces",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = offeredCardId,
                        onValueChange = { offeredCardId = it },
                        label = { Text("ID de la carta (ofrecida)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = offeredQty.toString(),
                        onValueChange = { offeredQty = it.toIntOrNull() ?: 1 },
                        label = { Text("Cantidad") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                }
            }

            // Requested cards section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cartas que solicitas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = requestedCardId,
                        onValueChange = { requestedCardId = it },
                        label = { Text("ID de la carta (solicitada)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = requestedQty.toString(),
                        onValueChange = { requestedQty = it.toIntOrNull() ?: 1 },
                        label = { Text("Cantidad") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (offeredCardId.isNotBlank() && requestedCardId.isNotBlank()) {
                        viewModel.createExchange(
                            CreateExchangeRequest(
                                receiverId = receiverId,
                                message = message.ifBlank { null },
                                offeredCards = listOf(ExchangeCardEntry(offeredCardId, offeredQty)),
                                requestedCards = listOf(ExchangeCardEntry(requestedCardId, requestedQty))
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = offeredCardId.isNotBlank() && requestedCardId.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar Solicitud")
                }
            }
        }
    }
}
