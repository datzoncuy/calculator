package com.datzon.liquidcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private val Ink = Color(0xFF080B14)
private val Mint = Color(0xFF9DF6D0)
private val MintBright = Color(0xFFB6FFDF)
private val Muted = Color(0xFF9AA5BA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent {
            MaterialTheme {
                Surface(color = Ink) { LiquidCalculator() }
            }
        }
    }
}

private enum class KeyKind { Number, Operator, Utility, Equals }
private data class CalcKey(val label: String, val kind: KeyKind)
private val keyRows = listOf(
    listOf(CalcKey("AC", KeyKind.Utility), CalcKey("⌫", KeyKind.Utility), CalcKey("%", KeyKind.Utility), CalcKey("÷", KeyKind.Operator)),
    listOf(CalcKey("7", KeyKind.Number), CalcKey("8", KeyKind.Number), CalcKey("9", KeyKind.Number), CalcKey("×", KeyKind.Operator)),
    listOf(CalcKey("4", KeyKind.Number), CalcKey("5", KeyKind.Number), CalcKey("6", KeyKind.Number), CalcKey("−", KeyKind.Operator)),
    listOf(CalcKey("1", KeyKind.Number), CalcKey("2", KeyKind.Number), CalcKey("3", KeyKind.Number), CalcKey("+", KeyKind.Operator)),
    listOf(CalcKey("±", KeyKind.Utility), CalcKey("0", KeyKind.Number), CalcKey(".", KeyKind.Number), CalcKey("=", KeyKind.Equals))
)

