package com.myhub.app.ui.components.dati

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Scrollbar verticale disegnata a mano: quella di Compose foundation è
 * disponibile solo su desktop.
 *
 * Va posizionata sopra la lista, sul lato sinistro; non intercetta i tocchi.
 */
@Composable
fun ScrollbarVerticale(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    colore: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
) {
    val info = listState.layoutInfo
    val visibili = info.visibleItemsInfo
    if (info.totalItemsCount == 0 || visibili.isEmpty()) return

    val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
    if (viewport <= 0f) return

    val altezzaMedia = visibili.sumOf { it.size }.toFloat() / visibili.size
    val altezzaTotale = (altezzaMedia * info.totalItemsCount).coerceAtLeast(viewport)
    if (altezzaTotale <= viewport + 1f) return

    val primo = visibili.first()
    val scorrimento = (primo.index * altezzaMedia - primo.offset)
        .coerceIn(0f, altezzaTotale - viewport)
    val frazioneVisibile = (viewport / altezzaTotale).coerceIn(0.08f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(6.dp)
            .padding(start = 1.dp)
    ) {
        val altezzaThumb = (maxHeight * frazioneVisibile).coerceAtLeast(24.dp)
        val spostamento = (maxHeight - altezzaThumb) *
            (scorrimento / (altezzaTotale - viewport)).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = spostamento)
                .width(4.dp)
                .height(altezzaThumb)
                .clip(RoundedCornerShape(2.dp))
                .background(colore)
        )
    }
}
