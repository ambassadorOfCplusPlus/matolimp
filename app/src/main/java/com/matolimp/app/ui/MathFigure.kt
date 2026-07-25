package com.matolimp.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Диаграммы, рисуемые нативно на Canvas (без бинарных изображений и WebView).
 * Вставляются в теорию маркером [[fig:name]].
 */
@Composable
fun MathFigure(name: String, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        when (name) {
            "chessboard" -> Canvas(Modifier.size(200.dp)) { chessboard(ink, primary) }
            "pigeonhole" -> Canvas(Modifier.size(250.dp, 110.dp)) { pigeonhole(ink, primary, muted) }
            "ramsey6" -> Canvas(Modifier.size(200.dp)) { ramsey6(ink, primary, muted) }
            "consecutive" -> Canvas(Modifier.size(250.dp, 90.dp)) { consecutive(ink, primary) }
            "staircase" -> Canvas(Modifier.size(220.dp, 150.dp)) { staircase(ink, primary) }
            "polygon" -> Canvas(Modifier.size(200.dp)) { polygon(ink, primary, 7) }
            "graph" -> Canvas(Modifier.size(240.dp, 170.dp)) { graph(ink, primary) }
            "triangle" -> Canvas(Modifier.size(220.dp, 150.dp)) { triangle(ink, primary) }
            else -> {}
        }
    }
}

private fun DrawScope.chessboard(ink: Color, primary: Color) {
    val n = 8
    val cell = size.width / n
    for (r in 0 until n) for (c in 0 until n) {
        if ((r + c) % 2 == 1) {
            drawRect(primary.copy(alpha = 0.35f), Offset(c * cell, r * cell), Size(cell, cell))
        }
        drawRect(ink.copy(alpha = 0.25f), Offset(c * cell, r * cell), Size(cell, cell), style = Stroke(1.5f))
    }
    // Вырезаны две противоположные угловые клетки
    listOf(0 to 0, n - 1 to n - 1).forEach { (r, c) ->
        val x = c * cell; val y = r * cell
        drawLine(ink, Offset(x + 3, y + 3), Offset(x + cell - 3, y + cell - 3), 3f)
        drawLine(ink, Offset(x + cell - 3, y + 3), Offset(x + 3, y + cell - 3), 3f)
    }
}

private fun DrawScope.pigeonhole(ink: Color, primary: Color, muted: Color) {
    val boxes = 3
    val gap = size.width * 0.06f
    val bw = (size.width - gap * (boxes + 1)) / boxes
    val bh = size.height * 0.8f
    val top = size.height * 0.12f
    val counts = intArrayOf(2, 1, 1) // в первом ящике — два «предмета»
    for (i in 0 until boxes) {
        val x = gap + i * (bw + gap)
        drawRoundRectCompat(muted, x, top, bw, bh, stroke = true)
        val cx = x + bw / 2
        val rDot = min(bw, bh) * 0.14f
        val k = counts[i]
        for (j in 0 until k) {
            val cy = top + bh * (j + 1) / (k + 1)
            drawCircle(primary, rDot, Offset(cx, cy))
        }
    }
}

private fun DrawScope.consecutive(ink: Color, primary: Color) {
    val labels = listOf("n−1", "n", "n+1")
    val r = size.height * 0.32f
    val cy = size.height * 0.42f
    for (i in labels.indices) {
        val cx = size.width * (i + 1) / 4f
        val highlight = i == 1
        drawCircle(if (highlight) primary.copy(alpha = 0.30f) else Color.Transparent, r, Offset(cx, cy))
        drawCircle(ink, r, Offset(cx, cy), style = Stroke(2.5f))
        drawLabel(labels[i], cx, cy + r * 0.35f, ink, r * 0.7f)
    }
    drawLabel("три подряд идущих числа", size.width / 2f, size.height * 0.95f, ink, size.height * 0.13f)
}

private fun DrawScope.ramsey6(ink: Color, primary: Color, muted: Color) {
    val cx = size.width / 2; val cy = size.height / 2
    val R = size.minDimension * 0.4f
    val pts = (0 until 6).map {
        val a = -Math.PI / 2 + it * 2 * Math.PI / 6
        Offset(cx + R * cos(a).toFloat(), cy + R * sin(a).toFloat())
    }
    // все рёбра; треугольник 0-2-4 выделен (иллюстрация монохромной тройки)
    val mono = setOf(setOf(0, 2), setOf(2, 4), setOf(0, 4))
    for (i in 0 until 6) for (j in i + 1 until 6) {
        val hot = setOf(i, j) in mono
        drawLine(if (hot) primary else muted.copy(alpha = 0.4f), pts[i], pts[j], if (hot) 3.5f else 1.5f)
    }
    pts.forEach { drawCircle(ink, size.minDimension * 0.035f, it) }
}

