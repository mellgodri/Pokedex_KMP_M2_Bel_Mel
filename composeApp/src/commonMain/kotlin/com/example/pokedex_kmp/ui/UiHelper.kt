package com.example.pokedex_kmp.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Int.formatPokemonNumber(): String = "N°${toString().padStart(3, '0')}"

fun pokemonTypeColor(type: String): Color = when (type.lowercase()) {
    "grama" -> Color(0xFF5ABD55)
    "veneno", "venenoso" -> Color(0xFF9B6AA1)
    "fogo" -> Color(0xFFFF964F)
    "água", "agua" -> Color(0xFF5A9FDD)
    "dragão", "dragao" -> Color(0xFF1D82C7)
    "elétrico", "eletrico" -> Color(0xFFF7D63F)
    "gelo" -> Color(0xFF70CEC2)
    "normal" -> Color(0xFF9FA19F)
    "fada" -> Color(0xFFDB70D6)
    "lutador" -> Color(0xFFCE416B)
    "aço", "aco", "metal" -> Color(0xFF5A8EA1)
    "fantasma" -> Color(0xFF6272B8)
    "psíquico", "psiquico" -> Color(0xFF9E729F)
    "terra" -> Color(0xFFD6B293)
    "voador" -> Color(0xFF8FA8DD)
    else -> Color(0xFF8A8F98)
}

fun pokemonTypeTextColor(type: String): Color {
    return when (type.lowercase()) {
        "elétrico", "eletrico" -> Color(0xFF111827)
        else -> Color.White
    }
}

fun pokemonTypeGradient(types: List<String>): Brush {
    val colors = types.ifEmpty { listOf("Normal") }.take(2).map(::pokemonTypeColor)
    return Brush.linearGradient(
        colors = if (colors.size == 1) listOf(colors.first(), colors.first().copy(alpha = 0.75f)) else colors,
    )
}

fun pokemonSoftBackground(types: List<String>): Color {
    return pokemonTypeColor(types.firstOrNull().orEmpty()).copy(alpha = 0.12f)
}
