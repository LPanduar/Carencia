package com.akm.carencia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.akm.carencia.R

// ---------- Colores (tokens del Figma "OFICIAL - CARENCIA APP MOVIL") ----------
object C {
    val Fondo = Color(0xFF080A0E)
    val Superficie = Color(0xFF11151C)
    val Borde = Color(0xFF232A35)
    val Chip = Color(0xFF1A1F28)
    val Activo = Color(0xFF262D39)
    val Crema = Color(0xFFECE6D8)
    val Texto = Color(0xFFB2BCC6)
    val Gris = Color(0xFF8A9BA8)
    val Rojo = Color(0xFFC21A1F)
    val RojoClaro = Color(0xFFE85458)
    val Azul = Color(0xFF5FA8C8)
    val ChipAcento = Color(0x2EC21A1F)      // rgba(194,26,31,0.18)
    val NavFondo = Color(0xF01A1F28)        // rgba(26,31,40,0.94)
    val VolverFondo = Color(0xD91A1F28)     // rgba(26,31,40,0.85)
}

// ---------- Tipografía ----------
val Barrio = FontFamily(Font(R.font.barrio))
val SpecialElite = FontFamily(Font(R.font.special_elite))

object T {
    fun barrio(size: Int, color: Color = C.Crema) =
        TextStyle(fontFamily = Barrio, fontSize = size.sp, color = color)

    fun elite(size: Int, color: Color = C.Crema, interlineado: Float? = null) =
        TextStyle(
            fontFamily = SpecialElite,
            fontSize = size.sp,
            color = color,
            lineHeight = interlineado?.em ?: TextStyle.Default.lineHeight
        )

    val Cuerpo = elite(15, C.Texto, 1.55f)
}

private val Esquema = darkColorScheme(
    primary = C.Crema,
    onPrimary = C.Fondo,
    secondary = C.RojoClaro,
    background = C.Fondo,
    onBackground = C.Crema,
    surface = C.Superficie,
    onSurface = C.Crema,
    surfaceVariant = C.Chip,
    onSurfaceVariant = C.Texto,
    outline = C.Borde,
    error = C.RojoClaro
)

@Composable
fun CarenciaTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        colorScheme = Esquema,
        typography = Typography(
            bodyLarge = base.bodyLarge.copy(fontFamily = SpecialElite),
            bodyMedium = base.bodyMedium.copy(fontFamily = SpecialElite),
            labelLarge = base.labelLarge.copy(fontFamily = SpecialElite),
            titleLarge = base.titleLarge.copy(fontFamily = Barrio)
        ),
        content = content
    )
}
