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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.Inicio
import com.akm.carencia.data.Personaje
import com.akm.carencia.data.PersonajeUpdateRequest
import com.akm.carencia.data.Repositorio
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.IconoTintado
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.componentes.Vacio
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ============================ Editar Inicio (Admin) ============================
class AdminInicioViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var estado by mutableStateOf<Estado<Inicio>>(Estado.Cargando); private set
    var descripcion by mutableStateOf("")
    var guardandoDescripcion by mutableStateOf(false); private set
    var aviso by mutableStateOf<String?>(null)

    fun cargar() = viewModelScope.launch {
        val r = repo.inicio()
        if (r is Resultado.Ok) descripcion = r.data.descripcion
        estado = r.aEstado()
    }

    // Ejecuta una operación de la API, avisa el resultado y recarga la pantalla
    private fun accion(bloque: suspend () -> Resultado<*>) = viewModelScope.launch {
        when (val r = bloque()) {
            is Resultado.Ok -> { aviso = r.mensaje ?: "Listo"; cargar() }
            is Resultado.Error -> aviso = r.mensaje
        }
    }

    fun guardarDescripcion() {
        if (descripcion.isBlank()) { aviso = "Escribe la descripción"; return }
        guardandoDescripcion = true
        viewModelScope.launch {
            when (val r = repo.actualizarDescripcion(descripcion)) {
                is Resultado.Ok -> aviso = r.mensaje ?: "Guardado"
                is Resultado.Error -> aviso = r.mensaje
            }
            guardandoDescripcion = false
        }
    }

    fun agregarEtiqueta(tipo: String, texto: String) = accion { repo.crearEtiqueta(tipo, texto) }
    fun editarEtiqueta(id: Int, texto: String) = accion { repo.actualizarEtiqueta(id, texto) }
    fun borrarEtiqueta(id: Int) = accion { repo.eliminarEtiqueta(id) }

    fun agregarAcerca(seccion: String, texto: String) = accion { repo.crearAcerca(seccion, texto) }
    fun editarAcerca(id: Int, texto: String) = accion { repo.actualizarAcerca(id, texto) }
    fun borrarAcerca(id: Int) = accion { repo.eliminarAcerca(id) }

    fun borrarPersonaje(id: Int) = accion { repo.eliminarPersonaje(id) }
}

