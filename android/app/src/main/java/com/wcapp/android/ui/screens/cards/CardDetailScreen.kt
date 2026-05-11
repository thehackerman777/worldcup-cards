package com.wcapp.android.ui.screens.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.CardResponse
import com.wcapp.android.ui.theme.RarityColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(cardId: String, onBack: () -> Unit) {
    val apiService = remember { org.koin.java.KoinJavaComponent.get(ApiService::class.java) }
    var card by remember { mutableStateOf<CardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cardId) {
        scope.launch {
            isLoading = true
            try {
                card = apiService.getCard(cardId)
            } catch (e: Exception) {
                error = e.message
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
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = rarityColor, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text("#${card.cardNumber}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(card.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(card.team, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Posición", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(card.position ?: "N/A", fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rareza", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(when(card.rarity){"COMMON"->"⭐ Común";"UNCOMMON"->"⭐⭐ Poco Común";"RARE"->"⭐⭐⭐ Rara";"LEGENDARY"->"⭐⭐⭐⭐⭐ Legendaria";else->card.rarity}, fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Edición", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(card.edition, fontWeight = FontWeight.Medium)
                }
            }
        }
        card.description?.let { desc ->
            Spacer(Modifier.height(16.dp))
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
