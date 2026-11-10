package com.akm.carencia.ui

import com.akm.carencia.data.Resultado
import java.text.SimpleDateFormat
import java.util.Locale

// Estado genérico de una pantalla que carga datos de la API
sealed interface Estado<out T> {
    data object Cargando : Estado<Nothing>
    data class Listo<T>(val data: T) : Estado<T>
    data class Error(val mensaje: String) : Estado<Nothing>
}

fun <T> Resultado<T>.aEstado(): Estado<T> = when (this) {
    is Resultado.Ok -> Estado.Listo(data)
    is Resultado.Error -> Estado.Error(mensaje)
}

object Fechas {
    private val es = Locale.Builder().setLanguage("es").setRegion("MX").build()
    private val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    private fun formatear(iso: String, patron: String): String = runCatching {
        val fecha = entrada.parse(iso.take(19))!!
        SimpleDateFormat(patron, es).format(fecha).replace(".", "")
    }.getOrDefault(iso.take(10))

    fun larga(iso: String) = formatear(iso, "d MMM yyyy")   // 27 oct 2026
    fun corta(iso: String) = formatear(iso, "d MMM")        // 5 oct
}
