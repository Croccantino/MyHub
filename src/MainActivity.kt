package com.myhub.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myhub.app.data.settings.AppSettingsRepository
import com.myhub.app.ui.biometrics.BiometricHelper
import com.myhub.app.ui.components.AppDrawerContent
import com.myhub.app.ui.components.BottomBar
import com.myhub.app.ui.navigation.Route
import com.myhub.app.ui.screens.AccessScreen
import com.myhub.app.ui.screens.DatiScreen
import com.myhub.app.ui.screens.DesktopStateScreen
import com.myhub.app.ui.screens.HomeScreen
import com.myhub.app.ui.screens.ImpostazioniScreen
import com.myhub.app.ui.screens.PlaceholderScreen
import com.myhub.app.ui.screens.PreferenzeScreen
import com.myhub.app.ui.theme.AppThemeMode
import com.myhub.app.ui.theme.MyHubTheme
import kotlinx.coroutines.launch

private const val TAG_BIO = "MyHubBio"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = AppSettingsRepository.getInstance(applicationContext)

        setContent {
            val themeMode by repository.themeModeFlow.collectAsState(initial = AppThemeMode.SYSTEM)
            val biometricEnabled by repository.biometricLockEnabledFlow.collectAsState(initial = null)

            MyHubTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (biometricEnabled) {
                        null -> {
                            // Attesa caricamento DataStore
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        true -> {
                            var isUnlocked by remember { mutableStateOf(false) }
                            var richiestaInCorso by remember { mutableStateOf(false) }

                            fun triggerBiometricAuth() {
                                if (richiestaInCorso) return
                                if (!BiometricHelper.canAuthenticate(this@MainActivity)) {                                    // Nessun sensore utilizzabile: non sblocchiamo da soli,
                                    // altrimenti il blocco sarebbe aggirabile.
                                    Log.w(TAG_BIO, "Sensore biometrico non disponibile")
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.biometric_non_disponibile),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }

                                richiestaInCorso = true
                                Log.d(TAG_BIO, "Apertura prompt biometrico")
                                BiometricHelper.showBiometricPrompt(
                                    activity = this@MainActivity,
                                    title = getString(R.string.biometric_prompt_title),
                                    subtitle = getString(R.string.biometric_prompt_subtitle),
                                    negativeButtonText = getString(R.string.annulla),
                                    onSuccess = {
                                        Log.d(TAG_BIO, "Sblocco biometrico riuscito")
                                        richiestaInCorso = false
                                        isUnlocked = true
                                    },
                                    onFailed = {
                                        // Impronta non riconosciuta: il prompt resta aperto.
                                        Log.d(TAG_BIO, "Impronta non riconosciuta")
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.biometric_impronta_non_riconosciuta),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { error ->
                                        // Annulla, Back o errore: si resta sulla LockScreen.
                                        Log.d(TAG_BIO, "Autenticazione non riuscita: $error")
                                        richiestaInCorso = false
                                        isUnlocked = false
                                        Toast.makeText(
                                            this@MainActivity,
                                            error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            LaunchedEffect(Unit) {
                                triggerBiometricAuth()
                            }

                            if (isUnlocked) {
                                MainApp()
                            } else {
                                LockScreen(onUnlockClick = { triggerBiometricAuth() })
                            }
                        }
                        false -> {
                            MainApp()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LockScreen(onUnlockClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_bloccata),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.biometric_prompt_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onUnlockClick) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.sblocca))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Route.Home.path
    var isConnected by remember { mutableStateOf(true) }

    // Lo Scaffold esterno gestisce la BottomBar, che rimane visibile anche con il Drawer aperto
    Scaffold(
        bottomBar = {
            // La schermata "Desktop State" ha la propria BottomBar.
            if (currentRoute != Route.Desktop.path) {
                BottomBar(
                    connected = isConnected,
                    onHelp = { navController.navigate(Route.Help.path) { launchSingleTop = true } },
                    onReload = { /* refresh */ },
                    onHome = { navController.navigate(Route.Home.path) { launchSingleTop = true } },
                    onToggleConnection = { isConnected = !isConnected }
                )
            }
        }
    ) { outerPadding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(currentRoute = currentRoute) { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route.path) { launchSingleTop = true }
                }
            },
            modifier = Modifier.padding(outerPadding)
        ) {
            Scaffold(
                topBar = {
                    // La schermata "Access" ha una sua intestazione con logo,
                    // titolo e timer: quella globale resta nascosta.
                    if (currentRoute != Route.Dati.path && currentRoute != Route.Desktop.path && currentRoute != Route.Access.path) {
                        TopAppBar(
                            title = { Text("MyHub") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "Menu",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    NavHost(navController = navController, startDestination = Route.Home.path) {
                        composable(Route.Home.path) {
                            HomeScreen(onNavigate = { routePath ->
                                navController.navigate(routePath) { launchSingleTop = true }
                            })
                        }
                        composable(Route.Dati.path) {
                            DatiScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onVaiPreferenze = {
                                    navController.navigate(Route.Preferenze.path) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(Route.Access.path) {
                            AccessScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onVaiPreferenze = {
                                    navController.navigate(Route.Preferenze.path) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(Route.Preferenze.path) { PreferenzeScreen() }
                        composable(Route.Impostazioni.path) { ImpostazioniScreen() }
                        composable(Route.Desktop.path) {
                            DesktopStateScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onHome = { navController.navigate(Route.Home.path) { launchSingleTop = true } }
                            )
                        }
                        composable(Route.Remote.path) { PlaceholderScreen("Remote control") }
                        composable(Route.Devices.path) { PlaceholderScreen("Dispositivi collegati") }
                        composable(Route.Terminal.path) { PlaceholderScreen("Terminal") }
                        composable(Route.Help.path) { PlaceholderScreen("Aiuto") }
                    }
                }
            }
        }
    }
}
