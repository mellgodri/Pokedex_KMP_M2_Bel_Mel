package com.example.pokedex_kmp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex_kmp.data.PokedexRepository
import com.example.pokedex_kmp.data.PokemonListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PageSize = 24

data class PokedexUiState(
    val items: List<PokemonListItem> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val query: String = "",
    val selectedType: String = "Todos os tipos",
    val isInitialLoading: Boolean = true,
    val isPageLoading: Boolean = false,
    val canLoadMore: Boolean = true,
    val errorMessage: String? = null,
)

class PokedexViewModel(
    private val repository: PokedexRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    private var currentOffset = 0
    private var isLoading = false

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            currentOffset = 0
            isLoading = true
            _uiState.update { it.copy(isInitialLoading = true, errorMessage = null, items = emptyList(), canLoadMore = true) }

            runCatching {
                repository.ensureInitialSync()
                repository.getPokemonPage(
                    query = _uiState.value.query,
                    type = _uiState.value.selectedType,
                    limit = PageSize,
                    offset = 0,
                )
            }.onSuccess { page ->
                currentOffset = page.size
                _uiState.update {
                    it.copy(
                        items = page,
                        favoriteIds = repository.getFavoriteIds(),
                        isInitialLoading = false,
                        isPageLoading = false,
                        canLoadMore = page.size == PageSize,
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isPageLoading = false,
                        canLoadMore = false,
                        errorMessage = error.message ?: "Erro ao carregar a Pokédex.",
                    )
                }
            }

            isLoading = false
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (isLoading || state.isInitialLoading || state.isPageLoading || !state.canLoadMore) return

        viewModelScope.launch {
            isLoading = true
            _uiState.update { it.copy(isPageLoading = true, errorMessage = null) }

            runCatching {
                repository.getPokemonPage(
                    query = _uiState.value.query,
                    type = _uiState.value.selectedType,
                    limit = PageSize,
                    offset = currentOffset,
                )
            }.onSuccess { page ->
                currentOffset += page.size
                _uiState.update {
                    it.copy(
                        items = it.items + page,
                        favoriteIds = repository.getFavoriteIds(),
                        isPageLoading = false,
                        canLoadMore = page.size == PageSize,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPageLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar mais Pokémons.",
                    )
                }
            }

            isLoading = false
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(query = value) }
        refresh()
    }

    fun onTypeSelected(type: String) {
        _uiState.update { it.copy(selectedType = type) }
        refresh()
    }
}
