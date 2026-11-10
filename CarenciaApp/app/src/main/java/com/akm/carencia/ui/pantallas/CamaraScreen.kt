package com.akm.carencia.ui.pantallas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.akm.carencia.CarenciaApp
import com.akm.carencia.R
import com.akm.carencia.data.Categorias
import com.akm.carencia.data.Resultado
import com.akm.carencia.notificaciones.Notificaciones
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.BotonCircular
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Segmentos
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

// Forma de arco del espejo (igual que en la Bienvenida)
private val FormaEspejo = RoundedCornerShape(topStart = 160.dp, topEnd = 160.dp, bottomStart = 28.dp, bottomEnd = 28.dp)

/**
 * "El espejo": componente CÁMARA del dispositivo con CameraX.
 * Arranca con la cámara frontal (efecto espejo), toma la foto, la reduce
 * y la guarda en el almacenamiento del teléfono (Mis fotos). Al terminar lanza una notificación local.
 */
@Composable
fun CamaraScreen(onCerrar: () -> Unit) {
    val context = LocalContext.current
    var permiso by remember { mutableStateOf(tienePermisoCamara(context)) }
    var negado by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permiso = it; negado = !it
    }
    LaunchedEffect(Unit) { if (!permiso) launcher.launch(Manifest.permission.CAMERA) }

    var foto by remember { mutableStateOf<Bitmap?>(null) }

    Box(Modifier.fillMaxSize().background(C.Fondo)) {
        when {
            !permiso -> SinPermiso(negado, onPedir = {
                if (negado) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                } else launcher.launch(Manifest.permission.CAMERA)
            })
            foto == null -> Visor(onFoto = { foto = it })
            else -> Guardar(foto!!, onRepetir = { foto = null }, onGuardada = onCerrar)
        }
        BotonCircular(
            R.drawable.ic_cerrar, "Cerrar", onCerrar,
            Modifier.statusBarsPadding().padding(start = 24.dp, top = 8.dp)
        )
    }
}

@Composable
private fun Visor(onFoto: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var frontal by remember { mutableStateOf(true) }
    var capturando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE // permite recortar en arco
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    DisposableEffect(frontal) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val selector = if (frontal) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
                error = null
            } catch (e: Exception) {
                error = "No se pudo abrir la cámara ${if (frontal) "frontal" else "trasera"}"
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose { runCatching { future.get().unbindAll() } }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        Text("El espejo", style = T.barrio(40))
        Text("Mírate. Tal vez algo te mire de vuelta.", style = T.elite(14, C.Gris), textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        // El arco del espejo cabe completo: ancho = min(ancho disponible, alto × 0.72)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val ancho = minOf(maxWidth, maxHeight * 0.72f)
            Box(
                Modifier
                    .size(ancho, ancho / 0.72f)
                    .clip(FormaEspejo)
                    .border(1.5.dp, C.Crema.copy(alpha = 0.14f), FormaEspejo)
                    .background(C.Superficie)
            ) {
                AndroidView({ previewView }, Modifier.fillMaxSize())
                error?.let {
                    Text(it, style = T.elite(14, C.RojoClaro), textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(44.dp))
            // Obturador
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(3.dp, C.Crema, CircleShape)
                    .padding(7.dp)
                    .clip(CircleShape)
                    .background(if (capturando) C.Gris else C.Crema)
                    .clickable(enabled = !capturando && error == null) {
                        capturando = true
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bmp = image.aBitmapDerecho(espejar = frontal)
                                    image.close()
                                    capturando = false
                                    onFoto(bmp)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    capturando = false
                                    error = "No se pudo tomar la foto"
                                }
                            }
                        )
                    }
            )
            BotonCircular(R.drawable.ic_girar, "Cambiar cámara", { frontal = !frontal })
        }
    }
}

@Composable
private fun Guardar(foto: Bitmap, onRepetir: () -> Unit, onGuardada: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var titulo by remember { mutableStateOf("") }
    var subiendo by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        Image(
            foto.asImageBitmap(), "Tu captura",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(0.72f)
                .clip(FormaEspejo)
                .border(1.5.dp, C.Crema.copy(alpha = 0.14f), FormaEspejo)
        )
        Spacer(Modifier.height(24.dp))
        CampoTexto(titulo, { titulo = it.take(100) }, "Ponle un título…")
        Spacer(Modifier.height(10.dp))
        Text("Se guardará en este teléfono, en Galería → Mis fotos.", style = T.elite(13, C.Gris))
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = T.elite(14, C.RojoClaro))
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Boton("Repetir", Modifier.weight(1f), secundario = true, habilitado = !subiendo, onClick = onRepetir)
            Boton(
                "Guardar", Modifier.weight(1f), cargando = subiendo, habilitado = titulo.isNotBlank()
            ) {
                subiendo = true; error = null
                // Almacenamiento local del teléfono (no se sube a la API)
                scope.launch {
                    try {
                        val guardada = CarenciaApp.instancia.fotos.guardar(foto, titulo)
                        Notificaciones.capturaGuardada(context, guardada.titulo)
                        onGuardada()
                    } catch (e: Exception) {
                        subiendo = false; error = "No se pudo guardar la foto"
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SinPermiso(negado: Boolean, onPedir: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("El espejo", style = T.barrio(40))
        Spacer(Modifier.height(12.dp))
        Text(
            "Para tomar capturas necesitamos usar la cámara del teléfono.",
            style = T.elite(15, C.Texto, 1.5f), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Boton(if (negado) "Abrir ajustes" else "Permitir cámara", onClick = onPedir)
    }
}

private fun tienePermisoCamara(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

// Endereza la foto según la rotación del sensor y la espeja si es la cámara frontal
private fun ImageProxy.aBitmapDerecho(espejar: Boolean): Bitmap {
    val original = toBitmap()
    val m = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
        if (espejar) postScale(-1f, 1f)
    }
    val girada = Bitmap.createBitmap(original, 0, 0, original.width, original.height, m, true)
    // Lado mayor a 1600 px para no subir fotos de varios MB
    val escala = 1600f / max(girada.width, girada.height)
    return if (escala < 1f) {
        Bitmap.createScaledBitmap(girada, (girada.width * escala).toInt(), (girada.height * escala).toInt(), true)
    } else girada
}
