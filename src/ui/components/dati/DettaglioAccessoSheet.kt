package com.myhub.app.ui.components.dati

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import com.myhub.app.data.model.AccessoPc
import com.myhub.app.data.model.EventoAccesso

/**
 * Scheda di dettaglio di un accesso.
 *
 * Mostra solo i campi previsti: niente telefono o email, che non sono
 * disponibili nei log.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DettaglioAccessoSheet(
    accesso: AccessoPc,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nonDisponibile = stringResource(R.string.dati_non_disponibile)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dati_dettaglio_titolo),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.chiudi)
                    )
                }
            }

            Spacer(modifier = Modifier.size(4.dp))

            RigaDettaglio(R.string.dati_ip, accesso.ip)
            RigaDettaglio(
                R.string.dati_data,
                formattaData(accesso.ts, nonDisponibile)
            )
            RigaDettaglio(
                R.string.dati_ora,
                formattaOra(accesso.ts, nonDisponibile)
            )
            RigaDettaglio(R.string.dati_metodo, accesso.metodo)
            RigaDettaglio(R.string.dati_utente, accesso.utente)
            RigaDettaglio(
                R.string.dati_esito,
                stringResource(
                    if (accesso.riuscito) R.string.dati_esito_ok
                    else R.string.dati_esito_fallito
                )
            )

            RigaPosizione(accesso.posizione ?: nonDisponibile)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            SezioneEventi(
                titolo = stringResource(R.string.dati_tracciamento),
                eventi = accesso.tracciamento
            )

            Spacer(modifier = Modifier.size(16.dp))

            SezioneEventi(
                titolo = stringResource(R.string.dati_cosa_ha_fatto),
                eventi = accesso.azioni
            )
        }
    }
}

@Composable
private fun RigaDettaglio(@StringRes etichetta: Int, valore: String?) {
    val nonDisponibile = stringResource(R.string.dati_non_disponibile)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(etichetta),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = valore?.takeIf { it.isNotBlank() } ?: nonDisponibile,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RigaPosizione(posizione: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(R.string.dati_posizione),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = posizione,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SezioneEventi(titolo: String, eventi: List<EventoAccesso>) {
    val nonDisponibile = stringResource(R.string.dati_non_disponibile)

    Text(text = titolo, style = MaterialTheme.typography.titleMedium)

    if (eventi.isEmpty()) {
        Text(
            text = nonDisponibile,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
        return
    }

    eventi.forEach { evento ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattaDataOra(evento.ts, nonDisponibile),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = evento.evento ?: nonDisponibile,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