@Composable
fun AdminInicioScreen(
    onVolver: () -> Unit,
    onPersonaje: (Int) -> Unit,   // 0 = nuevo
    vm: AdminInicioViewModel = viewModel()
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    var seccion by rememberSaveable { mutableStateOf(Categorias.Acerca.first()) }
    var personajePorBorrar by remember { mutableStateOf<Int?>(null) }

    Box(Modifier.fillMaxSize()) {
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
            Volver(onVolver)
            Text("Editar inicio", style = T.barrio(36))
            Text("Lo que cambies aquí se ve al instante en la pantalla de Inicio de todos.", style = T.elite(14, C.Gris, 1.5f))

            when (val e = vm.estado) {
                is Estado.Cargando -> Cargando()
                is Estado.Error -> MensajeError(e.mensaje, { vm.cargar() })
                is Estado.Listo -> {
                    val inicio = e.data

                    // ---------- Descripción ----------
                    Titulo("Descripción")
                    CampoTexto(vm.descripcion, { vm.descripcion = it.take(1000) }, "Descripción del juego", lineas = 4)
                    Boton("Guardar descripción", Modifier.fillMaxWidth(), cargando = vm.guardandoDescripcion) {
                        vm.guardarDescripcion()
                    }

                    // ---------- Etiquetas ----------
                    Titulo("Etiquetas", "PC, duración, versión…")
                    ListaEditable(
                        inicio.info.map { it.pkInicioEtiqueta to it.texto }, "Nueva etiqueta", 40,
                        onAgregar = { vm.agregarEtiqueta("Info", it) },
                        onGuardar = vm::editarEtiqueta, onBorrar = vm::borrarEtiqueta
                    )

                    Titulo("Géneros", "Etiquetas rojas: Terror, Point & click…")
                    ListaEditable(
                        inicio.generos.map { it.pkInicioEtiqueta to it.texto }, "Nuevo género", 40,
                        onAgregar = { vm.agregarEtiqueta("Genero", it) },
                        onGuardar = vm::editarEtiqueta, onBorrar = vm::borrarEtiqueta
                    )

                    // ---------- Personajes ----------
                    Titulo("Personajes", "El primero se muestra grande")
                    if (inicio.personajes.isEmpty()) Vacio("Todavía no hay personajes.")
                    inicio.personajes.forEach { p ->
                        FilaPersonaje(
                            p,
                            confirmando = personajePorBorrar == p.pkPersonaje,
                            onEditar = { onPersonaje(p.pkPersonaje) },
                            onBorrar = { personajePorBorrar = p.pkPersonaje },
                            onConfirmar = { personajePorBorrar = null; vm.borrarPersonaje(p.pkPersonaje) },
                            onCancelar = { personajePorBorrar = null }
                        )
                    }
                    Boton("+ Personaje", Modifier.fillMaxWidth(), secundario = true) { onPersonaje(0) }

                    // ---------- Acerca de ----------
                    Titulo("Acerca de")
                    Segmentos(Categorias.Acerca, seccion, { seccion = it }, padding = 0.dp)
                    ListaEditable(
                        inicio.seccion(seccion).map { it.pkAcercaItem to it.texto }, "Nuevo renglón en $seccion", 150,
                        onAgregar = { vm.agregarAcerca(seccion, it) },
                        onGuardar = vm::editarAcerca, onBorrar = vm::borrarAcerca
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
        vm.aviso?.let { Aviso(it) { vm.aviso = null } }
    }
}

@Composable
private fun Titulo(texto: String, ayuda: String? = null) {
    Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(texto, style = T.elite(20))
        if (ayuda != null) Text(ayuda, style = T.elite(13, C.Gris))
    }
}

// Lista de textos con agregar / editar / borrar en la misma tarjeta
@Composable
private fun ListaEditable(
    items: List<Pair<Int, String>>,
    placeholder: String,
    maximo: Int,
    onAgregar: (String) -> Unit,
    onGuardar: (Int, String) -> Unit,
    onBorrar: (Int) -> Unit
) {
    var editando by remember { mutableStateOf<Int?>(null) }
    var textoEdicion by remember { mutableStateOf("") }
    var porBorrar by remember { mutableStateOf<Int?>(null) }
    var nuevo by remember { mutableStateOf("") }

    Tarjeta(radio = 18.dp, padding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.isEmpty()) Text("Vacío", style = T.elite(14, C.Gris))
            items.forEach { (id, texto) ->
                when (id) {
                    editando -> {
                        CampoTexto(textoEdicion, { textoEdicion = it.take(maximo) }, placeholder)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Boton("Cancelar", Modifier.weight(1f), secundario = true, compacto = true) { editando = null }
                            Boton("Guardar", Modifier.weight(1f), compacto = true, habilitado = textoEdicion.isNotBlank()) {
                                onGuardar(id, textoEdicion); editando = null
                            }
                        }
                    }
                    porBorrar -> {
                        Text("¿Borrar \"$texto\"?", style = T.elite(14, C.RojoClaro))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Boton("Cancelar", Modifier.weight(1f), secundario = true, compacto = true) { porBorrar = null }
                            Boton("Borrar", Modifier.weight(1f), compacto = true) { onBorrar(id); porBorrar = null }
                        }
                    }
                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(texto, style = T.elite(15), modifier = Modifier.weight(1f))
                        Text("Editar", style = T.elite(13, C.Gris), modifier = Modifier
                            .clickable { editando = id; textoEdicion = texto; porBorrar = null }
                            .padding(8.dp))
                        Text("Borrar", style = T.elite(13, C.RojoClaro), modifier = Modifier
                            .clickable { porBorrar = id; editando = null }
                            .padding(8.dp))
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(C.Borde))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto(nuevo, { nuevo = it.take(maximo) }, placeholder, Modifier.weight(1f))
                Boton("Agregar", compacto = true, habilitado = nuevo.isNotBlank()) { onAgregar(nuevo); nuevo = "" }
            }
        }
    }
}

