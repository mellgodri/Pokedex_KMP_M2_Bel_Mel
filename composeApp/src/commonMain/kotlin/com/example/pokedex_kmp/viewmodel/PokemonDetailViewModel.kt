package com.example.pokedex_kmp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex_kmp.data.FavoritePokemon
import com.example.pokedex_kmp.data.PokedexRepository
import com.example.pokedex_kmp.data.PokemonTeamLimitException
import com.example.pokedex_kmp.data.Pokemon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PokemonDetailUiState(
    val pokemon: Pokemon? = null,
    val favorite: FavoritePokemon? = null,
    val capturedAtInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val validationMessage: String? = null,
    val limitErrorMessage: String? = null,
)

class PokemonDetailViewModel(
    private val repository: PokedexRepository,
    private val pokemonId: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val favorite = repository.getFavoriteById(pokemonId)
                repository.getPokemonDetail(pokemonId) to favorite
            }.onSuccess { (pokemon, favorite) ->
                _uiState.update {
                    it.copy(
                        pokemon = pokemon,
                        favorite = favorite,
                        capturedAtInput = favorite?.capturedAt.orEmpty(),
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar detalhes pela PokeAPI.",
                    )
                }
            }
        }
    }

    fun onCapturedAtChange(value: String) {
        _uiState.update { it.copy(capturedAtInput = value, validationMessage = null) }
    }

    fun dismissLimitError() {
        _uiState.update { it.copy(limitErrorMessage = null) }
    }

    fun saveFavorite() {
        val pokemon = _uiState.value.pokemon ?: return
        val location = _uiState.value.capturedAtInput.trim()

        if (location.isBlank()) {
            _uiState.update { it.copy(validationMessage = "Informe onde o Pokémon foi capturado.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationMessage = null, limitErrorMessage = null) }
            runCatching {
                repository.saveFavorite(pokemon, location)
                repository.getFavoriteById(pokemon.id)
            }.onSuccess { favorite ->
                _uiState.update { it.copy(favorite = favorite, isSaving = false) }
            }.onFailure { error ->
                if (error is PokemonTeamLimitException) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            limitErrorMessage = error.message ?: "Voce ja atingiu o limite de 6 Pokemons salvos.",
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            validationMessage = error.message ?: "Erro ao salvar favorito.",
                        )
                    }
                }
            }
        }
    }

    fun removeFavorite() {
        val id = _uiState.value.pokemon?.id ?: return
        viewModelScope.launch {
            repository.deleteFavorite(id)
            _uiState.update { it.copy(favorite = null, capturedAtInput = "") }
        }
    }
}
