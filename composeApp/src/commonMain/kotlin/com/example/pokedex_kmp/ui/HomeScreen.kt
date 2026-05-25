package com.example.pokedex_kmp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.example.pokedex_kmp.generated.resources.Res
import com.example.pokedex_kmp.generated.resources.treinador_masculino
import com.example.pokedex_kmp.generated.resources.treinadora_loira
import com.example.pokedex_kmp.generated.resources.treinadora_morena

private data class OnboardingPage(
    val images: List<DrawableResource>,
    val title: String,
    val description: String,
    val button: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onFinish: () -> Unit,
) {
    val pages = listOf(
        OnboardingPage(
            images = listOf(
                Res.drawable.treinadora_loira,
                Res.drawable.treinadora_morena,
            ),
            title = "Todos os Pokémons em um só Lugar",
            description = "Acesse uma vasta lista de Pokémon de todas as gerações já feitas pela Nintendo",
            button = "Continuar",
        ),
        OnboardingPage(
            images = listOf(
                Res.drawable.treinador_masculino,
            ),
            title = "Mantenha sua Pokédex atualizada",
            description = "Cadastre-se e mantenha seu perfil, pokémon favoritos, configurações e muito mais, salvos no aplicativo, mesmo sem conexão com a internet.",
            button = "Vamos começar!",
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 26.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        Row(
            modifier = Modifier.padding(bottom = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 9.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                Color(0xFFD3222A)
                            } else {
                                Color(0xFFE2E5EA)
                            },
                        ),
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3222A)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        ) {
            Text(
                text = pages[pagerState.currentPage].button,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingImages(images = page.images)

        Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall,
            lineHeight = 42.sp,
            color = Color(0xFF111827),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 25.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun OnboardingImages(
    images: List<DrawableResource>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(bottom = 10.dp),
        horizontalArrangement = if (images.size > 1) {
            Arrangement.spacedBy((-100).dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.Center
        },
        verticalAlignment = Alignment.Bottom,
    ) {
        images.forEach { imageResource ->
            Box(
                modifier = Modifier.size(
                    width = if (images.size > 1) 220.dp else 260.dp,
                    height = 320.dp,
                ),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = "Personagem do onboarding",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