@Composable
private fun FilaPersonaje(
    p: Personaje,
    confirmando: Boolean,
    onEditar: () -> Unit,
    onBorrar: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Tarjeta(radio = 18.dp, padding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ImagenRemota(p.imagenUrl, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), respaldo = R.drawable.acerca_cartel)
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.nombre, style = T.elite(17))
                    Etiqueta(p.rol, acento = true)
                }
            }
            if (confirmando) {
                Text("¿Borrar a ${p.nombre}?", style = T.elite(14, C.RojoClaro))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Boton("Cancelar", Modifier.weight(1f), secundario = true, onClick = onCancelar)
                    Boton("Borrar", Modifier.weight(1f), onClick = onConfirmar)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Boton("Editar", Modifier.weight(1f), secundario = true, compacto = true, onClick = onEditar)
                    Boton("Borrar", Modifier.weight(1f), secundario = true, compacto = true, onClick = onBorrar)
                }
            }
        }
    }
}

// ============================ Formulario de personaje ============================
class PersonajeFormViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var nombre by mutableStateOf("")
    var rol by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var rasgos by mutableStateOf("")
    var imagenActual by mutableStateOf<String?>(null); private set
    var imagenNueva by mutableStateOf<Uri?>(null)
    var cargando by mutableStateOf(false); private set
    var guardando by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    private var cargado = false

    fun cargar(id: Int) {
        if (cargado || id == 0) return
        cargado = true
        cargando = true
        viewModelScope.launch {
            when (val r = repo.inicio()) {
                is Resultado.Ok -> r.data.personajes.firstOrNull { it.pkPersonaje == id }?.let {
                    nombre = it.nombre; rol = it.rol; descripcion = it.descripcion
                    rasgos = it.rasgos.orEmpty(); imagenActual = it.imagenUrl
                } ?: run { error = "No se encontró el personaje" }
                is Resultado.Error -> error = r.mensaje
            }
            cargando = false
        }
    }

    fun guardar(context: Context, id: Int, listo: () -> Unit) {
        error = when {
            nombre.isBlank() -> "Escribe el nombre"
            rol.isBlank() -> "Escribe el rol (ej. Protagonista)"
            descripcion.isBlank() -> "Escribe una descripción"
            else -> null
        }
        if (error != null) return
        guardando = true
        viewModelScope.launch {
            val archivo = imagenNueva?.let { withContext(Dispatchers.IO) { copiarACache(context, it) } }
            val r = if (id == 0) {
                repo.crearPersonaje(nombre, rol, descripcion, rasgos, archivo)
            } else {
                val put = repo.actualizarPersonaje(
                    id, PersonajeUpdateRequest(nombre.trim(), rol.trim(), descripcion.trim(), rasgos.trim().ifBlank { null })
                )
                if (put is Resultado.Ok && archivo != null) repo.cambiarImagenPersonaje(id, archivo) else put
            }
            archivo?.delete()
            guardando = false
            when (r) {
                is Resultado.Ok -> listo()
                is Resultado.Error -> error = r.mensaje
            }
        }
    }

    private fun copiarACache(context: Context, uri: Uri): File {
        val ext = if (context.contentResolver.getType(uri) == "image/png") "png" else "jpg"
        val destino = File(context.cacheDir, "personaje_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(uri)!!.use { entrada ->
            destino.outputStream().use { entrada.copyTo(it) }
        }
        return destino
    }
}

@Composable
fun PersonajeFormScreen(id: Int, onListo: () -> Unit, vm: PersonajeFormViewModel = viewModel(key = "personaje_$id")) {
    val context = LocalContext.current
    LaunchedEffect(id) { vm.cargar(id) }
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
        Text(if (id == 0) "Nuevo personaje" else "Editar personaje", style = T.barrio(36))
        if (vm.cargando) { Cargando(); return@Column }

        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(C.Superficie)
                .border(1.dp, C.Borde, RoundedCornerShape(18.dp))
                .clickable { selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
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

        CampoTexto(vm.nombre, { vm.nombre = it.take(60) }, "Nombre")
        CampoTexto(vm.rol, { vm.rol = it.take(40) }, "Rol (Protagonista, Desaparecida…)")
        CampoTexto(vm.descripcion, { vm.descripcion = it.take(500) }, "Descripción", lineas = 4)
        CampoTexto(vm.rasgos, { vm.rasgos = it.take(200) }, "Rasgos separados por coma (opcional)")

        vm.error?.let { Text(it, style = T.elite(14, C.RojoClaro)) }

        Boton(
            if (id == 0) "Agregar personaje" else "Guardar cambios",
            Modifier.fillMaxWidth().padding(top = 6.dp),
            cargando = vm.guardando
        ) { vm.guardar(context, id, onListo) }
        Spacer(Modifier.height(32.dp))
    }
}
