package com.myhub.app.ui.components.dati

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import com.myhub.app.data.model.AccessoPc
import com.myhub.app.ui.theme.ConnectedGreen
import com.myhub.app.ui.theme.DisconnectedRed

/** Riga della sezione "Accessi al PC". */
@Composable
fun RigaAccesso(
    accesso: AccessoPc,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nonDisponibile = stringResource(R.string.dati_non_disponibile)

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accesso.ip ?: nonDisponibile,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${formattaData(accesso.ts, nonDisponibile)} · " +
                        formattaOra(accesso.ts, nonDisponibile),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ChipEsito(riuscito = accesso.riuscito)
        }
    }
}

/** Chip verde per gli accessi riusciti, rosso per quelli falliti. */
@Composable
fun ChipEsito(riuscito: Boolean, modifier: Modifier = Modifier) {
    val colore = if (riuscito) ConnectedGreen else DisconnectedRed
    val testo = stringResource(
        if (riuscito) R.string.dati_esito_ok else R.string.dati_esito_fallito
    )

    Surface(
        modifier = modifier,
        color = colore,
        contentColor = Color.White,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = testo,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
