package com.example.pokedex_kmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pokedex_kmp.data.PokemonListItem
import com.example.pokedex_kmp.data.PokemonTypeFilters
import com.example.pokedex_kmp.viewmodel.PokedexUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexGridScreen(
    state: PokedexUiState,
    onSearchChange: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onLoadMore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
) {
    var showTypeSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Pokédex",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827),
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                singleLine = true,
                placeholder = { Text("Procurar Pokémon...", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6B7280)) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0E4EA),
                    unfocusedBorderColor = Color(0xFFE0E4EA),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = { showTypeSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525)),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(state.selectedType, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                state.isInitialLoading -> LoadingState()
                state.errorMessage != null && state.items.isEmpty() -> ErrorState(state.errorMessage)
                state.items.isEmpty() -> EmptyState()
                else -> PokemonGrid(
                    state = state,
                    onLoadMore = onLoadMore,
                    onPokemonClick = onPokemonClick,
                )
            }
        }

        if (showTypeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTypeSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            ) {
                TypeFilterSheet(
                    selectedType = state.selectedType,
                    onTypeSelected = { type ->
                        onTypeSelected(type)
                        showTypeSheet = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    state: PokedexUiState,
    onLoadMore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 108.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(state.items, key = { _, pokemon -> pokemon.id }) { index, pokemon ->
            if (index >= state.items.lastIndex - 4) {
                LaunchedEffect(state.items.size, state.query, state.selectedType) {
                    onLoadMore()
                }
            }

            PokemonGridCard(
                pokemon = pokemon,
                isFavorite = state.favoriteIds.contains(pokemon.id),
                onClick = { onPokemonClick(pokemon.id) },
            )
        }

        if (state.isPageLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFFD3222A))
                }
            }
        }
    }
}

@Composable
private fun PokemonGridCard(
    pokemon: PokemonListItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE2E5EA))
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(27.dp),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 14.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(88.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = pokemon.id.formatPokemonNumber(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                )
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TypeFilterSheet(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Selecione o tipo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827),
        )

        Spacer(Modifier.height(8.dp))

        PokemonTypeFilters.forEach { type ->
            val isAll = type == "Todos os tipos"
            val background = if (isAll) Color(0xFF252525) else pokemonTypeColor(type)
            val textColor = if (isAll) Color.White else pokemonTypeTextColor(type)
            Button(
                onClick = { onTypeSelected(type) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = background),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selectedType == type) 4.dp else 0.dp),
            ) {
                Text(type, fontWeight = FontWeight.Bold, color = textColor)
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = Color(0xFFD3222A))
            Text("Sincronizando Pokédex...", color = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color(0xFFD3222A), textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhum Pokémon encontrado.", color = Color(0xFF6B7280))
    }
}
