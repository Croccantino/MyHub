package com.myhub.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myhub.app.ui.components.AppScaffold
import com.myhub.app.ui.screens.PlaceholderScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_DATI = "dati"
private const val ROUTE_PREFERENZE = "preferenze"
private const val ROUTE_IMPOSTAZIONI = "impostazioni"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: ROUTE_HOME

    AppScaffold(
        currentRoute = currentRoute,
        onDrawerItemClick = { route ->
            navController.navigate(route) { launchSingleTop = true }
        },
        onHome = {
            navController.navigate(ROUTE_HOME) { launchSingleTop = true }
        }
    ) {
        NavHost(navController = navController, startDestination = ROUTE_HOME) {
            composable(ROUTE_HOME) { PlaceholderScreen("Home") }
            composable(ROUTE_DATI) { PlaceholderScreen("Dati") }
            composable(ROUTE_PREFERENZE) { PlaceholderScreen("Preferenze") }
            composable(ROUTE_IMPOSTAZIONI) { PlaceholderScreen("Impostazioni") }
        }
    }
}
