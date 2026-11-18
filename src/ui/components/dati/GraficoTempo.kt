package com.myhub.app.ui.components.dati

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import com.myhub.app.data.model.CampioneIo
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import java.util.Locale
import kotlin.math.max

/**
 * Grafico a linee di una singola metrica I/O.
 *
 * [selettore] estrae il valore da mostrare da ogni campione, così lo stesso
 * componente serve sia per la lettura sia per la scrittura.
 */
@Composable
fun GraficoTempo(
    titolo: String,
    punti: List<CampioneIo>,
    selettore: (CampioneIo) -> Double,
    colore: Color,
    nonDisponibile: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = titolo, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.dati_mb_s),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (nonDisponibile || punti.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dati_io_non_disponibile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                GraficoVico(punti = punti, selettore = selettore, colore = colore)
            }
        }
    }
}

@Composable
private fun GraficoVico(
    punti: List<CampioneIo>,
    selettore: (CampioneIo) -> Double,
    colore: Color
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(punti) {
        modelProducer.runTransaction {
            lineSeries { series(punti.map { selettore(it).toFloat() }) }
        }
    }

    val etichette = remember(punti) {
        punti.map { formattaOraMinuti(it.ts, "--:--") }
    }
    val passo = remember(punti.size) { max(1, punti.size / 5) }
    val formattatoreX = remember(etichette) {
        CartesianValueFormatter { _, valore, _ ->
            val indice = valore.toInt().coerceIn(0, etichette.lastIndex.coerceAtLeast(0))
            etichette.getOrNull(indice)?.takeIf { it.isNotEmpty() } ?: "--:--"
        }
    }
    val formattatoreY = remember {
        CartesianValueFormatter { _, valore, _ ->
            String.format(Locale.ITALIAN, "%.1f", valore)
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(colore)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(colore.copy(alpha = 0.18f))
                        ),
                        stroke = LineCartesianLayer.LineStroke.continuous(
                            thickness = 2.dp,
                            cap = StrokeCap.Round
                        )
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                valueFormatter = formattatoreY,
                title = stringResource(R.string.dati_mb_s),
                titleComponent = rememberAxisLabelComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                valueFormatter = formattatoreX,
                itemPlacer = remember(passo) {
                    HorizontalAxis.ItemPlacer.aligned(spacing = { passo })
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false)
    )
}
