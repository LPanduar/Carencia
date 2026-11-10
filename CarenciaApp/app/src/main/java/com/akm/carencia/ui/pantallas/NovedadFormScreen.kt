package com.akm.carencia.ui.pantallas

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.NovedadUpdateRequest
import com.akm.carencia.data.Repositorio
import com.akm.carencia.data.Resultado
import com.akm.carencia.notificaciones.NovedadesWorker
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.IconoTintado
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NovedadFormViewModel : ViewModel() {
    private val app = CarenciaApp.instancia
    var titulo by mutableStateOf("")
    var resumen by mutableStateOf("")
    var contenido by mutableStateOf("")
    var categoria by mutableStateOf("Anuncio")
    var destacada by mutableStateOf(false)
    var imagenActual by mutableStateOf<String?>(null); private set  // ruta en el servidor
    var imagenNueva by mutableStateOf<Uri?>(null)                    // elegida del teléfono
    var cargando by mutableStateOf(false); private set
    var guardando by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    private var cargado = false

    fun cargar(id: Int) {
        if (cargado || id == 0) return
        cargado = true
        cargando = true
        viewModelScope.launch {
            when (val r = app.repo.novedad(id)) {
                is Resultado.Ok -> r.data.let {
                    titulo = it.titulo; resumen = it.resumen; contenido = it.contenido
                    categoria = it.categoria; destacada = it.destacada; imagenActual = it.imagenUrl
                }
                is Resultado.Error -> error = r.mensaje
            }
            cargando = false
        }
    }

    fun guardar(context: Context, id: Int, listo: () -> Unit) {
        error = when {
            titulo.isBlank() -> "Escribe un título"
            resumen.isBlank() -> "Escribe un resumen corto"
            contenido.isBlank() -> "Escribe el contenido"
            else -> null
        }
        if (error != null) return
        guardando = true
        viewModelScope.launch {
            val archivo = imagenNueva?.let { withContext(Dispatchers.IO) { copiarACache(context, it) } }
            val r = if (id == 0) {
                // POST api/Novedad (multipart)
                app.repo.crearNovedad(titulo, resumen, contenido, categoria, destacada, archivo)
            } else {
                // PUT api/Novedad/{id}  (+ PATCH imagen si eligió otra)
                val put = app.repo.actualizarNovedad(id, NovedadUpdateRequest(titulo.trim(), resumen.trim(), contenido.trim(), categoria, destacada))
                if (put is Resultado.Ok && archivo != null) app.repo.cambiarImagenNovedad(id, archivo) else put
            }
            archivo?.delete()
            guardando = false
            when (r) {
                is Resultado.Ok -> {
                    // Los teléfonos revisan cada 15 min; aquí forzamos la revisión para verlo al momento
                    if (id == 0) NovedadesWorker.revisarAhora(context)
                    listo()
                }
                is Resultado.Error -> error = r.mensaje
            }
        }
    }

    private fun copiarACache(context: Context, uri: Uri): File {
        val tipo = context.contentResolver.getType(uri)
        val ext = if (tipo == "image/png") "png" else "jpg"
        val destino = File(context.cacheDir, "novedad_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(uri)!!.use { entrada ->
            destino.outputStream().use { entrada.copyTo(it) }
        }
        return destino
    }
}

// Formulario de administrador: publicar o editar una novedad
@Composable
fun NovedadFormScreen(id: Int, onListo: () -> Unit, vm: NovedadFormViewModel = viewModel(key = "form_$id")) {
    val context = LocalContext.current
    LaunchedEffect(id) { vm.cargar(id) }
    // Selector de fotos del sistema (no requiere permiso de almacenamiento)
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.imagenNueva = uri
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(0.dp))
        Volver(onListo)
        Text(if (id == 0) "Nueva novedad" else "Editar novedad", style = T.barrio(36))
        Text(
            if (id == 0) "Se mostrará a todos los jugadores y recibirán una notificación."
            else "Los cambios se verán al instante en la app.",
            style = T.elite(14, C.Gris, 1.5f)
        )
        if (vm.cargando) { Cargando(); return@Column }

        // Imagen
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(C.Superficie)
                .border(1.dp, C.Borde, RoundedCornerShape(18.dp))
                .clickable {
                    selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center
        ) {
            val modelo: Any? = vm.imagenNueva ?: Repositorio.urlImagen(vm.imagenActual)
            if (modelo != null) {
                AsyncImage(modelo, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconoTintado(R.drawable.ic_mas, C.Gris, Modifier.size(28.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Agregar imagen (opcional)", style = T.elite(14, C.Gris))
                }
            }
        }

        CampoTexto(vm.titulo, { vm.titulo = it.take(150) }, "Título")
        CampoTexto(vm.resumen, { vm.resumen = it.take(300) }, "Resumen (se ve en la lista)", lineas = 2)
        CampoTexto(vm.contenido, { vm.contenido = it }, "Contenido completo", lineas = 5)

        Text("Categoría", style = T.elite(16))
        Segmentos(Categorias.Novedad, vm.categoria, { vm.categoria = it }, padding = 0.dp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Destacada", style = T.elite(16))
                Text("Aparece grande arriba de la lista", style = T.elite(13, C.Gris))
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = vm.destacada, onCheckedChange = { vm.destacada = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = C.Fondo, checkedTrackColor = C.Crema,
                    uncheckedThumbColor = C.Gris, uncheckedTrackColor = C.Chip, uncheckedBorderColor = C.Borde
                )
            )
        }

        vm.error?.let { Text(it, style = T.elite(14, C.RojoClaro)) }

        Boton(
            if (id == 0) "Publicar" else "Guardar cambios",
            Modifier.fillMaxWidth().padding(top = 6.dp),
            cargando = vm.guardando
        ) { vm.guardar(context, id, onListo) }
        Spacer(Modifier.height(32.dp))
    }
}
