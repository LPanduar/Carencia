package com.akm.carencia.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akm.carencia.CarenciaApp
import com.akm.carencia.data.ConteoCategoria
import com.akm.carencia.data.Estadistica
import com.akm.carencia.notificaciones.Notificaciones
import com.akm.carencia.notificaciones.NovedadesWorker
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

class PerfilViewModel : ViewModel() {
    var stats by mutableStateOf<Estado<Estadistica>>(Estado.Cargando); private set
    var misFotos by mutableStateOf(0); private set
    fun cargar() = viewModelScope.launch {
        misFotos = CarenciaApp.instancia.fotos.listar().size
        stats = CarenciaApp.instancia.repo.estadisticas().aEstado()
    }
}

// Ajustes: los jugadores no necesitan cuenta. Aquí están las estadísticas,
// las notificaciones y el acceso de administrador.
@Composable
fun PerfilScreen(
    onVolver: () -> Unit,
    onAdmin: () -> Unit,
    onPanel: () -> Unit,
    vm: PerfilViewModel = viewModel()
) {
    val app = CarenciaApp.instancia
    val sesion by app.sesion.sesion.collectAsState(initial = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var aviso by remember { mutableStateOf<String?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(0.dp))
            Volver(onVolver)
            Text("Ajustes", style = T.barrio(40))

            // ---------- Estadísticas (GET api/Estadistica) ----------
            Text("El juego en números", style = T.elite(20), modifier = Modifier.padding(top = 4.dp))
            when (val e = vm.stats) {
                is Estado.Cargando -> Cargando()
                is Estado.Error -> MensajeError(e.mensaje, { vm.cargar() })
                is Estado.Listo -> Estadisticas(e.data, vm.misFotos)
            }

            // ---------- Notificaciones ----------
            Tarjeta {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notificaciones", style = T.elite(18))
                    Text(
                        if (Notificaciones.tienePermiso(context))
                            "Te avisaremos cuando el equipo publique una novedad (revisión cada 15 minutos)."
                        else "Las notificaciones están desactivadas en los ajustes del teléfono.",
                        style = T.elite(14, C.Texto, 1.5f)
                    )
                    Boton("Buscar novedades ahora", Modifier.fillMaxWidth(), secundario = true) {
                        NovedadesWorker.revisarAhora(context)
                        aviso = "Revisando novedades…"
                    }
                }
            }

            // ---------- Administración ----------
            Tarjeta {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Administración", style = T.elite(18), modifier = Modifier.weight(1f))
                        if (sesion?.esAdmin == true) Etiqueta(sesion!!.nombre, acento = true)
                    }
                    if (sesion?.esAdmin == true) {
                        Text(
                            "Publica novedades y administra la galería oficial.",
                            style = T.elite(14, C.Texto, 1.5f)
                        )
                        Boton("Abrir panel de administración", Modifier.fillMaxWidth(), onClick = onPanel)
                        Boton("Cerrar sesión de administrador", Modifier.fillMaxWidth(), secundario = true) {
                            scope.launch { app.repo.cerrarSesion(); aviso = "Sesión cerrada" }
                        }
                    } else {
                        Text(
                            "Sólo para el equipo de Carencia.",
                            style = T.elite(14, C.Texto, 1.5f)
                        )
                        Boton("Acceso de administrador", Modifier.fillMaxWidth(), secundario = true, onClick = onAdmin)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
        aviso?.let { Aviso(it) { aviso = null } }
    }
}

@Composable
private fun Estadisticas(s: Estadistica, misFotos: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Dato("Niveles", s.totalNiveles, Modifier.weight(1f))
        Dato("Fotos", s.totalCapturas, Modifier.weight(1f))
        Dato("Novedades", s.totalNovedades, Modifier.weight(1f))
        Dato("Mis fotos", misFotos, Modifier.weight(1f))
    }
    Barras("Galería por categoría", s.capturasPorCategoria.orEmpty())
    Barras("Novedades por categoría", s.novedadesPorCategoria.orEmpty())
}

@Composable
private fun Dato(titulo: String, valor: Int, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(C.Superficie).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(valor.toString(), style = T.barrio(30))
        Text(titulo, style = T.elite(11, C.Gris), maxLines = 1)
    }
}

@Composable
private fun Barras(titulo: String, datos: List<ConteoCategoria>) {
    if (datos.isEmpty()) return
    val max = datos.maxOf { it.total }.coerceAtLeast(1)
    Tarjeta {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = T.elite(16))
            datos.forEach { d ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.categoria, style = T.elite(13, C.Texto), modifier = Modifier.width(96.dp), maxLines = 1)
                    Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(C.Chip)) {
                        Box(
                            Modifier.fillMaxWidth(d.total / max.toFloat()).height(10.dp)
                                .clip(RoundedCornerShape(5.dp)).background(C.Rojo)
                        )
                    }
                    Text(d.total.toString(), style = T.elite(13, C.Gris), modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
