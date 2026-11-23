package com.akm.carencia.ui.pantallas

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.AcercaItem
import com.akm.carencia.data.Inicio
import com.akm.carencia.data.InicioEtiqueta
import com.akm.carencia.data.Personaje
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.EncabezadoSeccion
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.ListaConPuntos
import com.akm.carencia.ui.componentes.Logo
import com.akm.carencia.ui.componentes.PortadaLocal
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.componentes.Vacio
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

private val Lado = Modifier.padding(horizontal = 24.dp)

// Contenido de Inicio: viene de GET api/Inicio (lo edita el admin). Si no hay conexión
// se muestra el contenido original para que la pantalla nunca quede vacía.
class InicioViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var inicio by mutableStateOf(InicioRespaldo.datos); private set
    var capturas by mutableStateOf<List<String>>(emptyList()); private set

    fun cargar() = viewModelScope.launch {
        (repo.inicio() as? Resultado.Ok)?.let { inicio = it.data }
        // Las capturas de Inicio son las 3 primeras fotos de la galería oficial
        (repo.capturas(null, null) as? Resultado.Ok)?.let { r -> capturas = r.data.take(3).map { it.imagenUrl } }
    }
}

// P3 · Inicio  +  P3 · Inicio extendido (Acerca de) en una sola pantalla con scroll
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InicioScreen(onVerGaleria: () -> Unit, onPerfil: () -> Unit, vm: InicioViewModel = viewModel()) {
    var pestana by rememberSaveable { mutableStateOf("Personajes") }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    val inicio = vm.inicio

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
                if (inicio.info.isNotEmpty()) {
                    FlowRow(
                        Lado.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { inicio.info.forEach { Etiqueta(it.texto) } }
                }
                Text(inicio.descripcion, style = T.Cuerpo, modifier = Lado)
                if (inicio.generos.isNotEmpty()) {
                    FlowRow(
                        Lado.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { inicio.generos.forEach { Etiqueta(it.texto, acento = true) } }
                }
                EncabezadoSeccion("Capturas", "Ver todo", onVerGaleria, Lado)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val forma = Modifier.size(248.dp, 150.dp).clip(RoundedCornerShape(16.dp))
                    if (vm.capturas.isNotEmpty()) {
                        items(vm.capturas) { ImagenRemota(it, forma) }
                    } else {
                        items(listOf(R.drawable.inicio_cap1, R.drawable.inicio_cap2, R.drawable.inicio_cap3)) {
                            Image(painterResource(it), null, contentScale = ContentScale.Crop, modifier = forma)
                        }
                    }
                }

                // ---------- Acerca de ----------
                Text("Acerca de", style = T.barrio(32), modifier = Lado.padding(top = 8.dp))
                Segmentos(
                    listOf("Personajes") + com.akm.carencia.data.Categorias.Acerca,
                    pestana, { pestana = it }
                )
                Column(Lado, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (pestana == "Personajes") {
                        if (inicio.personajes.isEmpty()) Vacio("Todavía no hay personajes.")
                        inicio.personajes.forEachIndexed { i, p ->
                            if (i == 0) PersonajePrincipal(p) else PersonajeCompacto(p)
                        }
                    } else {
                        val items = inicio.seccion(pestana).map { it.texto }
                        if (items.isEmpty()) Vacio("Todavía no hay nada aquí.") else ListaConPuntos(items)
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

private fun Personaje.tieneImagen() = !imagenUrl.isNullOrBlank() || InicioRespaldo.imagenLocal(this) != null

// Imagen del personaje: la del servidor o, sin conexión, la que trae la app
@Composable
private fun ImagenPersonaje(p: Personaje, modifier: Modifier) {
    val local = InicioRespaldo.imagenLocal(p)
    if (local != null) {
        Image(painterResource(local), p.nombre, contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        ImagenRemota(p.imagenUrl, modifier, descripcion = p.nombre)
    }
}

// Primer personaje (protagonista): tarjeta grande
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonajePrincipal(p: Personaje) {
    Tarjeta(padding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (p.tieneImagen()) {
                ImagenPersonaje(p, Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(16.dp)))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(p.nombre, style = T.elite(22), modifier = Modifier.weight(1f))
                Etiqueta(p.rol, acento = true)
            }
            Text(p.descripcion, style = T.elite(14, C.Texto, 1.55f))
            if (p.listaRasgos.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.listaRasgos.forEach { Etiqueta(it) }
                }
            }
        }
    }
}

// Resto de personajes: tarjeta horizontal
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonajeCompacto(p: Personaje) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (p.tieneImagen()) {
                ImagenPersonaje(p, Modifier.size(96.dp, 121.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(Modifier.width(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(p.nombre, style = T.elite(22))
                Etiqueta(p.rol, acento = true)
                Text(p.descripcion, style = T.elite(14, C.Texto, 1.55f))
                if (p.listaRasgos.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        p.listaRasgos.forEach { Etiqueta(it) }
                    }
                }
            }
        }
    }
}

// Contenido original de la app (se usa mientras carga o si no hay conexión)
object InicioRespaldo {
    // Ids negativos = datos locales (no existen en el servidor)
    private const val MARIO = -1
    private const val MARGARITA = -2

    fun imagenLocal(p: Personaje): Int? = when (p.pkPersonaje) {
        MARIO -> R.drawable.acerca_protagonista
        MARGARITA -> R.drawable.acerca_cartel
        else -> null
    }

    val datos = Inicio(
        descripcion = "Un joven de 17 años busca a su hermana, cuya existencia niega su propia familia, " +
            "cruzando portales que se abren desde el espejo de su baño hacia dimensiones cada vez más deformadas.",
        etiquetas = listOf(
            InicioEtiqueta(-1, "Info", "PC", 1), InicioEtiqueta(-2, "Info", "20 min", 2),
            InicioEtiqueta(-3, "Info", "Demo v0.1", 3),
            InicioEtiqueta(-4, "Genero", "Terror", 1), InicioEtiqueta(-5, "Genero", "Horror corporal", 2),
            InicioEtiqueta(-6, "Genero", "Point & click", 3)
        ),
        personajes = listOf(
            Personaje(
                MARIO, "Mario Cruz", "Protagonista",
                "Tiene 17 años. Es el único que recuerda a su hermana y está dispuesto a cruzar el espejo para encontrarla.",
                "Capricornio, Mexicano, Tranquilo", null, 1
            ),
            Personaje(MARGARITA, "Margarita", "Desaparecida", "16 años. Su familia dice que nunca existió.", null, null, 2)
        ),
        acerca = listOf(
            AcercaItem(-1, "Mecánicas", "Point & click: explora e interactúa", 1),
            AcercaItem(-2, "Mecánicas", "El espejo del baño abre portales", 2),
            AcercaItem(-3, "Mecánicas", "Cada dimensión está más deformada", 3),
            AcercaItem(-4, "Controles", "Clic: moverse e interactuar", 1),
            AcercaItem(-5, "Controles", "Esc: pausa y opciones", 2),
            AcercaItem(-6, "Tecnologías", "Videojuego para PC", 1),
            AcercaItem(-7, "Tecnologías", "App: Android · Jetpack Compose", 2),
            AcercaItem(-8, "Tecnologías", "API: ASP.NET · Dapper · SQL Server", 3)
        )
    )
}
