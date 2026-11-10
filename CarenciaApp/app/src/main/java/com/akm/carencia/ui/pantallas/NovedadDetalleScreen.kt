package com.akm.carencia.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Novedad
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.Fechas
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Portada
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

class NovedadDetalleViewModel : ViewModel() {
    private val app = CarenciaApp.instancia
    var estado by mutableStateOf<Estado<Novedad>>(Estado.Cargando); private set
    var aviso by mutableStateOf<String?>(null)
    var ocupado by mutableStateOf(false); private set

    fun cargar(id: Int) = viewModelScope.launch {
        if (estado !is Estado.Listo) estado = Estado.Cargando
        estado = app.repo.novedad(id).aEstado()
        app.sesion.marcarNovedadVista(id) // ya la vio: no hace falta notificarla
    }

    // PATCH api/Novedad/{id}/destacar
    fun alternarDestacada(n: Novedad) = viewModelScope.launch {
        ocupado = true
        when (val r = app.repo.destacarNovedad(n.pkNovedad, !n.destacada)) {
            is Resultado.Ok -> { estado = Estado.Listo(r.data); aviso = if (r.data.destacada) "Ahora es la destacada" else "Ya no está destacada" }
            is Resultado.Error -> aviso = r.mensaje
        }
        ocupado = false
    }

    // DELETE api/Novedad/{id}
    fun eliminar(n: Novedad, listo: () -> Unit) = viewModelScope.launch {
        ocupado = true
        when (val r = app.repo.eliminarNovedad(n.pkNovedad)) {
            is Resultado.Ok -> listo()
            is Resultado.Error -> { aviso = r.mensaje; ocupado = false }
        }
    }
}

@Composable
fun NovedadDetalleScreen(
    id: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    vm: NovedadDetalleViewModel = viewModel(key = "novedad_$id")
) {
    val sesion by CarenciaApp.instancia.sesion.sesion.collectAsState(initial = null)
    var confirmar by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar(id) } // recarga al volver de editar

    Box(Modifier.fillMaxSize()) {
        when (val e = vm.estado) {
            is Estado.Cargando -> Cargando(Modifier.padding(top = 200.dp))
            is Estado.Error -> MensajeError(e.mensaje, { vm.cargar(id) }, Modifier.padding(top = 160.dp))
            is Estado.Listo -> {
                val n = e.data
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Portada(320.dp) { ImagenRemota(n.imagenUrl, Modifier.fillMaxSize(), descripcion = n.titulo, respaldo = R.drawable.inicio_portada) }
                    Column(
                        Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Etiqueta(n.categoria, acento = true)
                            Spacer(Modifier.width(10.dp))
                            Text(Fechas.larga(n.fecha), style = T.elite(13, C.Gris))
                            if (n.destacada) { Spacer(Modifier.width(10.dp)); Etiqueta("Destacada") }
                        }
                        Text(n.titulo, style = T.barrio(34))
                        Text(n.resumen, style = T.elite(16, C.Crema, 1.5f))
                        Text(n.contenido, style = T.Cuerpo)
                        Text("Publicado por ${n.autorNombre ?: "el equipo de Carencia"}", style = T.elite(13, C.Gris))

                        // Panel de administración: sólo rol Admin
                        if (sesion?.esAdmin == true) {
                            Tarjeta {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Administrar", style = T.elite(18))
                                    if (confirmar) {
                                        Text("¿Eliminar esta novedad para todos los usuarios?", style = T.elite(14, C.RojoClaro))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Boton("No", Modifier.weight(1f), secundario = true) { confirmar = false }
                                            Boton("Eliminar", Modifier.weight(1f), cargando = vm.ocupado) { vm.eliminar(n, onVolver) }
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Boton("Editar", Modifier.weight(1f)) { onEditar(n.pkNovedad) }
                                            Boton(
                                                if (n.destacada) "Quitar destacado" else "Destacar",
                                                Modifier.weight(1f), secundario = true, cargando = vm.ocupado
                                            ) { vm.alternarDestacada(n) }
                                        }
                                        Boton("Eliminar", Modifier.fillMaxWidth(), secundario = true) { confirmar = true }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(EspacioBarra))
                    }
                }
            }
        }
        Volver(onVolver, Modifier.statusBarsPadding().padding(start = 24.dp, top = 8.dp))
        vm.aviso?.let { Aviso(it) { vm.aviso = null } }
    }
}