private fun DrawScope.staircase(ink: Color, primary: Color) {
    val steps = 4
    val sw = size.width * 0.9f / steps
    val sh = size.height * 0.85f / steps
    val x0 = size.width * 0.05f
    val yBase = size.height * 0.92f
    for (i in 0 until steps) {
        val h = sh * (i + 1)
        val x = x0 + i * sw
        drawRect(primary.copy(alpha = 0.25f), Offset(x, yBase - h), Size(sw, h))
        drawRect(ink.copy(alpha = 0.6f), Offset(x, yBase - h), Size(sw, h), style = Stroke(2f))
    }
    // стрелка перехода k -> k+1
    val ax = x0 + 1.5f * sw; val ay = yBase - sh * 2.4f
    val bx = x0 + 2.5f * sw; val by = yBase - sh * 3.4f
    drawLine(primary, Offset(ax, ay), Offset(bx, by), 3.5f)
    drawLine(primary, Offset(bx, by), Offset(bx - 12, by + 4), 3.5f)
    drawLine(primary, Offset(bx, by), Offset(bx - 4, by + 14), 3.5f)
    drawLabel("k", x0 + 1.5f * sw, yBase + size.height * 0.06f, ink, size.height * 0.1f)
    drawLabel("k+1", x0 + 2.5f * sw, yBase + size.height * 0.06f, ink, size.height * 0.1f)
}

private fun DrawScope.polygon(ink: Color, primary: Color, n: Int) {
    val cx = size.width / 2; val cy = size.height / 2
    val R = size.minDimension * 0.42f
    val pts = (0 until n).map {
        val a = -Math.PI / 2 + it * 2 * Math.PI / n
        Offset(cx + R * cos(a).toFloat(), cy + R * sin(a).toFloat())
    }
    // диагонали
    for (i in 0 until n) for (j in i + 1 until n) {
        val adjacent = (j - i == 1) || (i == 0 && j == n - 1)
        if (!adjacent) drawLine(primary.copy(alpha = 0.5f), pts[i], pts[j], 1.5f)
    }
    // стороны
    for (i in 0 until n) drawLine(ink, pts[i], pts[(i + 1) % n], 2.5f)
    pts.forEach { drawCircle(ink, size.minDimension * 0.03f, it) }
}

private fun DrawScope.graph(ink: Color, primary: Color) {
    val w = size.width; val h = size.height
    val v = listOf(
        Offset(w * 0.15f, h * 0.30f),
        Offset(w * 0.15f, h * 0.75f),
        Offset(w * 0.50f, h * 0.52f),
        Offset(w * 0.85f, h * 0.25f),
        Offset(w * 0.85f, h * 0.80f)
    )
    val edges = listOf(0 to 1, 0 to 2, 1 to 2, 2 to 3, 2 to 4, 3 to 4)
    edges.forEach { (a, b) -> drawLine(ink.copy(alpha = 0.7f), v[a], v[b], 2.5f) }
    // вершина 2 выделена (иллюстрация степени вершины)
    v.forEachIndexed { i, p ->
        drawCircle(if (i == 2) primary else ink, w * 0.035f, p)
    }
}

private fun DrawScope.triangle(ink: Color, primary: Color) {
    val a = Offset(size.width * 0.12f, size.height * 0.85f)
    val b = Offset(size.width * 0.88f, size.height * 0.85f)
    val c = Offset(size.width * 0.40f, size.height * 0.12f)
    drawLine(ink, a, b, 3f)
    drawLine(ink, b, c, 3f)
    drawLine(ink, c, a, 3f)
    // дуга угла при вершине A
    val r = size.minDimension * 0.16f
    drawArc(
        color = primary,
        startAngle = -60f, sweepAngle = 55f, useCenter = false,
        topLeft = Offset(a.x - r, a.y - r), size = Size(2 * r, 2 * r),
        style = Stroke(3f)
    )
    drawLabel("α", a.x + r * 1.3f, a.y - r * 0.5f, primary, size.minDimension * 0.14f)
    drawLabel("A", a.x - 8, a.y + size.height * 0.11f, ink, size.minDimension * 0.13f)
    drawLabel("B", b.x + 8, b.y + size.height * 0.11f, ink, size.minDimension * 0.13f)
    drawLabel("C", c.x, c.y - 8, ink, size.minDimension * 0.13f)
}

// --- вспомогательное ---

private fun DrawScope.drawRoundRectCompat(color: Color, x: Float, y: Float, w: Float, h: Float, stroke: Boolean) {
    val corner = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    if (stroke) {
        drawRoundRect(color, Offset(x, y), Size(w, h), corner, style = Stroke(2.5f))
    } else {
        drawRoundRect(color, Offset(x, y), Size(w, h), corner)
    }
}

private fun DrawScope.drawLabel(text: String, x: Float, y: Float, color: Color, sizePx: Float) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = sizePx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(text, x, y, paint)
    }
}
