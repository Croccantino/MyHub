package com.myhub.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImpostazioniScreen() {
    // Tutto in memoria, nessuna persistenza, nessuna rete.
    var tema by remember { mutableStateOf("Sistema") }
    var bloccoImpronta by remember { mutableStateOf(false) }
    var notificheAccessiFalliti by remember { mutableStateOf(true) }
    var frequenza by remember { mutableStateOf("30s") }
    var giorniConservazione by remember { mutableStateOf(30) }
    var dialogAperta by remember { mutableStateOf<String?>(null) }

    val versione = "0.0.1-alpha"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // Tema: selettore chiaro / scuro / sistema.
        ListItem(
            headlineContent = { Text("Tema") },
            supportingContent = { Text(tema) },
            modifier = Modifier.clickable { dialogAperta = "tema" }
        )

        // Blocco con impronta: Switch.
        ListItem(
            headlineContent = { Text("Blocco con impronta") },
            trailingContent = {
                Switch(
                    checked = bloccoImpronta,
                    onCheckedChange = { bloccoImpronta = it }
                )
            }
        )

        // Notifiche accessi falliti: Switch.
        ListItem(
            headlineContent = { Text("Notifiche accessi falliti") },
            trailingContent = {
                Switch(
                    checked = notificheAccessiFalliti,
                    onCheckedChange = { notificheAccessiFalliti = it }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Frequenza di aggiornamento: selettore 10s / 30s / 60s.
        ListItem(
            headlineContent = { Text("Frequenza di aggiornamento") },
            supportingContent = { Text(frequenza) },
            modifier = Modifier.clickable { dialogAperta = "frequenza" }
        )

        // Giorni di conservazione registro: selettore.
        ListItem(
            headlineContent = { Text("Giorni di conservazione registro") },
            supportingContent = { Text("$giorniConservazione giorni") },
            modifier = Modifier.clickable { dialogAperta = "giorni" }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Versione: sola lettura.
        ListItem(
            headlineContent = { Text("Versione") },
            supportingContent = { Text(versione) }
        )
    }

    when (dialogAperta) {
        "tema" -> {
            val opzioni = listOf("Chiaro", "Scuro", "Sistema")
            AlertDialog(
                onDismissRequest = { dialogAperta = null },
                title = { Text("Tema") },
                text = {
                    Column {
                        opzioni.forEach { opzione ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tema = opzione
                                        dialogAperta = null
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                RadioButton(selected = tema == opzione, onClick = null)
                                Text(
                                    text = opzione,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogAperta = null }) { Text("Chiudi") }
                }
            )
        }

        "frequenza" -> {
            val opzioni = listOf("10s", "30s", "60s")
            AlertDialog(
                onDismissRequest = { dialogAperta = null },
                title = { Text("Frequenza di aggiornamento") },
                text = {
                    Column {
                        opzioni.forEach { opzione ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        frequenza = opzione
                                        dialogAperta = null
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                RadioButton(selected = frequenza == opzione, onClick = null)
                                Text(
                                    text = opzione,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogAperta = null }) { Text("Chiudi") }
                }
            )
        }

        "giorni" -> {
            val opzioni = listOf(7, 30, 90)
            AlertDialog(
                onDismissRequest = { dialogAperta = null },
                title = { Text("Giorni di conservazione registro") },
                text = {
                    Column {
                        opzioni.forEach { opzione ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        giorniConservazione = opzione
                                        dialogAperta = null
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                RadioButton(
                                    selected = giorniConservazione == opzione,
                                    onClick = null
                                )
                                Text(
                                    text = "$opzione giorni",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogAperta = null }) { Text("Chiudi") }
                }
            )
        }

        null -> Unit
    }
}
