package com.wcapp.android.ui.screens.cards
import org.koin.java.KoinJavaComponent

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.onSuccess
import com.wcapp.android.data.remote.onFailure
import com.wcapp.android.data.remote.CardResponse
import com.wcapp.android.ui.theme.RarityColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit
) {
    val apiService: ApiService = KoinJavaComponent.get(ApiService::class.java)
    var card by remember { mutableStateOf<CardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cardId) {
        scope.launch {
            apiService.getCard(cardId).onFailure { e -> error = e.message }
            val result = apiService.getCard(cardId)
            if (result is com.wcapp.android.data.remote.ApiResult.Success) {
                card = result.data
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Carta") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                card != null -> CardDetailContent(card!!)
                else -> Text("Carta no encontrada")
            }
        }
    }
}

@Composable
private fun CardDetailContent(card: CardResponse) {
    val rarityColor = RarityColors.forRarity(card.rarity)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card number with rarity color
        Surface(
            color = rarityColor,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#${card.cardNumber}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Name
        Text(
            text = card.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        // Team
        Text(
            text = card.team,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Info cards
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Posición", card.position ?: "N/A")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow("Rareza", rarityLabel(card.rarity))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow("Edición", card.edition)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow("Año", card.year.toString())
            }
        }

        Spacer(Modifier.height(16.dp))

        // Description
        card.description?.let { desc ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(desc, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun rarityLabel(rarity: String): String = when (rarity.uppercase()) {
    "COMMON" -> "⭐ Común"
    "UNCOMMON" -> "⭐⭐ Poco Común"
    "RARE" -> "⭐⭐⭐ Rara"
    "LEGENDARY" -> "⭐⭐⭐⭐⭐ Legendaria"
    else -> rarity
}
