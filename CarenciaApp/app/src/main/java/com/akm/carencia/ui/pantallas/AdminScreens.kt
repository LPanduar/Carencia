package com.akm.carencia.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.akm.carencia.data.Estadistica
import com.akm.carencia.data.Novedad
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.Estado
import com.akm.carencia.ui.Fechas
import com.akm.carencia.ui.aEstado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Cargando
import com.akm.carencia.ui.componentes.Etiqueta
import com.akm.carencia.ui.componentes.ImagenRemota
import com.akm.carencia.ui.componentes.MensajeError
import com.akm.carencia.ui.componentes.Tarjeta
import com.akm.carencia.ui.componentes.Vacio
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.componentes.toque
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

// ============================ Login de administrador ============================
// Pantalla separada del login de jugadores. Usa POST api/Usuario/login-admin,
// que rechaza (403) cualquier cuenta que no tenga rol Admin.
@Composable
fun AdminLoginScreen(onListo: () -> Unit, onVolver: () -> Unit) {
    val repo = CarenciaApp.instancia.repo
    val scope = rememberCoroutineScope()
    var usuario by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun entrar() {
        error = when {
            usuario.isBlank() -> "Escribe tu usuario de administrador"
            contrasena.isBlank() -> "Escribe tu contraseña"
            else -> null
        }
        if (error != null) return
        cargando = true
        scope.launch {
            when (val r = repo.loginAdmin(usuario, contrasena)) {
                is Resultado.Ok -> onListo()
                is Resultado.Error -> { error = r.mensaje; cargando = false }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bienvenida_ambiente), null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Volver(onVolver)
            Spacer(Modifier.height(56.dp))

            // Insignia de zona restringida
            Row(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(C.ChipAcento)
                    .border(1.dp, C.Rojo.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(C.RojoClaro))
                Spacer(Modifier.width(8.dp))
                Text("Zona restringida", style = T.elite(13, C.RojoClaro))
            }
            Spacer(Modifier.height(18.dp))
            Text("Acceso de administrador", style = T.barrio(40))
            Spacer(Modifier.height(8.dp))
            Text(
                "Sólo el equipo de Carencia puede publicar novedades y anuncios del juego.",
                style = T.Cuerpo
            )
            Spacer(Modifier.height(28.dp))

            Tarjeta(padding = PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoTexto(usuario, { usuario = it.replace(" ", "") }, "Usuario administrador", icono = R.drawable.ic_perfil)
                    CampoTexto(contrasena, { contrasena = it }, "Contraseña", contrasena = true)
                    error?.let { Text(it, style = T.elite(14, C.RojoClaro)) }
                    Boton(
                        "Entrar al panel", Modifier.fillMaxWidth().padding(top = 6.dp),
                        cargando = cargando, onClick = ::entrar
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ============================ Panel de administración ============================
class AdminPanelViewModel : ViewModel() {
    private val repo = CarenciaApp.instancia.repo
    var novedades by mutableStateOf<Estado<List<Novedad>>>(Estado.Cargando); private set
    var stats by mutableStateOf<Estadistica?>(null); private set
    var aviso by mutableStateOf<String?>(null)

    fun cargar() = viewModelScope.launch {
        novedades = repo.novedades(null, null).aEstado()
        (repo.estadisticas() as? Resultado.Ok)?.let { stats = it.data }
    }

    // DELETE api/Novedad/{id}
    fun eliminar(n: Novedad) = viewModelScope.launch {
        when (val r = repo.eliminarNovedad(n.pkNovedad)) {
            is Resultado.Ok -> { aviso = "Novedad eliminada"; cargar() }
            is Resultado.Error -> aviso = r.mensaje
        }
    }

    // PATCH api/Novedad/{id}/destacar
    fun destacar(n: Novedad) = viewModelScope.launch {
        when (val r = repo.destacarNovedad(n.pkNovedad, !n.destacada)) {
            is Resultado.Ok -> { aviso = if (r.data.destacada) "Ahora es la destacada" else "Ya no está destacada"; cargar() }
            is Resultado.Error -> aviso = r.mensaje
        }
    }
}

@Composable
fun AdminPanelScreen(
    onVolver: () -> Unit,
    onPublicar: () -> Unit,
    onSubirFoto: () -> Unit,
    onEditar: (Int) -> Unit,
    onVer: (Int) -> Unit,
    onSalir: () -> Unit,
    vm: AdminPanelViewModel = viewModel()
) {
    val app = CarenciaApp.instancia
    val sesion by app.sesion.sesion.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var porEliminar by remember { mutableStateOf<Novedad?>(null) }
    var verificada by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }

    // Si la sesión se cierra o no es admin, regresa al login de administrador
    LaunchedEffect(sesion) {
        if (verificada && sesion?.esAdmin != true) onSalir()
        verificada = true
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Volver(onVolver)
                    Spacer(Modifier.weight(1f))
                    BotonCircular(R.drawable.ic_salir, "Cerrar sesión", {
                        scope.launch { app.repo.cerrarSesion(); onSalir() }
                    })
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta("Administrador", acento = true)
                    Text("Panel", style = T.barrio(44))
                    Text("Hola, ${sesion?.nombre ?: ""}. Publica novedades y administra la galería oficial; los jugadores lo ven al instante.", style = T.Cuerpo)
                }
            }
            vm.stats?.let { s ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DatoAdmin("Novedades", s.totalNovedades, Modifier.weight(1f))
                        DatoAdmin("Fotos", s.totalCapturas, Modifier.weight(1f))
                        DatoAdmin("Niveles", s.totalNiveles, Modifier.weight(1f))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Boton("+ Novedad", Modifier.weight(1f), onClick = onPublicar)
                    Boton("+ Foto galería", Modifier.weight(1f), secundario = true, onClick = onSubirFoto)
                }
            }
            item { Text("Novedades publicadas", style = T.elite(20), modifier = Modifier.padding(top = 10.dp)) }

            when (val e = vm.novedades) {
                is Estado.Cargando -> item { Cargando() }
                is Estado.Error -> item { MensajeError(e.mensaje, { vm.cargar() }) }
                is Estado.Listo -> {
                    if (e.data.isEmpty()) item { Vacio("Todavía no hay novedades. Publica la primera.") }
                    items(e.data, key = { it.pkNovedad }) { n ->
                        FilaNovedadAdmin(
                            n,
                            confirmando = porEliminar?.pkNovedad == n.pkNovedad,
                            onVer = { onVer(n.pkNovedad) },
                            onEditar = { onEditar(n.pkNovedad) },
                            onDestacar = { vm.destacar(n) },
                            onEliminar = { porEliminar = n },
                            onConfirmar = { porEliminar = null; vm.eliminar(n) },
                            onCancelar = { porEliminar = null }
                        )
                    }
                }
            }
        }
        vm.aviso?.let { Aviso(it) { vm.aviso = null } }
    }
}

