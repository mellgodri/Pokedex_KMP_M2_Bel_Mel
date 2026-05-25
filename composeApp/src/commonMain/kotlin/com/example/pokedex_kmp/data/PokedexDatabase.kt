package com.example.pokedex_kmp.data

interface PokedexDatabase {
    suspend fun isPokemonCacheEmpty(): Boolean
    suspend fun upsertPokemonCache(items: List<PokemonListItem>)
    suspend fun getPokemonPage(query: String, type: String?, limit: Int, offset: Int): List<PokemonListItem>
    suspend fun getPokemonByIdFromCache(id: Int): PokemonListItem?
    suspend fun getFavoriteIds(): Set<Int>
    suspend fun getFavorites(): List<FavoritePokemon>
    suspend fun getFavoriteById(id: Int): FavoritePokemon?
    suspend fun saveFavorite(pokemon: Pokemon, capturedAt: String)
    suspend fun deleteFavorite(id: Int)
}

expect fun getPlatformPokedexDatabase(): PokedexDatabase
