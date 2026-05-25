package com.example.pokedex_kmp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pokedex_kmp.data.AppGraph
import com.example.pokedex_kmp.navigation.FavoritesRoute
import com.example.pokedex_kmp.navigation.HomeRoute
import com.example.pokedex_kmp.navigation.PokedexRoute
import com.example.pokedex_kmp.navigation.PokemonDetailRoute
import com.example.pokedex_kmp.ui.FavoritesScreen
import com.example.pokedex_kmp.ui.HomeScreen
import com.example.pokedex_kmp.ui.PokedexGridScreen
import com.example.pokedex_kmp.ui.PokemonDetailScreen
import com.example.pokedex_kmp.viewmodel.FavoritesViewModel
import com.example.pokedex_kmp.viewmodel.PokedexViewModel
import com.example.pokedex_kmp.viewmodel.PokemonDetailViewModel

private data class BottomDestination(
    val label: String,
    val route: Any,
    val icon: ImageVector,
)

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val repository = remember { AppGraph.repository }
        val pokedexViewModel = remember { PokedexViewModel(repository) }
        val favoritesViewModel = remember { FavoritesViewModel(repository) }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val showBottomBar = currentDestination?.hasRoute(PokedexRoute::class) == true ||
            currentDestination?.hasRoute(FavoritesRoute::class) == true

        val bottomDestinations = listOf(
            BottomDestination(label = "Pokédex", route = PokedexRoute, icon = Icons.Default.CatchingPokemon),
            BottomDestination(label = "Favoritos", route = FavoritesRoute, icon = Icons.Default.Favorite),
        )

        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 4.dp,
                    ) {
                        bottomDestinations.forEach { destination ->
                            val selected = currentDestination?.hasRoute(destination.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(PokedexRoute) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                    )
                                },
                                label = { Text(destination.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFD3222A),
                                    indicatorColor = Color(0xFFD3222A),
                                    unselectedIconColor = Color(0xFF9CA3AF),
                                    unselectedTextColor = Color(0xFF9CA3AF),
                                ),
                            )
                        }
                    }
                }
            },
        ) { _ ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        onFinish = {
                            navController.navigate(PokedexRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                    )
                }

                composable<PokedexRoute> {
                    val state by pokedexViewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        favoritesViewModel.load()
                    }

                    PokedexGridScreen(
                        state = state,
                        onSearchChange = pokedexViewModel::onSearchChange,
                        onTypeSelected = pokedexViewModel::onTypeSelected,
                        onLoadMore = pokedexViewModel::loadNextPage,
                        onPokemonClick = { id -> navController.navigate(PokemonDetailRoute(id)) },
                    )
                }

                composable<PokemonDetailRoute> { entry ->
                    val route = entry.toRoute<PokemonDetailRoute>()
                    val detailViewModel = remember(route.pokemonId) {
                        PokemonDetailViewModel(repository, route.pokemonId)
                    }
                    val state by detailViewModel.uiState.collectAsState()

                    PokemonDetailScreen(
                        state = state,
                        onBackClick = {
                            pokedexViewModel.refresh()
                            favoritesViewModel.load()
                            navController.popBackStack()
                        },
                        onCapturedAtChange = detailViewModel::onCapturedAtChange,
                        onSaveFavorite = {
                            detailViewModel.saveFavorite()
                            pokedexViewModel.refresh()
                            favoritesViewModel.load()
                        },
                        onRemoveFavorite = {
                            detailViewModel.removeFavorite()
                            pokedexViewModel.refresh()
                            favoritesViewModel.load()
                        },
                        onDismissLimitError = detailViewModel::dismissLimitError,
                    )
                }

                composable<FavoritesRoute> {
                    val state by favoritesViewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        favoritesViewModel.load()
                    }

                    FavoritesScreen(
                        state = state,
                        onPokemonClick = { id -> navController.navigate(PokemonDetailRoute(id)) },
                        onRemove = { id ->
                            favoritesViewModel.remove(id)
                            pokedexViewModel.refresh()
                        },
                    )
                }
            }
        }
    }
}
