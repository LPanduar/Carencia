package com.akm.carencia.ui.pantallas

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Captura
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.FotoLocal
import com.akm.carencia.data.GaleriaTelefono
import com.akm.carencia.data.Repositorio
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.Fechas
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.BotonFlotante
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.EspacioBarra
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.TituloPantalla
import com.akm.carencia.ui.componentes.Vacio
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

// Un elemento de la galería: foto oficial (API) o foto del espejo (guardada en el teléfono)
sealed interface ItemGaleria {
    val clave: String
    val titulo: String
    data class Oficial(val c: Captura) : ItemGaleria {
        override val clave get() = "o${c.pkCaptura}"
        override val titulo get() = c.titulo
    }
    data class Mia(val f: FotoLocal) : ItemGaleria {
        override val clave get() = "m${f.id}"
        override val titulo get() = f.titulo
    }
}

private const val MIS_FOTOS = "Mis fotos"

class GaleriaViewModel : ViewModel() {
    private val app = CarenciaApp.instancia

    var estado by mutableStateOf<Estado<List<ItemGaleria>>>(Estado.Cargando); private set
    var filtro by mutableStateOf("Todo"); private set
    var busqueda by mutableStateOf(""); private set
    var aviso by mutableStateOf<String?>(null)
    private var job: Job? = null

    fun cambiarFiltro(f: String) { filtro = f; cargar() }

    fun buscar(texto: String) {
        busqueda = texto
        job?.cancel()
        job = viewModelScope.launch { delay(350); cargarAhora() } // espera a que deje de escribir
    }

    fun cargar() { job?.cancel(); job = viewModelScope.launch { cargarAhora() } }

    private suspend fun cargarAhora() {
        if (estado !is Estado.Listo) estado = Estado.Cargando
        // Mis fotos: almacenamiento local, se filtran aquí por título
        val mias = app.fotos.listar()
            .filter { busqueda.isBlank() || it.titulo.contains(busqueda.trim(), ignoreCase = true) }
            .map { ItemGaleria.Mia(it) }
        if (filtro == MIS_FOTOS) { estado = Estado.Listo(mias); return }

        // Oficiales: GET api/Captura con filtro y búsqueda
        estado = when (val r = app.repo.capturas(filtro.takeIf { it != "Todo" }, busqueda)) {
            is Resultado.Ok -> {
                val oficiales = r.data.map { ItemGaleria.Oficial(it) }
                Estado.Listo(if (filtro == "Todo") mias + oficiales else oficiales)
            }
            is Resultado.Error -> if (filtro == "Todo" && mias.isNotEmpty()) Estado.Listo(mias) else Estado.Error(r.mensaje)
        }
    }

    // ---------- Fotos oficiales ----------
    fun descargar(c: Captura) = viewModelScope.launch {
        val bytes = app.repo.descargarImagen(c.imagenUrl)
        aviso = if (bytes != null && GaleriaTelefono.guardar(app, bytes, c.titulo))
            "Guardada en Imágenes/Carencia" else "No se pudo descargar la foto"
    }

    // PUT api/Captura/{id} (Admin)
    fun editarOficial(c: Captura, titulo: String, categoria: String, listo: () -> Unit) = viewModelScope.launch {
        when (val r = app.repo.actualizarCaptura(c.pkCaptura, titulo, categoria)) {
            is Resultado.Ok -> { aviso = "Foto actualizada"; listo(); cargarAhora() }
            is Resultado.Error -> aviso = r.mensaje
        }
    }

    // DELETE api/Captura/{id} (Admin)
    fun eliminarOficial(c: Captura, listo: () -> Unit) = viewModelScope.launch {
        when (val r = app.repo.eliminarCaptura(c.pkCaptura)) {
            is Resultado.Ok -> { aviso = "Foto eliminada de la galería"; listo(); cargarAhora() }
            is Resultado.Error -> aviso = r.mensaje
        }
    }

    // ---------- Mis fotos (almacenamiento local) ----------
    fun exportar(f: FotoLocal) = viewModelScope.launch {
        val bytes = app.fotos.archivoDe(f).readBytes()
        aviso = if (GaleriaTelefono.guardar(app, bytes, f.titulo)) "Guardada en Imágenes/Carencia" else "No se pudo guardar"
    }

    fun renombrar(f: FotoLocal, titulo: String, listo: () -> Unit) = viewModelScope.launch {
        app.fotos.renombrar(f.id, titulo); aviso = "Foto renombrada"; listo(); cargarAhora()
    }

    fun eliminarMia(f: FotoLocal, listo: () -> Unit) = viewModelScope.launch {
        app.fotos.eliminar(f.id); aviso = "Foto eliminada"; listo(); cargarAhora()
    }
}

