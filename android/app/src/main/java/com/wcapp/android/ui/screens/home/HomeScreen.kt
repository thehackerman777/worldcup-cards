package com.wcapp.android.ui.screens.home
import org.koin.java.KoinJavaComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun HomeScreen(
    onNavigateToAlbum: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToExchanges: () -> Unit,
    onNavigateToPanini: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = KoinJavaComponent.get(HomeViewModel::class.java)
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("World Cup Cards") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Configuración")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Album, "Álbum") },
                    label = { Text("Álbum") },
                    selected = false,
                    onClick = onNavigateToAlbum
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SwapHoriz, "Intercambios") },
                    label = { Text("Intercambios") },
                    selected = false,
                    onClick = onNavigateToExchanges
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcome
            Text(
                text = "¡Bienvenido, ${uiState.username}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Stats cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Album,
                        label = "Álbum",
                        value = "${String.format("%.1f", uiState.albumCompletion)}%",
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ContentCopy,
                        label = "Repetidas",
                        value = "${uiState.repeatedCount}",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SwapHoriz,
                        label = "Intercambios",
                        value = "${uiState.pendingExchanges}",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick actions
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(
                icon = Icons.Default.Album,
                title = "Mi Álbum",
                subtitle = "Ver y gestionar tus cartas",
                onClick = onNavigateToAlbum
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                icon = Icons.Default.CardTravel,
                title = "Catálogo de Cartas",
                subtitle = "Explora todas las cartas disponibles",
                onClick = onNavigateToCards
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                icon = Icons.Default.SwapHoriz,
                title = "Intercambios",
                subtitle = "Solicita o acepta intercambios",
                onClick = onNavigateToExchanges
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                icon = Icons.Default.Hub,
                title = "Panini Sync",
                subtitle = "Buscar usuarios y sincronizar álbum Panini",
                onClick = onNavigateToPanini
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
