package com.example.pokedex_kmp.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.pokedex_kmp.data.Pokemon
import com.example.pokedex_kmp.ui.components.StatIndicator
import com.example.pokedex_kmp.ui.components.TypeChip
import com.example.pokedex_kmp.viewmodel.PokemonDetailUiState

@Composable
fun PokemonDetailScreen(
    state: PokemonDetailUiState,
    onBackClick: () -> Unit,
    onCapturedAtChange: (String) -> Unit,
    onSaveFavorite: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onDismissLimitError: () -> Unit,
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD3222A))
            }
        }

        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.errorMessage, color = Color(0xFFD3222A), textAlign = TextAlign.Center)
                    Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3222A))) {
                        Text("Voltar")
                    }
                }
            }
        }

        state.limitErrorMessage != null -> PokemonLimitErrorScreen(
            message = state.limitErrorMessage,
            onDismiss = onDismissLimitError,
        )

        state.pokemon != null -> DetailContent(
            state = state,
            onBackClick = onBackClick,
            onCapturedAtChange = onCapturedAtChange,
            onSaveFavorite = onSaveFavorite,
            onRemoveFavorite = onRemoveFavorite,
        )
    }
}

@Composable
private fun DetailContent(
    state: PokemonDetailUiState,
    onBackClick: () -> Unit,
    onCapturedAtChange: (String) -> Unit,
    onSaveFavorite: () -> Unit,
    onRemoveFavorite: () -> Unit,
) {
    val pokemon = state.pokemon ?: return
    val mainColor = pokemonTypeColor(pokemon.types.firstOrNull().orEmpty())
    val isFavorite = state.favorite != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(mainColor)
                    .padding(top = 44.dp, start = 22.dp, end = 22.dp),
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }

                IconButton(
                    onClick = { if (isFavorite) onRemoveFavorite() else onSaveFavorite() },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }

                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 64.dp)
                        .size(240.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 52.dp, topEnd = 52.dp))
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = pokemon.id.formatPokemonNumber(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7B8494),
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = pokemon.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pokemon.types.forEach { TypeChip(type = it) }
                }

                Text(
                    text = pokemon.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 25.sp,
                    color = Color(0xFF5F6673),
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DetailMiniCard("PESO", "${pokemon.weightKg} kg", Modifier.weight(1f))
                    DetailMiniCard("ALTURA", "${pokemon.heightMeters} m", Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DetailMiniCard("CATEGORIA", pokemon.category, Modifier.weight(1f))
                    DetailMiniCard("HABILIDADE", pokemon.ability, Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = state.capturedAtInput,
                    onValueChange = onCapturedAtChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Onde foi capturado?") },
                    placeholder = { Text("Ex: Itajaí, Praia Brava, casa...") },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainColor,
                        unfocusedBorderColor = Color(0xFFE0E4EA),
                    ),
                )

                if (state.validationMessage != null) {
                    Text(
                        text = state.validationMessage,
                        color = Color(0xFFD3222A),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = if (isFavorite) onRemoveFavorite else onSaveFavorite,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) Color(0xFF252525) else Color(0xFFD3222A)),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (isFavorite) "Remover dos favoritos" else "Salvar nos favoritos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                if (isFavorite) {
                    Text(
                        text = "Capturado em: ${state.favorite.capturedAt}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5F6673),
                    )
                }

                if (pokemon.stats.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ESTATÍSTICAS", fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                            pokemon.stats.forEach { stat -> StatIndicator(stat) }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}


@Composable
private fun PokemonLimitErrorScreen(
    message: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(44.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD3222A),
                )
            }

            Text(
                text = "Limite atingido",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Sua Pokédex permite salvar no máximo 6 Pokémons.",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 25.sp,
                color = Color(0xFF5F6673),
                textAlign = TextAlign.Center,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = Color(0xFF7B8494),
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3222A)),
            ) {
                Text("Entendi", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun DetailMiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7B8494))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), textAlign = TextAlign.Center)
        }
    }
}
