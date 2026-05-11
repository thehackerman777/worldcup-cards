package com.wcapp.android.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wcapp.android.data.local.SessionManager
import com.wcapp.android.ui.screens.album.AlbumScreen
import com.wcapp.android.ui.screens.auth.LoginScreen
import com.wcapp.android.ui.screens.auth.RegisterScreen
import com.wcapp.android.ui.screens.cards.CardDetailScreen
import com.wcapp.android.ui.screens.cards.CardsScreen
import com.wcapp.android.ui.screens.exchange.CreateExchangeScreen
import com.wcapp.android.ui.screens.exchange.ExchangeDetailScreen
import com.wcapp.android.ui.screens.exchange.ExchangesScreen
import com.wcapp.android.ui.screens.home.HomeScreen
import com.wcapp.android.ui.screens.settings.SettingsScreen
import org.koin.java.KoinJavaComponent.get

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val ALBUM = "album"
    const val CARDS = "cards"
    const val CARD_DETAIL = "card/{cardId}"
    const val EXCHANGES = "exchanges"
    const val EXCHANGE_DETAIL = "exchange/{exchangeId}"
    const val CREATE_EXCHANGE = "create-exchange/{receiverId}"
    const val SETTINGS = "settings"

    fun cardDetail(cardId: String) = "card/$cardId"
    fun exchangeDetail(exchangeId: String) = "exchange/$exchangeId"
    fun createExchange(receiverId: String) = "create-exchange/$receiverId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionManager = get(SessionManager::class.java)
    val sessionState by sessionManager.sessionState.collectAsState()

    val startDestination = if (sessionState.isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAlbum = { navController.navigate(Routes.ALBUM) },
                onNavigateToCards = { navController.navigate(Routes.CARDS) },
                onNavigateToExchanges = { navController.navigate(Routes.EXCHANGES) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ALBUM) {
            AlbumScreen(
                onNavigateToCardDetail = { cardId -> navController.navigate(Routes.cardDetail(cardId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CARDS) {
            CardsScreen(
                onNavigateToCardDetail = { cardId -> navController.navigate(Routes.cardDetail(cardId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CARD_DETAIL,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
            CardDetailScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EXCHANGES) {
            ExchangesScreen(
                onNavigateToDetail = { exchangeId -> navController.navigate(Routes.exchangeDetail(exchangeId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EXCHANGE_DETAIL,
            arguments = listOf(navArgument("exchangeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exchangeId = backStackEntry.arguments?.getString("exchangeId") ?: ""
            ExchangeDetailScreen(
                exchangeId = exchangeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CREATE_EXCHANGE,
            arguments = listOf(navArgument("receiverId") { type = NavType.StringType })
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            CreateExchangeScreen(
                receiverId = receiverId,
                onExchangeCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
