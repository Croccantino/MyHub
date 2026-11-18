package com.myhub.app.ui.components.desktopstate

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(
    series: List<List<Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    showGrid: Boolean = false,
    minValue: Float = 0f,
    maxValue: Float = 100f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        val range = (maxValue - minValue).coerceAtLeast(0.001f)

        if (showGrid) {
            for (g in listOf(0.25f, 0.5f, 0.75f)) {
                val y = h - (g * h)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        series.forEachIndexed { i, s ->
            if (s.size < 2) return@forEachIndexed
            val path = Path()
            s.forEachIndexed { idx, v ->
                val x = idx.toFloat() / (s.size - 1) * w
                val norm = ((v - minValue) / range).coerceIn(0f, 1f)
                val y = h - norm * h
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = colors.getOrElse(i) { Color.Gray },
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}
