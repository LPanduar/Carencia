package com.akm.carencia.ui.pantallas

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.notificaciones.Notificaciones
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.EncabezadoSeccion
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ListaConPuntos
import com.akm.carencia.ui.componentes.Logo
import com.akm.carencia.ui.componentes.PortadaLocal
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T

private val Lado = Modifier.padding(horizontal = 24.dp)

// P3 · Inicio  +  P3 · Inicio extendido (Acerca de) en una sola pantalla con scroll
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InicioScreen(onVerGaleria: () -> Unit, onPerfil: () -> Unit) {
    var pestana by rememberSaveable { mutableStateOf("Personajes") }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box {
                PortadaLocal(R.drawable.inicio_portada, 470.dp)
                Column(Modifier.padding(top = 344.dp)) {
                    Logo(Lado)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Spacer(Modifier.height(0.dp))
                Row(Lado, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Etiqueta("PC"); Etiqueta("20 min"); Etiqueta("Demo v0.1")
                }
                Text(
                    "Un joven de 17 años busca a su hermana, cuya existencia niega su propia familia, " +
                        "cruzando portales que se abren desde el espejo de su baño hacia dimensiones cada vez más deformadas.",
                    style = T.Cuerpo, modifier = Lado
                )
                FlowRow(
                    Lado.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Etiqueta("Terror", acento = true)
                    Etiqueta("Horror corporal", acento = true)
                    Etiqueta("Point & click", acento = true)
                }
                EncabezadoSeccion("Capturas", "Ver todo", onVerGaleria, Lado)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf(R.drawable.inicio_cap1, R.drawable.inicio_cap2, R.drawable.inicio_cap3)) {
                        Image(
                            painterResource(it), null, contentScale = ContentScale.Crop,
                            modifier = Modifier.size(248.dp, 150.dp).clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                // ---------- Acerca de ----------
                Text("Acerca de", style = T.barrio(32), modifier = Lado.padding(top = 8.dp))
                Segmentos(
                    listOf("Personajes", "Mecánicas", "Controles", "Tecnologías"),
                    pestana, { pestana = it }
                )
                Column(Lado, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    when (pestana) {
                        "Personajes" -> Personajes()
                        "Mecánicas" -> ListaConPuntos(
                            listOf(
                                "Point & click: explora e interactúa",
                                "El espejo del baño abre portales",
                                "Cada dimensión está más deformada"
                            )
                        )
                        "Controles" -> ListaConPuntos(
                            listOf("Clic: moverse e interactuar", "Esc: pausa y opciones")
                        )
                        else -> ListaConPuntos(
                            listOf(
                                "Videojuego para PC",
                                "App: Android · Jetpack Compose",
                                "API: ASP.NET · Dapper · SQL Server"
                            )
                        )
                    }
                }
                Spacer(Modifier.height(EspacioBarra))
            }
        }

        BotonCircular(
            R.drawable.ic_perfil, "Perfil", onPerfil,
            Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 8.dp, end = 24.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Personajes() {
    Tarjeta(padding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painterResource(R.drawable.acerca_protagonista), "Mario Cruz",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(16.dp))
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Mario Cruz", style = T.elite(22), modifier = Modifier.weight(1f))
                Etiqueta("Protagonista", acento = true)
            }
            Text(
                "Tiene 17 años. Es el único que recuerda a su hermana y está dispuesto a cruzar el espejo para encontrarla.",
                style = T.elite(14, C.Texto, 1.55f)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("Capricornio"); Etiqueta("Mexicano"); Etiqueta("Tranquilo")
            }
        }
    }
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.acerca_cartel), "Cartel de desaparecida",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(96.dp, 121.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Margarita", style = T.elite(22))
                Etiqueta("Desaparecida", acento = true)
                Text("16 años. Su familia dice que nunca existió.", style = T.elite(14, C.Texto, 1.55f))
            }
        }
    }
}