// P3 · Galería
@Composable
fun GaleriaScreen(onCamara: () -> Unit, vm: GaleriaViewModel = viewModel()) {
    val context = LocalContext.current
    val sesion by CarenciaApp.instancia.sesion.sesion.collectAsState(initial = null)
    val esAdmin = sesion?.esAdmin == true
    var buscando by rememberSaveable { mutableStateOf(false) }
    var abierta by remember { mutableStateOf<ItemGaleria?>(null) }
    var pendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Android 7-9: pedir permiso de almacenamiento antes de guardar en la galería del teléfono
    val permisoAlmacenamiento = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) pendiente?.invoke() else vm.aviso = "Sin permiso para guardar en el teléfono"
        pendiente = null
    }
    fun guardarEnTelefono(accion: () -> Unit) {
        if (GaleriaTelefono.necesitaPermiso(context)) {
            pendiente = accion
            permisoAlmacenamiento.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else accion()
    }

    // Al volver de la cámara se recarga para mostrar la foto nueva
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = EspacioBarra + 60.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(Modifier.statusBarsPadding().padding(top = 40.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TituloPantalla("Galería", Modifier.weight(1f))
                        BotonCircular(R.drawable.ic_buscar, "Buscar", { buscando = !buscando; if (!buscando) vm.buscar("") })
                    }
                    AnimatedVisibility(buscando) {
                        CampoTexto(
                            vm.busqueda, vm::buscar, "Buscar por título…",
                            icono = R.drawable.ic_buscar, modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Segmentos(listOf("Todo") + Categorias.Captura + MIS_FOTOS, vm.filtro, vm::cambiarFiltro, padding = 0.dp)
                    Spacer(Modifier.height(10.dp))
                }
            }

            when (val e = vm.estado) {
                is Estado.Cargando -> item(span = StaggeredGridItemSpan.FullLine) { Cargando() }
                is Estado.Error -> item(span = StaggeredGridItemSpan.FullLine) { MensajeError(e.mensaje, vm::cargar) }
                is Estado.Listo -> {
                    if (e.data.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                        Vacio(
                            if (vm.filtro == MIS_FOTOS) "Aún no tienes fotos. Toca la cámara y mírate en el espejo."
                            else "No hay fotos que coincidan."
                        )
                    }
                    items(e.data, key = { it.clave }) { item -> Foto(item) { abierta = item } }
                }
            }
        }

        // La cámara es para todos: las fotos se guardan en el teléfono
        BotonFlotante(
            R.drawable.ic_camara, "Tomar foto con el espejo", onCamara,
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 24.dp, bottom = 108.dp)
        )

        vm.aviso?.let { Aviso(it) { vm.aviso = null } }
    }

    when (val item = abierta) {
        is ItemGaleria.Oficial -> DetalleOficial(
            c = item.c, esAdmin = esAdmin,
            onCerrar = { abierta = null },
            onDescargar = { guardarEnTelefono { vm.descargar(item.c) } },
            onGuardar = { t, cat -> vm.editarOficial(item.c, t, cat) { abierta = null } },
            onEliminar = { vm.eliminarOficial(item.c) { abierta = null } }
        )
        is ItemGaleria.Mia -> DetalleMia(
            f = item.f,
            onCerrar = { abierta = null },
            onExportar = { guardarEnTelefono { vm.exportar(item.f) } },
            onRenombrar = { t -> vm.renombrar(item.f, t) { abierta = null } },
            onEliminar = { vm.eliminarMia(item.f) { abierta = null } }
        )
        null -> Unit
    }
}

private fun modeloDe(item: ItemGaleria): Any? = when (item) {
    is ItemGaleria.Oficial -> Repositorio.urlImagen(item.c.imagenUrl)
    is ItemGaleria.Mia -> CarenciaApp.instancia.fotos.archivoDe(item.f)
}

// App 3 / Foto: el alto sale de la proporción real de la imagen (los recortes oficiales
// tienen exactamente las medidas del Figma: 163×220, 163×150, 163×110)
@Composable
private fun Foto(item: ItemGaleria, onClick: () -> Unit) {
    var proporcion by remember(item.clave) { mutableStateOf(0.74f) }
    Column(Modifier.clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            AsyncImage(
                model = modeloDe(item),
                contentDescription = item.titulo,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(C.Superficie),
                error = ColorPainter(C.Superficie),
                onSuccess = { s ->
                    val tam = s.painter.intrinsicSize
                    if (tam.width > 0 && tam.height > 0) proporcion = (tam.width / tam.height).coerceIn(0.6f, 1.6f)
                },
                modifier = Modifier.fillMaxWidth().aspectRatio(proporcion).clip(RoundedCornerShape(14.dp))
            )
            if (item is ItemGaleria.Mia) {
                Etiqueta("Mía", modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            }
        }
        Text(item.titulo, style = T.elite(12, C.Gris), maxLines = 1)
    }
}

