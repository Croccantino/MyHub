package com.myhub.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.myhub.app.data.FakeData
import com.myhub.app.ui.components.HomeTile

@Composable
fun HomeScreen() {
    val contesto = LocalContext.current
    val icone = listOf<ImageVector>(
        Icons.Filled.Monitor,
        Icons.Filled.Storage,
        Icons.AutoMirrored.Filled.ScreenShare,
        Icons.Filled.Usb,
        Icons.Filled.Terminal
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(FakeData.homeCard) { index, card ->
            HomeTile(
                icona = icone[index],
                titolo = card.titolo,
                sottotitolo = card.sottotitolo,
                onClick = {
                    Toast.makeText(contesto, card.titolo, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