@Composable
private fun LiquidCalculator() {
    var expression by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("0") }
    var formula by remember { mutableStateOf("") }
    var evaluated by remember { mutableStateOf(false) }
    var justError by remember { mutableStateOf(false) }

    fun clear() {
        expression = "0"; result = "0"; formula = ""; evaluated = false; justError = false
    }
    fun token(): String = Regex("[0-9]*\\.?[0-9]+$").find(expression)?.value ?: "0"
    fun endsInOperator(): Boolean = expression.lastOrNull()?.let { it == '+' || it == '-' || it == '*' || it == '/' } ?: false
    fun setResult() {
        try {
            val parsed = ExpressionParser(expression).parse()
            if (!parsed.isFinite()) error("Invalid result")
            formula = expression
            result = formatNumber(parsed)
            evaluated = true
            justError = false
        } catch (_: Exception) {
            justError = true
            result = "Can't divide by zero"
            evaluated = true
            formula = expression
        }
    }
    fun press(label: String) {
        val wasError = justError
        justError = false
        when (label) {
            "AC" -> clear()
            "⌫" -> {
                if (evaluated) { clear() } else {
                    expression = if (expression.length <= 1 || (expression.length == 2 && expression.startsWith("-"))) "0" else expression.dropLast(1)
                }
            }
            "+", "−", "×", "÷" -> {
                if (evaluated) { expression = if (wasError) "0" else result.replace(",", ""); evaluated = false; formula = "" }
                val op = when (label) { "−" -> "-"; "×" -> "*"; "÷" -> "/"; else -> "+" }
                if (expression == "0" && op == "-") expression = "-"
                else if (endsInOperator()) expression = expression.dropLast(1) + op
                else expression += op
            }
            "=" -> if (!evaluated) setResult() else if (wasError) justError = true
            "%" -> {
                if (evaluated) { expression = if (wasError) "0" else result.replace(",", ""); evaluated = false; formula = "" }
                val m = Regex("[0-9]+(?:\\.[0-9]*)?$").find(expression)
                if (m != null) expression = expression.substring(0, m.range.first) + ((m.value.toDoubleOrNull() ?: 0.0) / 100.0).toString()
            }
            "±" -> {
                if (evaluated && !wasError) { expression = (-(result.replace(",", "").toDoubleOrNull() ?: 0.0)).toString(); evaluated = false; formula = "" }
                else if (evaluated && wasError) { expression = "0"; evaluated = false; formula = "" }
                else {
                    val match = Regex("[0-9]+(?:\\.[0-9]*)?$").find(expression)
                    if (match != null) {
                        val start = match.range.first
                        if (start == 0) expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                        else {
                            val prev = expression[start - 1]
                            if (prev == '+' || prev == '-') expression = expression.substring(0, start - 1) + (if (prev == '+') "-" else "+") + match.value
                            else if (prev == '*' || prev == '/') expression = expression.substring(0, start) + "(-${match.value})"
                        }
                    }
                }
            }
            "." -> {
                if (evaluated) { expression = "0."; evaluated = false; formula = "" }
                else if (!token().contains('.')) {
                    expression = if (endsInOperator()) expression + "0."
                    else if (expression == "0") "0." else expression + "."
                }
            }
            else -> {
                if (evaluated) { expression = label; evaluated = false; formula = "" }
                else expression = if (expression == "0") label else expression + label
            }
        }
    }

    val visibleFormula = when {
        evaluated && formula.isNotBlank() -> formula.replace("*", " × ").replace("/", " ÷ ").replace("-", "−").replace("+", " + ")
        expression == "0" -> "YOUR NEXT MOVE STARTS HERE"
        else -> expression.replace("*", " × ").replace("/", " ÷ ").replace("-", "−").replace("+", " + ")
    }
    val display = if (evaluated) result else if (endsInOperator()) "0" else token()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF0B1120), Ink, Color(0xFF111A24))))
    ) {
        Box(Modifier.offset(x = maxWidth * 0.46f, y = (-60).dp).size(270.dp).blur(55.dp).background(Brush.radialGradient(listOf(Color(0xFF2CBF9D).copy(alpha = 0.24f), Color.Transparent)), CircleShape))
        Box(Modifier.offset(x = (-110).dp, y = maxHeight * 0.35f).size(245.dp).blur(60.dp).background(Brush.radialGradient(listOf(Color(0xFF5C74E8).copy(alpha = 0.16f), Color.Transparent)), CircleShape))

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 19.dp)
                .widthIn(max = 460.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.size(43.dp).shadow(14.dp, RoundedCornerShape(15.dp)).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(Color(0xFFB8FFE0), Color(0xFF51BFA1)))), contentAlignment = Alignment.Center) {
                        Text("◈", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF092019))
                    }
                    Column {
                        Text("DATZON", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.8.sp, color = Color.White)
                        Text("LIQUID CALCULATOR", fontSize = 9.sp, letterSpacing = 1.55.sp, color = Muted)
                    }
                }
                Row(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.075f)).border(1.dp, Color.White.copy(alpha = .13f), CircleShape).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).background(Mint, CircleShape))
                    Text("PRO", fontSize = 10.sp, color = Mint, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                Modifier.fillMaxWidth().shadow(30.dp, RoundedCornerShape(30.dp)).clip(RoundedCornerShape(30.dp))
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = .105f), Color.White.copy(alpha = .035f), Color(0xFF96F8D3).copy(alpha = .035f))))
                    .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = .23f), Color.White.copy(alpha = .04f), Mint.copy(alpha = .17f)))), RoundedCornerShape(30.dp))
                    .padding(horizontal = 23.dp, vertical = 22.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LIVE WORKSPACE", fontSize = 9.sp, letterSpacing = 2.sp, color = Muted, fontWeight = FontWeight.SemiBold)
                    Text("●  LOCAL ENGINE", fontSize = 9.sp, letterSpacing = 1.sp, color = Mint.copy(alpha = .9f))
                }
                Spacer(Modifier.height(27.dp))
                Text(visibleFormula, modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End, fontSize = 13.sp, letterSpacing = .7.sp, color = Muted)
                Spacer(Modifier.height(8.dp))
                AnimatedContent(targetState = if (justError) "Error" else display, transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(90)) }, label = "result") { shown ->
                    Text(shown, modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End, fontSize = if (shown.length > 10) 31.sp else 43.sp, lineHeight = 48.sp, letterSpacing = (-1.4).sp, fontWeight = FontWeight.Light, color = if (justError) Color(0xFFFF9FAE) else Color(0xFFF6FAFF))
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(22.dp).height(2.dp).background(Mint, CircleShape))
                    Text(if (evaluated && !justError) "RESULT LOCKED IN" else "PRECISION MODE", fontSize = 9.sp, letterSpacing = 1.7.sp, color = Mint.copy(alpha = .82f))
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth()) {
                keyRows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { key ->
                            KeyButton(key = key, modifier = Modifier.weight(1f).height(63.dp), onClick = { press(key.label) })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("DESIGNED IN LIQUID GLASS", fontSize = 9.sp, letterSpacing = 1.7.sp, color = Color.White.copy(alpha = .40f))
                Spacer(Modifier.size(9.dp))
                Box(Modifier.size(3.dp).background(Mint.copy(alpha = .65f), CircleShape))
                Spacer(Modifier.size(9.dp))
                Text("DATZON STUDIO · 01", fontSize = 9.sp, letterSpacing = 1.4.sp, color = Color.White.copy(alpha = .40f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun KeyButton(key: CalcKey, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val background = when (key.kind) {
        KeyKind.Number -> Brush.linearGradient(listOf(Color.White.copy(alpha = .105f), Color.White.copy(alpha = .045f)))
        KeyKind.Operator -> Brush.linearGradient(listOf(Color(0xFFB5FFE0).copy(alpha = .98f), Color(0xFF6DDBB7).copy(alpha = .92f)))
        KeyKind.Utility -> Brush.linearGradient(listOf(Color.White.copy(alpha = .17f), Color.White.copy(alpha = .075f)))
        KeyKind.Equals -> Brush.linearGradient(listOf(Color(0xFFC0FFE5), Color(0xFF66DDB6)))
    }
    val textColor = when (key.kind) {
        KeyKind.Operator, KeyKind.Equals -> Color(0xFF09251B)
        KeyKind.Utility -> Color(0xFFE5F1F0)
        KeyKind.Number -> Color(0xFFF7FAFF)
    }
    Box(
        modifier = modifier.shadow(if (key.kind == KeyKind.Equals) 11.dp else 0.dp, shape)
            .clip(shape).background(background)
            .border(1.dp, if (key.kind == KeyKind.Operator || key.kind == KeyKind.Equals) Color.White.copy(alpha = .48f) else Color.White.copy(alpha = .095f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(key.label, fontSize = when (key.label) { "AC" -> 15.sp; "⌫" -> 22.sp; "±" -> 20.sp; else -> 22.sp }, fontWeight = if (key.kind == KeyKind.Operator || key.kind == KeyKind.Equals || key.kind == KeyKind.Utility) FontWeight.SemiBold else FontWeight.Normal, color = textColor)
    }
}

private fun formatNumber(value: Double): String {
    if (abs(value) < 0.000000005) return "0"
    val symbols = DecimalFormatSymbols(Locale.US)
    return DecimalFormat("#,##0.########", symbols).format(value)
}

private class ExpressionParser(private val source: String) {
    private var position = 0

    fun parse(): Double {
        val value = expression()
        skipSpaces()
        if (position != source.length) throw IllegalArgumentException("Unexpected token")
        if (!value.isFinite()) throw ArithmeticException("Not finite")
        return value
    }

    private fun expression(): Double {
        var value = term()
        while (true) {
            skipSpaces()
            value = when {
                take('+') -> value + term()
                take('-') -> value - term()
                else -> return value
            }
        }
    }

    private fun term(): Double {
        var value = factor()
        while (true) {
            skipSpaces()
            when {
                take('*') -> value *= factor()
                take('/') -> {
                    val divisor = factor()
                    if (abs(divisor) < 0.0000000000001) throw ArithmeticException("Divide by zero")
                    value /= divisor
                }
                else -> return value
            }
        }
    }

    private fun factor(): Double {
        skipSpaces()
        if (take('+')) return factor()
        if (take('-')) return -factor()
        if (take('(')) {
            val value = expression()
            skipSpaces()
            if (!take(')')) throw IllegalArgumentException("Missing closing parenthesis")
            return value
        }
        val start = position
        var hasDigit = false
        while (position < source.length && source[position].isDigit()) { position++; hasDigit = true }
        if (position < source.length && source[position] == '.') {
            position++
            while (position < source.length && source[position].isDigit()) { position++; hasDigit = true }
        }
        if (!hasDigit) throw IllegalArgumentException("Number expected")
        return source.substring(start, position).toDouble()
    }

    private fun take(char: Char): Boolean {
        if (position < source.length && source[position] == char) { position++; return true }
        return false
    }

    private fun skipSpaces() { while (position < source.length && source[position].isWhitespace()) position++ }
}
