package com.example.pokedex_kmp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PokeApiClient(
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
) {
    private val baseUrl = "https://pokeapi.co/api/v2"

    suspend fun fetchInitialPokemonCache(limit: Int = 151): List<PokemonListItem> {
        val response: PokemonListResponse = httpClient.get("$baseUrl/pokemon?limit=$limit&offset=0").body()

        return coroutineScope {
            response.results.map { entry ->
                async {
                    val id = entry.idFromUrl()
                    runCatching { fetchPokemonSummary(id) }
                        .getOrElse {
                            PokemonListItem(
                                id = id,
                                name = entry.name.toPokemonDisplayName(),
                                imageUrl = officialArtworkUrl(id),
                                types = emptyList(),
                            )
                        }
                }
            }.map { it.await() }
                .sortedBy { it.id }
        }
    }

    suspend fun fetchPokemonDetail(id: Int): Pokemon {
        val detail: PokemonDetailResponse = httpClient.get("$baseUrl/pokemon/$id").body()
        val species = runCatching { fetchPokemonSpecies(id) }.getOrNull()

        return detail.toDomain(species)
    }

    private suspend fun fetchPokemonSummary(id: Int): PokemonListItem {
        val detail: PokemonDetailResponse = httpClient.get("$baseUrl/pokemon/$id").body()
        return PokemonListItem(
            id = detail.id,
            name = detail.name.toPokemonDisplayName(),
            imageUrl = detail.bestImageUrl() ?: officialArtworkUrl(detail.id),
            types = detail.types.map { translatePokemonType(it.type.name) },
        )
    }

    private suspend fun fetchPokemonSpecies(id: Int): PokemonSpeciesResponse {
        return httpClient.get("$baseUrl/pokemon-species/$id").body()
    }
}

@Serializable
private data class PokemonListResponse(
    val results: List<NamedApiResource>,
)

@Serializable
private data class NamedApiResource(
    val name: String,
    val url: String = "",
) {
    fun idFromUrl(): Int {
        return url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
    }
}

@Serializable
private data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int = 0,
    val weight: Int = 0,
    val types: List<TypeSlotResponse> = emptyList(),
    val abilities: List<AbilitySlotResponse> = emptyList(),
    val stats: List<StatSlotResponse> = emptyList(),
    val sprites: SpritesResponse = SpritesResponse(),
)

@Serializable
private data class TypeSlotResponse(
    val slot: Int = 0,
    val type: NamedApiResource,
)

@Serializable
private data class AbilitySlotResponse(
    val ability: NamedApiResource,
    @SerialName("is_hidden") val isHidden: Boolean = false,
)

@Serializable
private data class StatSlotResponse(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedApiResource,
)

@Serializable
private data class SpritesResponse(
    @SerialName("front_default") val frontDefault: String? = null,
    val other: OtherSpritesResponse? = null,
)

@Serializable
private data class OtherSpritesResponse(
    @SerialName("official-artwork") val officialArtwork: OfficialArtworkResponse? = null,
)

@Serializable
private data class OfficialArtworkResponse(
    @SerialName("front_default") val frontDefault: String? = null,
)

@Serializable
private data class PokemonSpeciesResponse(
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextResponse> = emptyList(),
    val genera: List<GeneraResponse> = emptyList(),
)

@Serializable
private data class FlavorTextResponse(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResource,
)

@Serializable
private data class GeneraResponse(
    val genus: String,
    val language: NamedApiResource,
)

private fun PokemonDetailResponse.toDomain(species: PokemonSpeciesResponse?): Pokemon {
    val displayTypes = types.map { translatePokemonType(it.type.name) }
    val firstAbility = abilities.firstOrNull { !it.isHidden }?.ability?.name
        ?: abilities.firstOrNull()?.ability?.name
        ?: "Não informado"

    val description = species?.flavorTextEntries
        ?.firstOrNull { it.language.name == "pt-BR" || it.language.name == "pt" }
        ?.flavorText
        ?: species?.flavorTextEntries?.firstOrNull { it.language.name == "en" }?.flavorText
        ?: "Descrição não disponível no momento."

    val category = species?.genera
        ?.firstOrNull { it.language.name == "pt-BR" || it.language.name == "pt" }
        ?.genus
        ?: species?.genera?.firstOrNull { it.language.name == "en" }?.genus
        ?: "Pokémon"

    return Pokemon(
        id = id,
        name = name.toPokemonDisplayName(),
        imageUrl = bestImageUrl() ?: officialArtworkUrl(id),
        types = displayTypes,
        heightMeters = height / 10.0,
        weightKg = weight / 10.0,
        description = description.cleanPokedexText(),
        ability = firstAbility.toPokemonDisplayName(),
        category = category.replace(" Pokémon", ""),
        stats = stats.map { PokemonStat(name = translateStatName(it.stat.name), value = it.baseStat) },
    )
}

private fun PokemonDetailResponse.bestImageUrl(): String? {
    return sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault
}

private fun officialArtworkUrl(id: Int): String {
    return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}

fun String.toPokemonDisplayName(): String {
    return split("-", "_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }
}

private fun String.cleanPokedexText(): String {
    return replace("\n", " ")
        .replace("\u000c", " ")
        .replace("  ", " ")
        .trim()
}

fun translatePokemonType(type: String): String = when (type.lowercase()) {
    "normal" -> "Normal"
    "fire" -> "Fogo"
    "water" -> "Água"
    "electric" -> "Elétrico"
    "grass" -> "Grama"
    "ice" -> "Gelo"
    "fighting" -> "Lutador"
    "poison" -> "Veneno"
    "ground" -> "Terra"
    "flying" -> "Voador"
    "psychic" -> "Psíquico"
    "bug" -> "Inseto"
    "rock" -> "Pedra"
    "ghost" -> "Fantasma"
    "dragon" -> "Dragão"
    "dark" -> "Sombrio"
    "steel" -> "Aço"
    "fairy" -> "Fada"
    else -> type.toPokemonDisplayName()
}

private fun translateStatName(stat: String): String = when (stat.lowercase()) {
    "hp" -> "HP"
    "attack" -> "Ataque"
    "defense" -> "Defesa"
    "special-attack" -> "Atk. Esp."
    "special-defense" -> "Def. Esp."
    "speed" -> "Velocidade"
    else -> stat.toPokemonDisplayName()
}
