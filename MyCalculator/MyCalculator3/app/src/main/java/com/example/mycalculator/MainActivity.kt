package com.example.mycalculator

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dark by remember { mutableStateOf(false) }
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CalculatorScreen(
                        isDark = dark,
                        onToggleTheme = { dark = !dark }
                    )
                }
            }
        }
    }
}

enum class KeyType {
    NUMBER, OPERATOR, SCIENTIFIC, ACTION, EQUALS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(isDark: Boolean, onToggleTheme: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isScientificMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyCalculator") },
                navigationIcon = {
                    TextButton(onClick = { isScientificMode = !isScientificMode }) {
                        Text(if (isScientificMode) "STD" else "SCI", fontSize = 18.sp)
                    }
                },
                actions = {
                    ThemeToggle(isDark = isDark, onToggle = onToggleTheme)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Display(expression = expression, result = result)
            Spacer(modifier = Modifier.height(16.dp))

            if (isScientificMode) {
                val scientificKeys1 = listOf("sin", "cos", "tan", "log", "ln")
                val scientificKeys2 = listOf("asin", "acos", "atan", "1/x", "x!")
                val constantKeys = listOf("(", ")", "π", "e", "^", "√")

                KeyRow(keys = scientificKeys1, keyType = KeyType.SCIENTIFIC, onKey = { k -> expression += "$k(" })
                Spacer(modifier = Modifier.height(12.dp))
                KeyRow(keys = scientificKeys2, keyType = KeyType.SCIENTIFIC, onKey = { k ->
                    expression += when (k) {
                        "1/x" -> "1/"
                        "x!" -> "fact("
                        else -> "$k("
                    }
                })
                Spacer(modifier = Modifier.height(12.dp))
                KeyRow(keys = constantKeys, keyType = KeyType.SCIENTIFIC, onKey = { k ->
                    expression += when (k) {
                        "π" -> "pi"
                        "e" -> "e"
                        "√" -> "sqrt("
                        else -> k
                    }
                })
                Spacer(modifier = Modifier.height(12.dp))
            }

            Keypad(
                onKey = { expression += it },
                onClear = { expression = ""; result = "" },
                onBackspace = { if (expression.isNotEmpty()) expression = expression.dropLast(1) },
                onEquals = {
                    result = try {
                        if (expression.isNotBlank()) evaluateExpression(expression).toString() else ""
                    } catch (e: Exception) {
                        "Error: ${e.message}"
                    }
                }
            )
        }
    }
}

@Composable
fun Keypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Key("7", Modifier.weight(1f), KeyType.NUMBER) { onKey("7") }
            Key("8", Modifier.weight(1f), KeyType.NUMBER) { onKey("8") }
            Key("9", Modifier.weight(1f), KeyType.NUMBER) { onKey("9") }
            Key("⌫", Modifier.weight(1f), KeyType.ACTION) { onBackspace() }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Key("4", Modifier.weight(1f), KeyType.NUMBER) { onKey("4") }
            Key("5", Modifier.weight(1f), KeyType.NUMBER) { onKey("5") }
            Key("6", Modifier.weight(1f), KeyType.NUMBER) { onKey("6") }
            Key("*", Modifier.weight(1f), KeyType.OPERATOR) { onKey("*") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Key("1", Modifier.weight(1f), KeyType.NUMBER) { onKey("1") }
            Key("2", Modifier.weight(1f), KeyType.NUMBER) { onKey("2") }
            Key("3", Modifier.weight(1f), KeyType.NUMBER) { onKey("3") }
            Key("-", Modifier.weight(1f), KeyType.OPERATOR) { onKey("-") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Key("C", Modifier.weight(1f), KeyType.ACTION) { onClear() }
            Key("0", Modifier.weight(1f), KeyType.NUMBER) { onKey("0") }
            Key(".", Modifier.weight(1f), KeyType.NUMBER) { onKey(".") }
            Key("+", Modifier.weight(1f), KeyType.OPERATOR) { onKey("+") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Key("/", Modifier.weight(1f), KeyType.OPERATOR) { onKey("/") }
            Key("=", Modifier.weight(3.3f), KeyType.EQUALS) { onEquals() }
        }
    }
}


@Composable
fun Display(expression: String, result: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = expression.ifEmpty { "0" },
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.End,
            maxLines = 2
        )
        Text(
            text = result,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
fun ThemeToggle(isDark: Boolean, onToggle: () -> Unit) {
    val label = if (isDark) "Dark" else "Light"
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun KeyRow(keys: List<String>, keyType: KeyType, onKey: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        keys.forEach { label ->
            Key(
                label = label,
                modifier = Modifier.weight(1f),
                keyType = keyType,
                onClick = { onKey(label) }
            )
        }
    }
}

@Composable
fun Key(
    label: String,
    modifier: Modifier = Modifier,
    keyType: KeyType,
    onClick: () -> Unit
) {
    val (bgColor, fgColor) = when (keyType) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        KeyType.SCIENTIFIC -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        KeyType.ACTION -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        KeyType.EQUALS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 22.sp, color = fgColor, fontWeight = FontWeight.Medium)
    }
}

