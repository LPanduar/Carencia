package com.akm.carencia.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.Novedad
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.Fechas
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.BotonFlotante
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.TituloPantalla
import com.akm.carencia.ui.componentes.Vacio
import com.akm.carencia.ui.componentes.toque
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NovedadesViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var estado by mutableStateOf<Estado<List<Novedad>>>(Estado.Cargando); private set
    var filtro by mutableStateOf("Todo"); private set
    var busqueda by mutableStateOf(""); private set
    private var job: Job? = null

    fun cambiarFiltro(f: String) { filtro = f; cargar() }

    fun buscar(t: String) {
        busqueda = t
        job?.cancel()
        job = viewModelScope.launch { delay(350); cargarAhora() }
    }

    fun cargar() { job?.cancel(); job = viewModelScope.launch { cargarAhora() } }

    private suspend fun cargarAhora() {
        if (estado !is Estado.Listo) estado = Estado.Cargando
        estado = repo.novedades(filtro.takeIf { it != "Todo" }, busqueda).aEstado()
    }
}

// P3 · Novedades
@Composable
fun NovedadesScreen(onNovedad: (Int) -> Unit, onNueva: () -> Unit, vm: NovedadesViewModel = viewModel()) {
    val sesion by CarenciaApp.instancia.sesion.sesion.collectAsState(initial = null)
    var buscando by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = EspacioBarra + 40.dp)
        ) {
            item {
                Column(Modifier.statusBarsPadding().padding(top = 40.dp)) {
                    Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                        TituloPantalla("Novedades", Modifier.weight(1f))
                        BotonCircular(R.drawable.ic_buscar, "Buscar", { buscando = !buscando; if (!buscando) vm.buscar("") })
                    }
                    AnimatedVisibility(buscando) {
                        CampoTexto(
                            vm.busqueda, vm::buscar, "Buscar anuncios…", icono = R.drawable.ic_buscar,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Segmentos(listOf("Todo") + Categorias.Novedad, vm.filtro, vm::cambiarFiltro)
                    Spacer(Modifier.height(22.dp))
                }
            }
            when (val e = vm.estado) {
                is Estado.Cargando -> item { Cargando() }
                is Estado.Error -> item { MensajeError(e.mensaje, vm::cargar) }
                is Estado.Listo -> {
                    val destacada = e.data.firstOrNull { it.destacada } ?: e.data.firstOrNull()
                    val anteriores = e.data.filter { it != destacada }
                    if (destacada == null) item { Vacio("No hay novedades que coincidan.") }
                    destacada?.let { d -> item(key = "d${d.pkNovedad}") { Destacada(d) { onNovedad(d.pkNovedad) } } }
                    if (anteriores.isNotEmpty()) {
                        item {
                            Text("Anteriores", style = T.elite(20), modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp))
                        }
                        itemsIndexed(anteriores, key = { _, n -> n.pkNovedad }) { i, n ->
                            Entrada(n, primera = i == 0, ultima = i == anteriores.lastIndex) { onNovedad(n.pkNovedad) }
                        }
                    }
                }
            }
        }

        // Sólo el rol Admin ve el botón para publicar
        if (sesion?.esAdmin == true) {
            BotonFlotante(
                R.drawable.ic_mas, "Publicar novedad", onNueva,
                Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 24.dp, bottom = 108.dp)
            )
        }
    }
}

@Composable
private fun Destacada(n: Novedad, onClick: () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(C.Superficie)
            .border(1.dp, C.Borde, RoundedCornerShape(22.dp))
            .toque(onClick)
    ) {
        ImagenRemota(n.imagenUrl, Modifier.fillMaxWidth().height(180.dp), descripcion = n.titulo, respaldo = R.drawable.inicio_portada)
        Column(
            Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Etiqueta(n.categoria, acento = true)
                Spacer(Modifier.width(10.dp))
                Text(Fechas.larga(n.fecha), style = T.elite(13, C.Gris))
            }
            Text(n.titulo, style = T.barrio(28))
            Text(n.resumen, style = T.Cuerpo)
            Text("Leer completo", style = T.elite(14))
        }
    }
}

// App 3 / Entrada: línea de tiempo, toda la tarjeta es tocable
@Composable
private fun Entrada(n: Novedad, primera: Boolean, ultima: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(IntrinsicSize.Min)
            .padding(bottom = if (ultima) 0.dp else 14.dp)
    ) {
        Column(
            Modifier.fillMaxHeight().padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (primera) C.RojoClaro else C.Gris))
            if (!ultima) {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(2.dp).weight(1f).background(C.Borde))
            }
        }
        Spacer(Modifier.width(14.dp))
        Row(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(C.Superficie)
                .border(1.dp, C.Borde, RoundedCornerShape(18.dp))
                .toque(onClick)
                .padding(12.dp)
        ) {
            ImagenRemota(n.imagenUrl, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)), respaldo = R.drawable.inicio_cap2)
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etiqueta(n.categoria)
                    Spacer(Modifier.width(8.dp))
                    Text(Fechas.corta(n.fecha), style = T.elite(13, C.Gris))
                }
                Text(n.titulo, style = T.elite(16), maxLines = 2)
            }
        }
    }
}
