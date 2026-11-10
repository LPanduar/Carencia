package com.akm.carencia.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akm.carencia.CarenciaApp
import com.akm.carencia.data.Nivel
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.EncabezadoSeccion
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.ListaConPuntos
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Portada
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.TituloPantalla
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.componentes.toque
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

// ============================ P3 · Niveles ============================
class NivelesViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<List<Nivel>>>(Estado.Cargando); private set
    init { cargar() }
    fun cargar() = viewModelScope.launch {
        estado = Estado.Cargando
        estado = CarenciaApp.instancia.repo.niveles().aEstado()
    }
}

@Composable
fun NivelesScreen(onNivel: (Int) -> Unit, vm: NivelesViewModel = viewModel()) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = EspacioBarra),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(top = 40.dp)) {
                TituloPantalla("Niveles")
                Spacer(Modifier.height(14.dp))
                // Cita con línea roja
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(C.Rojo))
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "¿Realmente está viajando entre dimensiones o todo ocurre dentro de su mente?",
                        style = T.elite(16, C.Texto, 1.55f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        when (val e = vm.estado) {
            is Estado.Cargando -> item { Cargando() }
            is Estado.Error -> item { MensajeError(e.mensaje, { vm.cargar() }) }
            is Estado.Listo -> items(e.data, key = { it.pkNivel }) { n -> NivelGrande(n) { onNivel(n.pkNivel) } }
        }
    }
}

// App 3 / Nivel grande: la imagen manda, número de nivel grande y tenue
@Composable
private fun NivelGrande(n: Nivel, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(C.Superficie)
            .toque(onClick)
    ) {
        ImagenRemota(n.imagenTarjeta, Modifier.fillMaxSize(), alineacion = Alignment.BottomCenter, descripcion = n.titulo)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.25f to C.Fondo.copy(alpha = 0.05f), 1f to C.Fondo.copy(alpha = 0.92f))
            )
        )
        Text(
            n.numero.toString(), style = T.barrio(150),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-12).dp, y = (-18).dp).alpha(0.16f)
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Etiqueta("${n.duracionMin} min")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nivel ${n.numero}", style = T.elite(14, C.RojoClaro))
                Text(n.titulo, style = T.barrio(28))
            }
        }
    }
}

// ============================ P3 · Nivel 1/2/3 ============================
class NivelDetalleViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var estado by mutableStateOf<Estado<Nivel>>(Estado.Cargando); private set
    var todos by mutableStateOf<List<Nivel>>(emptyList()); private set
    private var cargado = -1

    fun cargar(id: Int, forzar: Boolean = false) {
        if (cargado == id && !forzar) return
        cargado = id
        viewModelScope.launch {
            estado = Estado.Cargando
            estado = repo.nivel(id).aEstado()
            if (todos.isEmpty()) (repo.niveles() as? Resultado.Ok)?.let { todos = it.data.sortedBy { n -> n.numero } }
        }
    }
}

private val TiposAmbiente = listOf("Personajes", "Mecánicas", "Controles", "Tecnologías")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NivelDetalleScreen(
    id: Int,
    onVolver: () -> Unit,
    onNivel: (Int) -> Unit,
    onVerNiveles: () -> Unit,
    onVerGaleria: () -> Unit,
    vm: NivelDetalleViewModel = viewModel(key = "nivel_$id")
) {
    vm.cargar(id)
    var tipo by rememberSaveable { mutableStateOf("Personajes") }
    val lado = Modifier.padding(horizontal = 24.dp)

    Box(Modifier.fillMaxSize()) {
        when (val e = vm.estado) {
            is Estado.Cargando -> Cargando(Modifier.padding(top = 200.dp))
            is Estado.Error -> MensajeError(e.mensaje, { vm.cargar(id, true) }, Modifier.padding(top = 160.dp))
            is Estado.Listo -> {
                val n = e.data
                val indice = vm.todos.indexOfFirst { it.pkNivel == n.pkNivel }
                val anterior = vm.todos.getOrNull(indice - 1)
                val siguiente = vm.todos.getOrNull(indice + 1)

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box {
                        Portada(440.dp) {
                            ImagenRemota(n.imagenPortada, Modifier.fillMaxSize(), alineacion = Alignment.BottomCenter)
                        }
                        Text(n.titulo, style = T.barrio(38), modifier = lado.padding(top = 330.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Spacer(Modifier.height(0.dp))
                        FlowRow(
                            lado.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Etiqueta("${n.duracionMin} min")
                            Etiqueta("Horror corporal", acento = true)
                            Etiqueta("Point & click", acento = true)
                        }
                        Text(n.descripcion, style = T.Cuerpo, modifier = lado)

                        val capturas = n.capturas.orEmpty()
                        if (capturas.isNotEmpty()) {
                            EncabezadoSeccion("Capturas", "Ver todo", onVerGaleria, lado)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(capturas, key = { it.pkNivelCaptura }) {
                                    ImagenRemota(
                                        it.imagenUrl,
                                        Modifier.size(248.dp, 150.dp).clip(RoundedCornerShape(16.dp)),
                                        alineacion = Alignment.BottomCenter
                                    )
                                }
                            }
                        }

                        Text("Ambiente", style = T.elite(20), modifier = lado)
                        Segmentos(TiposAmbiente, tipo, { tipo = it })
                        val filas = n.detalles.orEmpty().filter { it.tipo == tipo }.sortedBy { it.orden }.map { it.texto }
                        ListaConPuntos(filas.ifEmpty { listOf("Sin información todavía") }, lado)

                        // Navegación entre niveles
                        Row(lado.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (anterior != null) {
                                Boton("Nivel anterior", Modifier.weight(1f), secundario = true) { onNivel(anterior.pkNivel) }
                            }
                            if (siguiente != null) {
                                Boton("Siguiente nivel", Modifier.weight(1f)) { onNivel(siguiente.pkNivel) }
                            } else if (vm.todos.isNotEmpty()) {
                                Boton("Ver niveles", Modifier.weight(1f), onClick = onVerNiveles)
                            }
                        }
                        Spacer(Modifier.height(EspacioBarra))
                    }
                }
            }
        }
        Volver(onVolver, Modifier.statusBarsPadding().padding(start = 24.dp, top = 8.dp))
    }
}
