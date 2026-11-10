package com.akm.carencia.data

// Modelos que devuelve CarenciaAPI (JSON camelCase). Todas las respuestas
// vienen envueltas en { succeded, message, result }.

data class ApiResponse<T>(
    val succeded: Boolean,
    val message: String?,
    val result: T?
)

data class Usuario(
    val pkUsuario: Int,
    val nombre: String,
    val userName: String,
    val estatus: Boolean,
    val fkRol: Int,
    val rolNombre: String?,
    val fechaRegistro: String?
) {
    val esAdmin get() = rolNombre == "Admin"
}

data class LoginResponse(
    val token: String,
    val expira: String,
    val usuario: Usuario
)

data class Nivel(
    val pkNivel: Int,
    val numero: Int,
    val titulo: String,
    val descripcion: String,
    val duracionMin: Int,
    val imagenTarjeta: String?,
    val imagenPortada: String?,
    val detalles: List<NivelDetalle>?,
    val capturas: List<NivelCaptura>?
)

data class NivelDetalle(
    val pkNivelDetalle: Int,
    val tipo: String,
    val texto: String,
    val orden: Int
)

data class NivelCaptura(
    val pkNivelCaptura: Int,
    val imagenUrl: String,
    val orden: Int
)

data class Novedad(
    val pkNovedad: Int,
    val titulo: String,
    val resumen: String,
    val contenido: String,
    val categoria: String,
    val imagenUrl: String?,
    val destacada: Boolean,
    val fecha: String,
    val fkAutor: Int?,
    val autorNombre: String?
)

data class Captura(
    val pkCaptura: Int,
    val titulo: String,
    val categoria: String,
    val imagenUrl: String,
    val fkUsuario: Int?,
    val usuarioNombre: String?,
    val fecha: String
)

data class ConteoCategoria(val categoria: String, val total: Int)
data class UsuarioTop(val nombre: String, val total: Int)

data class Estadistica(
    val totalUsuarios: Int,
    val totalCapturas: Int,
    val totalCapturasComunidad: Int,
    val totalNovedades: Int,
    val totalNiveles: Int,
    val capturasPorCategoria: List<ConteoCategoria>?,
    val novedadesPorCategoria: List<ConteoCategoria>?,
    val topFotografos: List<UsuarioTop>?
)

// ---------- Cuerpos de petición ----------
data class LoginRequest(val userName: String, val password: String)

data class NovedadUpdateRequest(
    val titulo: String,
    val resumen: String,
    val contenido: String,
    val categoria: String,
    val destacada: Boolean
)
data class DestacarRequest(val destacada: Boolean)
data class CapturaUpdateRequest(val titulo: String, val categoria: String)

object Categorias {
    val Novedad = listOf("Interfaz", "Mecánicas", "Arte", "Anuncio")
    val Captura = listOf("Escenarios", "Personajes", "Arte")
}
