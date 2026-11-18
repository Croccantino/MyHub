package com.myhub.app.ui.components.dati

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import kotlinx.coroutines.delay

/** Intestazione fissa della schermata "Access".
 *
 * Il timer dell'uptime vive qui (tick locale ogni secondo) invece che nello
 * stato globale del ViewModel, così la ricomposizione ogni secondo interessa
 * solo l'intestazione e non grafici e lista.
 */
@Composable
fun IntestazioneAccess(
    uptimeSecondiIniziale: Long,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uptimeLocale by remember(uptimeSecondiIniziale) {
        mutableStateOf(uptimeSecondiIniziale)
    }

    // Avanza il contatore ogni secondo in locale.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            uptimeLocale += 1
        }
    }

    // Risincronizza col valore del server quando arriva (ogni 60 s).
    LaunchedEffect(uptimeSecondiIniziale) {
        uptimeLocale = uptimeSecondiIniziale
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenDrawer) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.menu),
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = stringResource(R.string.access),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formattaUptime(uptimeLocale),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
