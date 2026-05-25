package com.example.pokedex_kmp.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object PokedexRoute

@Serializable
data class PokemonDetailRoute(val pokemonId: Int)

@Serializable
object FavoritesRoute

