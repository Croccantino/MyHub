package com.myhub.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.myhub.app.ui.theme.ConnectedGreen
import com.myhub.app.ui.theme.DisconnectedRed

@Composable
fun BottomBar(
    connected: Boolean,
    onHelp: () -> Unit,
    onReload: () -> Unit,
    onHome: () -> Unit,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHelp) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Aiuto")
            }
            IconButton(onClick = onReload) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reload")
            }
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = onToggleConnection) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.PowerSettingsNew,
                        contentDescription = "Spina",
                        tint = if (connected) ConnectedGreen else DisconnectedRed
                    )
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = "Wifi",
                        tint = if (connected) ConnectedGreen else DisconnectedRed
                    )
                }
            }
        }
    }
}