private fun factorial(n: Double): Double {
    if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial only for non-negative integers")
    if (n == 0.0) return 1.0
    var result = 1.0
    for (i in 1..n.toInt()) {
        result *= i
    }
    return result
}

fun evaluateExpression(exprRaw: String): Double {
    val exprProcessed = if (exprRaw.startsWith("1/")) {
        val numberPart = exprRaw.substring(2)
        try {
            val innerResult = evaluateExpression(numberPart)
            (1 / innerResult).toString()
        } catch (e: Exception) {
            exprRaw
        }
    } else {
        exprRaw
    }

    val expr = exprProcessed.replace("\\s+".toRegex(), "")
        .replace("pi", Math.PI.toString())
        .replace("e", Math.E.toString())

    val tokens = tokenize(expr)
    val rpn = toRpn(tokens)
    return evalRpn(rpn)
}

private fun tokenize(s: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val c = s[i]
        when {
            c.isDigit() || c == '.' -> {
                var j = i + 1
                while (j < s.length && (s[j].isDigit() || s[j] == '.')) j++
                tokens.add(s.substring(i, j)); i = j
            }
            c.isLetter() -> {
                var j = i + 1
                while (j < s.length && s[j].isLetter()) j++
                tokens.add(s.substring(i, j)); i = j
            }
            c in "+-*/^()" -> { tokens.add(c.toString()); i++ }
            c == '/' && i > 0 && s[i - 1] == '1' && (i == 1 || !s[i - 2].isDigit()) -> {
                if (tokens.last() == "1") tokens.removeAt(tokens.lastIndex)
                tokens.add("1/")
                i++
            }
            else -> throw IllegalArgumentException("Invalid char: $c")
        }
    }
    return tokens
}

private fun precedence(op: String) = when (op) {
    "^" -> 4
    "1/" -> 4
    "*", "/" -> 3
    "+", "-" -> 2
    else -> 0
}

private fun isRightAssociative(op: String) = op == "^"

private fun isFunction(name: String) = name in setOf(
    "sin", "cos", "tan", "log", "ln", "sqrt",
    "asin", "acos", "atan", "fact"
)

private fun toRpn(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val stack = ArrayDeque<String>()
    tokens.forEachIndexed { idx, t ->
        when {
            t.toDoubleOrNull() != null -> output.add(t)
            isFunction(t) -> stack.addLast(t)
            t in "+-*/^" || t == "1/" -> {
                if (t == "-" && (idx == 0 || tokens[idx - 1] in "+-*/^(")) output.add("0")
                while (stack.isNotEmpty()) {
                    val top = stack.last()
                    if ((top in "+-*/^" || top == "1/") &&
                        (precedence(top) > precedence(t) ||
                                (precedence(top) == precedence(t) && !isRightAssociative(t)))
                    ) {
                        output.add(stack.removeLast())
                    } else break
                }
                stack.addLast(t)
            }
            t == "(" -> stack.addLast(t)
            t == ")" -> {
                while (stack.isNotEmpty() && stack.last() != "(") {
                    output.add(stack.removeLast())
                }
                if (stack.isEmpty()) throw IllegalArgumentException("Mismatched parentheses")
                stack.removeLast()

                if (stack.isNotEmpty() && isFunction(stack.last())) {
                    output.add(stack.removeLast())
                }
            }
        }
    }
    while (stack.isNotEmpty()) {
        val top = stack.removeLast()
        if (top == "(" || top == ")") throw IllegalArgumentException("Mismatched parentheses")
        output.add(top)
    }
    return output
}

private fun evalRpn(tokens: List<String>): Double {
    val stack = ArrayDeque<Double>()
    for (token in tokens) {
        val num = token.toDoubleOrNull()
        if (num != null) {
            stack.addLast(num)
        } else if (isFunction(token)) {
            if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for $token")
            val arg = stack.removeLast()
            val res = when (token) {
                "sin" -> sin(Math.toRadians(arg))
                "cos" -> cos(Math.toRadians(arg))
                "tan" -> tan(Math.toRadians(arg))
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "sqrt" -> sqrt(arg)
                "asin" -> Math.toDegrees(asin(arg))
                "acos" -> Math.toDegrees(acos(arg))
                "atan" -> Math.toDegrees(atan(arg))
                "fact" -> factorial(arg)
                else -> throw IllegalArgumentException("Unknown function $token")
            }
            stack.addLast(res)
        } else {
            if (token == "1/") {
                if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for 1/x")
                val a = stack.removeLast()
                stack.addLast(1 / a)
                continue
            }

            if (stack.size < 2) throw IllegalArgumentException("Missing operands for $token")
            val b = stack.removeLast()
            val a = stack.removeLast()
            val res = when (token) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                "^" -> a.pow(b)
                else -> throw IllegalArgumentException("Unknown operator: $token")
            }
            stack.addLast(res)
        }
    }
    if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
    return stack.single()
}