@Composable
private fun MarcoDetalle(onCerrar: () -> Unit, contenido: @Composable () -> Unit) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(C.Superficie)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) { contenido() }
    }
}

@Composable
private fun ImagenDetalle(modelo: Any?, titulo: String) {
    AsyncImage(
        model = modelo, contentDescription = titulo, contentScale = ContentScale.Crop,
        placeholder = ColorPainter(C.Fondo), error = ColorPainter(C.Fondo),
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp).height(320.dp).clip(RoundedCornerShape(16.dp))
    )
}

// Foto oficial: cualquiera la descarga; sólo el admin la edita o elimina
@Composable
private fun DetalleOficial(
    c: Captura,
    esAdmin: Boolean,
    onCerrar: () -> Unit,
    onDescargar: () -> Unit,
    onGuardar: (String, String) -> Unit,
    onEliminar: () -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var confirmar by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf(c.titulo) }
    var categoria by remember { mutableStateOf(c.categoria) }

    MarcoDetalle(onCerrar) {
        ImagenDetalle(Repositorio.urlImagen(c.imagenUrl), c.titulo)
        if (editando) {
            CampoTexto(titulo, { titulo = it.take(100) }, "Título")
            Segmentos(Categorias.Captura, categoria, { categoria = it }, padding = 0.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Boton("Cancelar", Modifier.weight(1f), secundario = true) { editando = false }
                Boton("Guardar", Modifier.weight(1f), habilitado = titulo.isNotBlank()) { onGuardar(titulo, categoria) }
            }
            return@MarcoDetalle
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.titulo, style = T.elite(22), modifier = Modifier.weight(1f))
            Etiqueta(c.categoria)
        }
        Text("Arte oficial de Carencia · ${Fechas.larga(c.fecha)}", style = T.elite(13, C.Gris))
        Boton("Descargar al teléfono", Modifier.fillMaxWidth(), onClick = onDescargar)
        if (esAdmin) {
            if (confirmar) {
                Text("¿Eliminar esta foto de la galería para todos?", style = T.elite(14, C.RojoClaro))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("No", Modifier.weight(1f), secundario = true) { confirmar = false }
                    Boton("Sí, eliminar", Modifier.weight(1f), onClick = onEliminar)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("Editar", Modifier.weight(1f), secundario = true) { editando = true }
                    Boton("Eliminar", Modifier.weight(1f), secundario = true) { confirmar = true }
                }
            }
        }
    }
}

// Foto del espejo: vive en el teléfono; el jugador la renombra, la exporta o la borra
@Composable
private fun DetalleMia(
    f: FotoLocal,
    onCerrar: () -> Unit,
    onExportar: () -> Unit,
    onRenombrar: (String) -> Unit,
    onEliminar: () -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var confirmar by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf(f.titulo) }
    val fecha = remember(f.fecha) { SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale("es", "MX")).format(Date(f.fecha)).replace(".", "") }

    MarcoDetalle(onCerrar) {
        ImagenDetalle(CarenciaApp.instancia.fotos.archivoDe(f), f.titulo)
        when {
            editando -> {
                CampoTexto(titulo, { titulo = it.take(100) }, "Título")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("Cancelar", Modifier.weight(1f), secundario = true) { editando = false }
                    Boton("Guardar", Modifier.weight(1f), habilitado = titulo.isNotBlank()) { onRenombrar(titulo) }
                }
            }
            confirmar -> {
                Text("¿Eliminar esta foto de tu teléfono?", style = T.elite(14, C.RojoClaro))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("No", Modifier.weight(1f), secundario = true) { confirmar = false }
                    Boton("Sí, eliminar", Modifier.weight(1f), onClick = onEliminar)
                }
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.titulo, style = T.elite(22), modifier = Modifier.weight(1f))
                    Etiqueta("Espejo", acento = true)
                }
                Text("Tomada con el espejo · $fecha", style = T.elite(13, C.Gris))
                Boton("Guardar en la galería del teléfono", Modifier.fillMaxWidth(), onClick = onExportar)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("Renombrar", Modifier.weight(1f), secundario = true) { editando = true }
                    Boton("Eliminar", Modifier.weight(1f), secundario = true) { confirmar = true }
                }
            }
        }
    }
}

// Mensaje breve flotante (reemplaza al Snackbar con el estilo del juego)
@Composable
fun BoxScope.Aviso(texto: String, onFin: () -> Unit) {
    LaunchedEffect(texto) { delay(2500); onFin() }
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(C.Activo)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) { Text(texto, style = T.elite(14)) }
}
