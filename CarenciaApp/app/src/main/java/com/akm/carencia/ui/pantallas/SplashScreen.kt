package com.akm.carencia.ui.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akm.carencia.R
import com.akm.carencia.ui.componentes.Logo
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// P3 · Bienvenida (portada / splash)
@Composable
fun SplashScreen(onListo: () -> Unit) {
    val aparicion = remember { Animatable(0f) }
    val glitch = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { aparicion.animateTo(1f, tween(900)) }
        // Pequeño temblor del espejo, como si algo lo cruzara
        repeat(2) {
            delay(500)
            glitch.animateTo(1f, tween(60)); glitch.animateTo(-1f, tween(60)); glitch.animateTo(0f, tween(60))
        }
        delay(900)
        onListo()
    }

    val espejo = RoundedCornerShape(topStart = 110.dp, topEnd = 110.dp, bottomStart = 24.dp, bottomEnd = 24.dp)

    Box(Modifier.fillMaxSize()) {
        // "Ambiente": la escena del baño desenfocada (ya viene con blur y opacidad del Figma)
        Image(
            painterResource(R.drawable.bienvenida_ambiente), null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier.fillMaxSize().navigationBarsPadding().alpha(aparicion.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Image(
                painterResource(R.drawable.bienvenida_espejo), "El espejo del baño",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(220.dp, 300.dp)
                    .graphicsLayer { translationX = glitch.value * 6f }
                    .clip(espejo)
            )
            Spacer(Modifier.height(36.dp))
            Logo()
            Spacer(Modifier.height(10.dp))
            Text("Nadie recuerda que tuviste una…", style = T.elite(15, C.Gris))
            Spacer(Modifier.weight(1.3f))
            Text("Demo v0.1, Estudio3", style = T.elite(12, C.Gris))
            Spacer(Modifier.height(40.dp))
        }
    }
}
