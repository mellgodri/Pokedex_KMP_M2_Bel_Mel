package com.example.pokedex_kmp.data

actual fun getPlatformPokedexDatabase(): PokedexDatabase = IosInMemoryPokedexDatabase

private object IosInMemoryPokedexDatabase : PokedexDatabase {
    private val cache = mutableListOf<PokemonListItem>()
    private val favorites = linkedMapOf<Int, FavoritePokemon>()

    override suspend fun isPokemonCacheEmpty(): Boolean = cache.isEmpty()

    override suspend fun upsertPokemonCache(items: List<PokemonListItem>) {
        cache.removeAll { current -> items.any { it.id == current.id } }
        cache.addAll(items)
        cache.sortBy { it.id }
    }

    override suspend fun getPokemonPage(query: String, type: String?, limit: Int, offset: Int): List<PokemonListItem> {
        return cache.asSequence()
            .filter { query.isBlank() || it.name.lowercase().contains(query.lowercase()) }
            .filter { type.isNullOrBlank() || it.types.any { current -> current.lowercase().contains(type.lowercase()) } }
            .drop(offset)
            .take(limit)
            .toList()
    }

    override suspend fun getPokemonByIdFromCache(id: Int): PokemonListItem? = cache.firstOrNull { it.id == id }
    override suspend fun getFavoriteIds(): Set<Int> = favorites.keys.toSet()
    override suspend fun getFavorites(): List<FavoritePokemon> = favorites.values.sortedByDescending { it.savedAtMillis }
    override suspend fun getFavoriteById(id: Int): FavoritePokemon? = favorites[id]

    override suspend fun saveFavorite(pokemon: Pokemon, capturedAt: String) {
        favorites[pokemon.id] = FavoritePokemon(
            id = pokemon.id,
            name = pokemon.name,
            imageUrl = pokemon.imageUrl,
            types = pokemon.types,
            capturedAt = capturedAt,
            savedAtMillis = 0L,
        )
    }

    override suspend fun deleteFavorite(id: Int) {
        favorites.remove(id)
    }
}
