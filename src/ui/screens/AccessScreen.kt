package com.myhub.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myhub.app.R
import com.myhub.app.data.model.DiscoInfo
import com.myhub.app.data.repository.AgenteDatiRepository
import com.myhub.app.data.repository.DesktopRepository
import com.myhub.app.ui.components.dati.GraficoTempo
import com.myhub.app.ui.components.dati.IntestazioneAccess
import com.myhub.app.ui.components.dati.ScrollbarVerticale

/**
 * Schermata "Access" - accessi e statistica del PC.
 *
 * Mostra grafici I/O, lista accessi e uptime del sistema.
 */
@Composable
fun AccessScreen(
    onOpenDrawer: () -> Unit = {},
    onVaiPreferenze: () -> Unit = {}
) {
    val contesto = LocalContext.current
    val repositoryDesktop = remember { DesktopRepository.getInstance(contesto) }
    val repositoryDati = remember { AgenteDatiRepository() }

    val viewModel: DatiViewModel = viewModel(
        factory = remember(repositoryDati, repositoryDesktop) {
            DatiViewModelFactory(repositoryDati, repositoryDesktop.activeServerFlow)
        }
    )
    val stato by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleStartEffect(viewModel) {
        viewModel.onVisibile()
        onStopOrDispose { viewModel.onNonVisibile() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        IntestazioneAccess(
            uptimeSecondiIniziale = stato.uptimeSecondi,
            onOpenDrawer = onOpenDrawer
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (stato.fase) {
                FaseDati.SENZA_SERVER -> MessaggioSenzaServer(
                    onVaiPreferenze = onVaiPreferenze
                )
                FaseDati.CARICAMENTO -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                FaseDati.ERRORE -> MessaggioErrore(
                    errore = stato.errore,
                    onRiprova = viewModel::riprova
                )
                FaseDati.PRONTO -> ContenutoDati(
                    stato = stato,
                    onRefresh = viewModel::refresh
                )
            }
        }
    }
}

@Composable
private fun MessaggioSenzaServer(onVaiPreferenze: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.dati_nessun_server),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVaiPreferenze) {
            Text(stringResource(R.string.dati_vai_preferenze))
        }
    }
}

@Composable
private fun MessaggioErrore(errore: ErroreDati?, onRiprova: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(messaggioErrore(errore)),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRiprova) {
            Text(stringResource(R.string.retry))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenutoDati(
    stato: DatiUiState,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = stato.refreshInCorso,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Grafici I/O
                item {
                    GraficoTempo(
                        titolo = stringResource(R.string.dati_lettura),
                        punti = stato.campioniIo,
                        selettore = { it.letturaMbS },
                        colore = MaterialTheme.colorScheme.primary,
                        nonDisponibile = stato.ioNonDisponibile
                    )
                }
                item {
                    GraficoTempo(
                        titolo = stringResource(R.string.dati_scrittura),
                        punti = stato.campioniIo,
                        selettore = { it.scritturaMbS },
                        colore = MaterialTheme.colorScheme.tertiary,
                        nonDisponibile = stato.ioNonDisponibile
                    )
                }
            }
        }

        ScrollbarVerticale(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

private fun messaggioErrore(errore: ErroreDati?): Int = when (errore) {
    ErroreDati.TOKEN -> R.string.dati_errore_token
    ErroreDati.RETE -> R.string.dati_errore_rete
    ErroreDati.GENERICO -> R.string.dati_errore_generico
    null -> R.string.dati_errore_generico
}
