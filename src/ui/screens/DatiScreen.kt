package com.myhub.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.myhub.app.data.FakeData
import com.myhub.app.model.Accesso
import com.myhub.app.ui.components.dati.ScrollbarVerticale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatiScreen() {
    // Uptime finto: si incrementa ogni secondo.
    var uptimeSecondi by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            uptimeSecondi++
        }
    }
    val ore = uptimeSecondi / 3600
    val minuti = (uptimeSecondi % 3600) / 60
    val secondi = uptimeSecondi % 60
    val timer = "%02d:%02d:%02d".format(ore, minuti, secondi)

    // Funzione locale (non @Composable): disegna una polilinea con Canvas.
    fun DrawScope.disegnaPunti(punti: List<Float>, colore: Color) {
        if (punti.size < 2) return
        val max = punti.max()
        val min = punti.min()
        val range = (max - min).coerceAtLeast(1f)
        val passoX = size.width / (punti.size - 1)
        for (i in 0 until punti.size - 1) {
            val x1 = i * passoX
            val y1 = size.height - ((punti[i] - min) / range) * size.height
            val x2 = (i + 1) * passoX
            val y2 = size.height - ((punti[i + 1] - min) / range) * size.height
            drawLine(colore, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.dp.toPx())
        }
    }

    var accessoSelezionato by remember { mutableStateOf<Accesso?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Colori letti qui (contesto @Composable) e passati ai lambda del Canvas.
    val coloreLettura = MaterialTheme.colorScheme.primary
    val coloreScrittura = MaterialTheme.colorScheme.tertiary

    Column(modifier = Modifier.fillMaxSize()) {
        // Intestazione: titolo a sinistra, timer a destra.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Access",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = timer,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                )
            ) {
                // Grafico Lettura (Canvas, nessuna libreria).
                item {
                    Text("Lettura", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        disegnaPunti(FakeData.lettura, coloreLettura)
                    }
                }

                // Grafico Scrittura (Canvas, nessuna libreria).
                item {
                    Text("Scrittura", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        disegnaPunti(FakeData.scrittura, coloreScrittura)
                    }
                }

                // Sezione Accessi al PC.
                item {
                    Text(
                        "Accessi al PC",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(FakeData.accessi) { accesso ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accessoSelezionato = accesso }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(accesso.ip, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            accesso.data,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            accesso.ora,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ScrollbarVerticale(
                listState = listState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // Dettaglio accesso: solo IP, data, ora, canale, esito, azione compiuta.
    accessoSelezionato?.let { accesso ->
        ModalBottomSheet(
            onDismissRequest = { accessoSelezionato = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("IP: ${accesso.ip}", style = MaterialTheme.typography.bodyLarge)
                Text("Data: ${accesso.data}", style = MaterialTheme.typography.bodyLarge)
                Text("Ora: ${accesso.ora}", style = MaterialTheme.typography.bodyLarge)
                Text("Canale: ${accesso.canale}", style = MaterialTheme.typography.bodyLarge)
                Text("Esito: ${accesso.esito}", style = MaterialTheme.typography.bodyLarge)
                Text("Azione: ${accesso.azione}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
