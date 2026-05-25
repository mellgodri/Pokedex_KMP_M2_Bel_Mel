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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pokedex_kmp.data.FavoritePokemon
import com.example.pokedex_kmp.ui.components.TypeChip
import com.example.pokedex_kmp.viewmodel.FavoritesUiState

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onPokemonClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 26.dp),
    ) {
        Spacer(Modifier.height(54.dp))

        Text(
            text = "Favoritos",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${state.items.size} Pokémons salvos",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFADB3BD),
        )

        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD3222A))
            }

            state.items.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum favorito salvo ainda.", color = Color(0xFF6B7280))
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.id }) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        onClick = { onPokemonClick(favorite.id) },
                        onRemove = { onRemove(favorite.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: FavoritePokemon,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val mainColor = pokemonTypeColor(favorite.types.firstOrNull().orEmpty())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(mainColor.copy(alpha = 0.12f))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp, end = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = favorite.id.formatPokemonNumber(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = favorite.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                favorite.types.take(2).forEach { TypeChip(it) }
            }
            Text(
                text = "Capturado em: ${favorite.capturedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .height(118.dp)
                .size(width = 128.dp, height = 118.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .background(mainColor),
        ) {
            AsyncImage(
                model = favorite.imageUrl,
                contentDescription = favorite.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(92.dp),
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = if (favorite.id > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Remover",
                    tint = Color.White,
                )
            }
        }
    }
}
