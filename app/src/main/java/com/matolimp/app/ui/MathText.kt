package com.matolimp.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.noties.jlatexmath.JLatexMathDrawable

/**
 * Текст с формулами. Строчная математика — $...$, выносная (отдельным блоком) — $$...$$.
 * Рендер нативно на Canvas через JLaTeXMath (без WebView). Разбор LaTeX кэшируется.
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val nodes = remember(text) { splitByDisplay(text) }

    // Быстрый путь: один текстовый блок — без обёртки в Column.
    if (nodes.size == 1 && nodes[0] is Node.Para) {
        InlinePara((nodes[0] as Node.Para).s, modifier, style)
        return
    }

    Column(modifier) {
        nodes.forEach { node ->
            when (node) {
                is Node.Para -> if (node.s.isNotBlank()) InlinePara(node.s, Modifier, style)
                is Node.Display -> DisplayFormula(node.latex, style)
                is Node.Figure -> MathFigure(node.name)
            }
        }
    }
}

/** Абзац с возможными строчными формулами $...$. */
@Composable
private fun InlinePara(text: String, modifier: Modifier, style: TextStyle) {
    val density = LocalDensity.current
    val color = if (style.color.isSpecified) style.color else LocalContentColor.current
    val fontSizePx = with(density) { fontSizeOf(style).toPx() }
    val argb = color.toArgb()

    val items = remember(text, fontSizePx, argb) {
        parseInline(text).map { seg ->
            when (seg) {
                is Inline.Txt -> Prepared.Txt(seg.s)
                is Inline.Math -> runCatching {
                    JLatexMathDrawable.builder(seg.latex).textSize(fontSizePx).color(argb).build()
                }.fold(
                    onSuccess = { Prepared.Formula(it) },
                    onFailure = { Prepared.Txt(seg.latex) }
                )
            }
        }
    }

    if (items.all { it is Prepared.Txt }) {
        Text(items.joinToString("") { (it as Prepared.Txt).s }, modifier = modifier, style = style)
        return
    }

    val inline = HashMap<String, InlineTextContent>()
    val annotated = buildAnnotatedString {
        items.forEachIndexed { i, item ->
            when (item) {
                is Prepared.Txt -> append(item.s)
                is Prepared.Formula -> {
                    val id = "m$i"
                    val d = item.drawable
                    val wSp = with(density) { d.intrinsicWidth.toSp() }
                    val hSp = with(density) { d.intrinsicHeight.toSp() }
                    appendInlineContent(id, "□")
                    inline[id] = InlineTextContent(
                        Placeholder(wSp, hSp, PlaceholderVerticalAlign.TextCenter)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawIntoCanvas { canvas ->
                                d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                                d.draw(canvas.nativeCanvas)
                            }
                        }
                    }
                }
            }
        }
    }

    // Межстрочный интервал под высоту самой крупной строчной формулы, но не меньше обычного:
    // иначе абзац с маленькими формулами ($n$, $k$) сжимал бы обычный текст.
    val maxFormulaPx = items.filterIsInstance<Prepared.Formula>()
        .maxOfOrNull { it.drawable.intrinsicHeight } ?: 0
    val lineHeight = with(density) { maxOf(fontSizePx * 1.4f, maxFormulaPx * 1.08f).toSp() }
    val spacedStyle = style.copy(lineHeight = lineHeight)

    Text(annotated, modifier = modifier, style = spacedStyle, inlineContent = inline)
}

/** Выносная формула — отдельным центрированным блоком, с прокруткой если широкая. */
@Composable
private fun DisplayFormula(latex: String, style: TextStyle) {
    val density = LocalDensity.current
    val color = if (style.color.isSpecified) style.color else LocalContentColor.current
    val fontSizePx = with(density) { fontSizeOf(style).toPx() }
    val argb = color.toArgb()

    val drawable = remember(latex, fontSizePx, argb) {
        runCatching {
            JLatexMathDrawable.builder(latex).textSize(fontSizePx * 1.15f).color(argb).build()
        }.getOrNull()
    }

    if (drawable == null) {
        Text("\$\$$latex\$\$", style = style)
        return
    }

    val wDp = with(density) { drawable.intrinsicWidth.toDp() }
    val hDp = with(density) { drawable.intrinsicHeight.toDp() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(wDp, hDp)) {
            drawIntoCanvas { canvas ->
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

private fun fontSizeOf(style: TextStyle): TextUnit =
    style.fontSize.takeIf { it != TextUnit.Unspecified } ?: 16.sp

// --- Разбор ---

private sealed interface Node {
    data class Para(val s: String) : Node
    data class Display(val latex: String) : Node
    data class Figure(val name: String) : Node
}

private sealed interface Inline {
    data class Txt(val s: String) : Inline
    data class Math(val latex: String) : Inline
}

private sealed interface Prepared {
    data class Txt(val s: String) : Prepared
    data class Formula(val drawable: JLatexMathDrawable) : Prepared
}

private val BLOCK_REGEX =
    Regex("""\$\$(.+?)\$\$|\[\[fig:([a-z0-9_]+)\]\]""", RegexOption.DOT_MATCHES_ALL)
private val INLINE_REGEX = Regex("""\$(.+?)\$""", RegexOption.DOT_MATCHES_ALL)

/** Делит текст на абзацы, выносные формулы $$...$$ и диаграммы [[fig:name]]. */
private fun splitByDisplay(input: String): List<Node> {
    val nodes = mutableListOf<Node>()
    var last = 0
    for (m in BLOCK_REGEX.findAll(input)) {
        if (m.range.first > last) nodes += Node.Para(input.substring(last, m.range.first).trim('\n'))
        val latex = m.groupValues[1]
        val fig = m.groupValues[2]
        if (latex.isNotEmpty()) nodes += Node.Display(latex.trim()) else nodes += Node.Figure(fig)
        last = m.range.last + 1
    }
    if (last < input.length) nodes += Node.Para(input.substring(last).trim('\n'))
    return if (nodes.isEmpty()) listOf(Node.Para(input)) else nodes
}

/** Делит абзац на текст и строчные формулы $...$. */
private fun parseInline(input: String): List<Inline> {
    val segs = mutableListOf<Inline>()
    var last = 0
    for (m in INLINE_REGEX.findAll(input)) {
        if (m.range.first > last) segs += Inline.Txt(input.substring(last, m.range.first))
        segs += Inline.Math(m.groupValues[1])
        last = m.range.last + 1
    }
    if (last < input.length) segs += Inline.Txt(input.substring(last))
    return segs
}
