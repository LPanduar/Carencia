package com.akm.carencia.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akm.carencia.R
import com.akm.carencia.data.Repositorio
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T

// ===================== App 3 / Logo =====================
// Glitch reservado al logo (Bienvenida e Inicio). Los títulos de pantalla van en Barrio limpio.
@Composable
fun Logo(modifier: Modifier = Modifier) {
    val estilo = T.barrio(64)
    Box(modifier) {
        Text("Carencia", style = estilo.copy(color = C.Rojo), modifier = Modifier.offset(x = 4.dp))
        Text("Carencia", style = estilo.copy(color = C.Azul), modifier = Modifier.offset(x = (-3).dp))
        Text("Carencia", style = estilo)
    }
}

// Título de pantalla (Barrio 40, sin glitch)
@Composable
fun TituloPantalla(texto: String, modifier: Modifier = Modifier) {
    Text(texto, style = T.barrio(40), modifier = modifier)
}

// ===================== App 3 / Etiqueta =====================
@Composable
fun Etiqueta(texto: String, acento: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (acento) C.ChipAcento else C.Chip)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(texto, style = T.elite(13, if (acento) C.RojoClaro else C.Texto), maxLines = 1)
    }
}

// ===================== App 3 / Segmento =====================
@Composable
fun Segmento(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (activo) C.Activo else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(texto, style = T.elite(14, if (activo) C.Crema else C.Gris), maxLines = 1)
    }
}

// Fila de segmentos desplazable (filtros / pestañas)
@Composable
fun Segmentos(
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = padding),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        opciones.forEach { Segmento(it, it == seleccion) { onSeleccion(it) } }
    }
}

// ===================== App 3 / Botón =====================
@Composable
fun Boton(
    texto: String,
    modifier: Modifier = Modifier,
    secundario: Boolean = false,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    compacto: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (secundario) C.Chip else C.Crema)
            .alpha(if (habilitado) 1f else 0.5f)
            .clickable(enabled = habilitado && !cargando, onClick = onClick)
            .padding(horizontal = if (compacto) 8.dp else 22.dp, vertical = if (compacto) 13.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(
                Modifier.size(20.dp), strokeWidth = 2.dp,
                color = if (secundario) C.Crema else C.Fondo
            )
        } else {
            Text(texto, style = T.elite(if (compacto) 14 else 16, if (secundario) C.Crema else C.Fondo), maxLines = 1)
        }
    }
}

// ===================== App 3 / Volver (y botones circulares) =====================
@Composable
fun BotonCircular(@DrawableRes icono: Int, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(C.VolverFondo)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icono), contentDescription = descripcion, tint = C.Crema, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun Volver(onClick: () -> Unit, modifier: Modifier = Modifier) =
    BotonCircular(R.drawable.ic_volver, "Volver", onClick, modifier)

// Botón flotante (cámara / publicar)
@Composable
fun BotonFlotante(@DrawableRes icono: Int, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(C.Crema)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icono), contentDescription = descripcion, tint = C.Fondo, modifier = Modifier.size(26.dp))
    }
}

// ===================== App 3 / Navegación (barra flotante) =====================
enum class Pestana(val ruta: String, val titulo: String, @DrawableRes val icono: Int, @DrawableRes val iconoActivo: Int) {
    Inicio("inicio", "Inicio", R.drawable.ic_nav_inicio, R.drawable.ic_nav_inicio_activo),
    Galeria("galeria", "Galería", R.drawable.ic_nav_galeria, R.drawable.ic_nav_galeria_activo),
    Niveles("niveles", "Niveles", R.drawable.ic_nav_niveles, R.drawable.ic_nav_niveles_activo),
    Novedades("novedades", "Novedades", R.drawable.ic_nav_novedades, R.drawable.ic_nav_novedades_activo)
}

