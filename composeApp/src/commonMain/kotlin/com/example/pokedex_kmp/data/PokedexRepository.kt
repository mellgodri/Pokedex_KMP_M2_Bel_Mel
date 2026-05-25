package com.example.pokedex_kmp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val MAX_FAVORITE_POKEMONS = 6

class PokemonTeamLimitException(
    message: String = "Voce ja possui 6 Pokemons salvos. Remova um Pokemon dos favoritos para adicionar outro."
) : IllegalStateException(message)

class PokedexRepository(
    private val database: PokedexDatabase,
    private val apiClient: PokeApiClient = PokeApiClient(),
) {
    suspend fun ensureInitialSync() {
        withContext(Dispatchers.Default) {
            if (database.isPokemonCacheEmpty()) {
                val remoteItems = runCatching { apiClient.fetchInitialPokemonCache() }
                    .getOrElse { fallbackPokemonCache() }

                database.upsertPokemonCache(remoteItems)
            }
        }
    }

    suspend fun getPokemonPage(query: String, type: String?, limit: Int, offset: Int): List<PokemonListItem> {
        return withContext(Dispatchers.Default) {
            database.getPokemonPage(
                query = query.trim(),
                type = type?.takeUnless { it == "Todos os tipos" },
                limit = limit,
                offset = offset,
            )
        }
    }

    suspend fun getFavoriteIds(): Set<Int> = withContext(Dispatchers.Default) {
        database.getFavoriteIds()
    }

    suspend fun getFavorites(): List<FavoritePokemon> = withContext(Dispatchers.Default) {
        database.getFavorites()
    }

    suspend fun getFavoriteById(id: Int): FavoritePokemon? = withContext(Dispatchers.Default) {
        database.getFavoriteById(id)
    }

    suspend fun getPokemonDetail(id: Int): Pokemon {
        return runCatching { apiClient.fetchPokemonDetail(id) }
            .getOrElse {
                val cached = database.getPokemonByIdFromCache(id)
                cached?.toFallbackPokemon()
                    ?: throw IllegalStateException("Não foi possível carregar o Pokémon pela API.")
            }
    }

    suspend fun saveFavorite(pokemon: Pokemon, capturedAt: String) = withContext(Dispatchers.Default) {
        val isAlreadySaved = database.getFavoriteById(pokemon.id) != null
        val savedCount = database.getFavorites().size

        if (!isAlreadySaved && savedCount >= MAX_FAVORITE_POKEMONS) {
            throw PokemonTeamLimitException()
        }

        database.saveFavorite(pokemon, capturedAt.trim())
    }

    suspend fun deleteFavorite(id: Int) = withContext(Dispatchers.Default) {
        database.deleteFavorite(id)
    }

    private fun PokemonListItem.toFallbackPokemon(): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            imageUrl = imageUrl,
            types = types,
            heightMeters = 0.0,
            weightKg = 0.0,
            description = "Informações detalhadas indisponíveis sem conexão. Abra novamente quando estiver conectado para atualizar pela PokeAPI.",
            ability = "Não informado",
            category = "Pokémon",
            stats = emptyList(),
        )
    }
}

object AppGraph {
    val repository: PokedexRepository by lazy {
        PokedexRepository(getPlatformPokedexDatabase())
    }
}

private fun fallbackPokemonCache(): List<PokemonListItem> = listOf(
    PokemonListItem(1, "Bulbasaur", imageUrl(1), listOf("Grama", "Veneno")),
    PokemonListItem(2, "Ivysaur", imageUrl(2), listOf("Grama", "Veneno")),
    PokemonListItem(3, "Venusaur", imageUrl(3), listOf("Grama", "Veneno")),
    PokemonListItem(4, "Charmander", imageUrl(4), listOf("Fogo")),
    PokemonListItem(5, "Charmeleon", imageUrl(5), listOf("Fogo")),
    PokemonListItem(6, "Charizard", imageUrl(6), listOf("Fogo", "Voador")),
    PokemonListItem(7, "Squirtle", imageUrl(7), listOf("Água")),
    PokemonListItem(8, "Wartortle", imageUrl(8), listOf("Água")),
    PokemonListItem(9, "Blastoise", imageUrl(9), listOf("Água")),
    PokemonListItem(25, "Pikachu", imageUrl(25), listOf("Elétrico")),
    PokemonListItem(39, "Jigglypuff", imageUrl(39), listOf("Normal", "Fada")),
    PokemonListItem(52, "Meowth", imageUrl(52), listOf("Normal")),
    PokemonListItem(92, "Gastly", imageUrl(92), listOf("Fantasma", "Veneno")),
    PokemonListItem(133, "Eevee", imageUrl(133), listOf("Normal")),
    PokemonListItem(150, "Mewtwo", imageUrl(150), listOf("Psíquico")),
)

private fun imageUrl(id: Int): String = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
