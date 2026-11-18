package com.myhub.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myhub.app.ui.components.CardTile
import com.myhub.app.ui.navigation.Route

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Griglia 2x2: Riga 1 (Desktop state, Dati)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardTile(
                title = "Desktop state",
                icon = Icons.Filled.Monitor,
                onClick = { onNavigate(Route.Desktop.path) },
                modifier = Modifier.weight(1f)
            )
            CardTile(
                title = "Dati",
                icon = Icons.Filled.Album,
                onClick = { onNavigate(Route.Dati.path) },
                modifier = Modifier.weight(1f)
            )
        }

        // Griglia 2x2: Riga 2 (Remote control, Dispositivi collegati)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardTile(
                title = "Remote control",
                icon = Icons.AutoMirrored.Filled.ScreenShare,
                onClick = { onNavigate(Route.Remote.path) },
                modifier = Modifier.weight(1f)
            )
            CardTile(
                title = "Dispositivi collegati",
                icon = Icons.Filled.Usb,
                onClick = { onNavigate(Route.Devices.path) },
                modifier = Modifier.weight(1f)
            )
        }

        // Riga 3: Terminal (tessera orizzontale a tutta larghezza)
        CardTile(
            title = "Terminal",
            icon = Icons.Filled.Terminal,
            onClick = { onNavigate(Route.Terminal.path) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
