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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.IconoTintado
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Admin: sube una foto oficial a la galería del juego (POST api/Captura, multipart)
@Composable
fun AdminFotoScreen(onListo: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imagen by remember { mutableStateOf<Uri?>(null) }
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categorias.Captura.first()) }
    var subiendo by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imagen = uri
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
        Text("Nueva foto", style = T.barrio(36))
        Text("Se agrega a la galería oficial y todos los jugadores podrán verla y descargarla.", style = T.elite(14, C.Gris, 1.5f))

        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(C.Superficie)
                .border(1.dp, C.Borde, RoundedCornerShape(18.dp))
                .clickable { selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentAlignment = Alignment.Center
        ) {
            if (imagen != null) {
                AsyncImage(imagen, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconoTintado(R.drawable.ic_mas, C.Gris, Modifier.size(28.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Elegir imagen", style = T.elite(14, C.Gris))
                }
            }
        }

        CampoTexto(titulo, { titulo = it.take(100) }, "Título")
        Text("Categoría", style = T.elite(16))
        Segmentos(Categorias.Captura, categoria, { categoria = it }, padding = 0.dp)
        error?.let { Text(it, style = T.elite(14, C.RojoClaro)) }

        Boton(
            "Subir a la galería", Modifier.fillMaxWidth().padding(top = 6.dp),
            cargando = subiendo, habilitado = imagen != null && titulo.isNotBlank()
        ) {
            subiendo = true; error = null
            scope.launch {
                val archivo = withContext(Dispatchers.IO) { copiarACache(context, imagen!!) }
                val r = CarenciaApp.instancia.repo.crearCaptura(titulo, categoria, archivo)
                archivo.delete()
                when (r) {
                    is Resultado.Ok -> onListo()
                    is Resultado.Error -> { error = r.mensaje; subiendo = false }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun copiarACache(context: Context, uri: Uri): File {
    val ext = if (context.contentResolver.getType(uri) == "image/png") "png" else "jpg"
    val destino = File(context.cacheDir, "foto_${System.currentTimeMillis()}.$ext")
    context.contentResolver.openInputStream(uri)!!.use { entrada -> destino.outputStream().use { entrada.copyTo(it) } }
    return destino
}