@Composable
private fun DatoAdmin(titulo: String, valor: Int, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(C.Superficie)
            .border(1.dp, C.Borde, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(valor.toString(), style = T.barrio(32))
        Text(titulo, style = T.elite(12, C.Gris))
    }
}

@Composable
private fun FilaNovedadAdmin(
    n: Novedad,
    confirmando: Boolean,
    onVer: () -> Unit,
    onEditar: () -> Unit,
    onDestacar: () -> Unit,
    onEliminar: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Tarjeta(radio = 18.dp, padding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.toque(onVer)) {
                ImagenRemota(
                    n.imagenUrl, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                    respaldo = R.drawable.inicio_cap2
                )
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Etiqueta(n.categoria, acento = n.destacada)
                        Spacer(Modifier.width(8.dp))
                        Text(Fechas.corta(n.fecha), style = T.elite(12, C.Gris))
                        if (n.destacada) { Spacer(Modifier.width(8.dp)); Text("★ Destacada", style = T.elite(12, C.RojoClaro)) }
                    }
                    Text(n.titulo, style = T.elite(16), maxLines = 2)
                }
            }
            if (confirmando) {
                Text("¿Eliminar \"${n.titulo}\" para todos?", style = T.elite(14, C.RojoClaro))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Boton("Cancelar", Modifier.weight(1f), secundario = true, onClick = onCancelar)
                    Boton("Eliminar", Modifier.weight(1f), onClick = onConfirmar)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Boton("Editar", Modifier.weight(1f), secundario = true, compacto = true, onClick = onEditar)
                    Boton(if (n.destacada) "Quitar ★" else "Destacar", Modifier.weight(1f), secundario = true, compacto = true, onClick = onDestacar)
                    Boton("Borrar", Modifier.weight(1f), secundario = true, compacto = true, onClick = onEliminar)
                }
            }
        }
    }
}