@Composable
fun BarraNavegacion(activa: Pestana?, onClick: (Pestana) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(C.NavFondo)
            .border(1.dp, C.Borde, RoundedCornerShape(24.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Pestana.entries.forEach { p ->
            val esActiva = p == activa
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (esActiva) C.Activo else Color.Transparent)
                    .clickable { onClick(p) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Activa = icono relleno + fondo (los colores vienen en el vector del Figma)
                Image(
                    painterResource(if (esActiva) p.iconoActivo else p.icono),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text(p.titulo, style = T.elite(11, if (esActiva) C.Crema else C.Gris), maxLines = 1)
            }
        }
    }
}

// Alto reservado al final de cada pantalla para que la barra flotante no tape contenido
val EspacioBarra = 120.dp

// ===================== Imágenes =====================
@Composable
fun ImagenRemota(
    ruta: String?,
    modifier: Modifier = Modifier,
    alineacion: Alignment = Alignment.Center,
    descripcion: String? = null,
    @DrawableRes respaldo: Int? = null // si no hay imagen (ej. novedad publicada sin foto)
) {
    if (ruta.isNullOrBlank() && respaldo != null) {
        Image(painterResource(respaldo), descripcion, contentScale = ContentScale.Crop, alignment = alineacion, modifier = modifier)
        return
    }
    AsyncImage(
        model = Repositorio.urlImagen(ruta),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        alignment = alineacion,
        placeholder = ColorPainter(C.Superficie),
        error = ColorPainter(C.Superficie),
        modifier = modifier
    )
}

// Portada con los dos degradados del Figma ("Fundido" y "Fundido superior")
@Composable
fun Portada(alto: Dp, contenido: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().height(alto)) {
        contenido()
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.3f to C.Fondo.copy(alpha = 0.15f), 1f to C.Fondo)
            )
        )
        Box(
            Modifier.fillMaxWidth().height(120.dp).background(
                Brush.verticalGradient(listOf(C.Fondo.copy(alpha = 0.55f), Color.Transparent))
            )
        )
    }
}

@Composable
fun PortadaLocal(@DrawableRes imagen: Int, alto: Dp) = Portada(alto) {
    Image(
        painterResource(imagen), null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

// ===================== Tarjetas y listas =====================
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    radio: Dp = 22.dp,
    padding: PaddingValues = PaddingValues(14.dp),
    contenido: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radio))
            .background(C.Superficie)
            .border(BorderStroke(1.dp, C.Borde), RoundedCornerShape(radio))
            .padding(padding)
    ) { contenido() }
}

// Lista con punto rojo (bloque "Ambiente" / "Personajes" del Figma)
@Composable
fun ListaConPuntos(items: List<String>, modifier: Modifier = Modifier) {
    Tarjeta(modifier, radio = 20.dp, padding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
        Column {
            items.forEachIndexed { i, texto ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(C.Rojo))
                    Spacer(Modifier.width(12.dp))
                    Text(texto, style = T.elite(16))
                }
                if (i < items.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(C.Borde))
            }
        }
    }
}

@Composable
fun EncabezadoSeccion(titulo: String, accion: String? = null, onAccion: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = T.elite(20), modifier = Modifier.weight(1f))
        if (accion != null) {
            Text(accion, style = T.elite(14, C.Gris), modifier = Modifier.clickable(onClick = onAccion))
        }
    }
}

// ===================== Campo de texto con el estilo del juego =====================
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    contrasena: Boolean = false,
    lineas: Int = 1,
    @DrawableRes icono: Int? = null,
    teclado: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(C.Superficie)
            .border(1.dp, C.Borde, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = if (lineas > 1) Alignment.Top else Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(painterResource(icono), null, tint = C.Gris, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        BasicTextField(
            value = valor,
            onValueChange = onCambio,
            textStyle = T.elite(15),
            singleLine = lineas == 1,
            minLines = lineas,
            cursorBrush = SolidColor(C.RojoClaro),
            visualTransformation = if (contrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (contrasena) KeyboardType.Password else teclado),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (valor.isEmpty()) Text(placeholder, style = T.elite(15, C.Gris))
                    inner()
                }
            }
        )
    }
}

// ===================== Estados de carga / error / vacío =====================
@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = C.RojoClaro, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun MensajeError(mensaje: String, onReintentar: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(mensaje, style = T.elite(15, C.Texto, 1.5f), textAlign = TextAlign.Center)
        if (onReintentar != null) {
            Spacer(Modifier.height(16.dp))
            Boton("Reintentar", secundario = true, onClick = onReintentar)
        }
    }
}

@Composable
fun Vacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto, style = T.elite(15, C.Gris, 1.5f), textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(32.dp)
    )
}

// Clic sin efecto de onda (para tarjetas grandes con imagen)
fun Modifier.toque(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)
)

@Composable
fun IconoTintado(@DrawableRes icono: Int, color: Color, modifier: Modifier = Modifier) {
    Image(painterResource(icono), null, colorFilter = ColorFilter.tint(color), modifier = modifier)
}

@Composable
fun rememberSinIndicacion() = remember { MutableInteractionSource() }
