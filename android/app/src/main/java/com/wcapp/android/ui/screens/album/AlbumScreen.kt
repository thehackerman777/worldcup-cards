package com.wcapp.android.ui.screens.album
import org.koin.java.KoinJavaComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wcapp.android.ui.theme.RarityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    onNavigateToCardDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = KoinJavaComponent.get(AlbumViewModel::class.java)
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Álbum") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Álbum") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Repetidas (${uiState.repeatedCards.size})") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.selectedTab) {
                0 -> AlbumContent(
                    uiState = uiState,
                    onCardClick = onNavigateToCardDetail
                )
                1 -> RepeatedContent(
                    repeatedCards = uiState.repeatedCards,
                    onCardClick = onNavigateToCardDetail
                )
            }
        }
    }
}

@Composable
private fun AlbumContent(
    uiState: AlbumUiState,
    onCardClick: (String) -> Unit
) {
    val album = uiState.album

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (album == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error al cargar el álbum", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Progreso del Álbum",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (album.completionPercentage / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${album.albumCards} / ${album.totalCards} cartas (${String.format("%.1f", album.completionPercentage)}%)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Cards grid
        if (album.cards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Album, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Tu álbum está vacío", style = MaterialTheme.typography.bodyLarge)
                    Text("Agrega cartas desde el catálogo", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(album.cards) { userCard ->
                    Card(
                        onClick = { onCardClick(userCard.card.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = userCard.card.cardNumber.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = RarityColors.forRarity(userCard.card.rarity)
                            )
                            Text(
                                text = userCard.card.name,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            if (userCard.quantity > 1) {
                                Text(
                                    text = "x${userCard.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepeatedContent(
    repeatedCards: List<com.wcapp.android.data.remote.UserCardResponse>,
    onCardClick: (String) -> Unit
) {
    if (repeatedCards.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No tienes cartas repetidas")
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(repeatedCards) { userCard ->
                Card(
                    onClick = { onCardClick(userCard.card.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = userCard.card.cardNumber.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = RarityColors.forRarity(userCard.card.rarity)
                        )
                        Text(
                            text = userCard.card.name,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Text(
                            text = "x${userCard.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        if (userCard.tradeable) {
                            Text("🔄", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
