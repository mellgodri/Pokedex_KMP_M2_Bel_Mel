package com.example.pokedex_kmp.data

import kotlinx.serialization.Serializable

@Serializable
data class PokemonStat(
    val name: String,
    val value: Int,
)

@Serializable
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val heightMeters: Double,
    val weightKg: Double,
    val description: String,
    val ability: String,
    val category: String,
    val stats: List<PokemonStat>,
)

@Serializable
data class PokemonListItem(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
)

@Serializable
data class FavoritePokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val capturedAt: String,
    val savedAtMillis: Long,
)

val PokemonTypeFilters = listOf(
    "Todos os tipos",
    "Água",
    "Dragão",
    "Elétrico",
    "Fada",
    "Fantasma",
    "Fogo",
    "Gelo",
    "Grama",
    "Normal",
    "Psíquico",
    "Veneno",
)
