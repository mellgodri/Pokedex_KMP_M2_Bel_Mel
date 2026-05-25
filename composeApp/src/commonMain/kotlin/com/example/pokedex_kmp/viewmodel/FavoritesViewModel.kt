package com.example.pokedex_kmp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex_kmp.data.FavoritePokemon
import com.example.pokedex_kmp.data.PokedexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val items: List<FavoritePokemon> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class FavoritesViewModel(
    private val repository: PokedexRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getFavorites() }
                .onSuccess { favorites ->
                    _uiState.update { it.copy(items = favorites, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro ao carregar favoritos.",
                        )
                    }
                }
        }
    }

    fun remove(id: Int) {
        viewModelScope.launch {
            repository.deleteFavorite(id)
            load()
        }
    }
}
