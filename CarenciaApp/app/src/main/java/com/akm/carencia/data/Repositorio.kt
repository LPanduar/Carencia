package com.akm.carencia.data

import com.akm.carencia.BuildConfig
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface Resultado<out T> {
    data class Ok<T>(val data: T, val mensaje: String? = null) : Resultado<T>
    data class Error(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>
}

// Único punto de acceso a la API: arma Retrofit, agrega el JWT y traduce errores
class Repositorio(private val sesion: SesionManager) {

    private val gson = Gson()

    private val cliente: OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val token = sesion.tokenActual
                    val req = chain.request().newBuilder().apply {
                        if (token != null) header("Authorization", "Bearer $token")
                    }.build()
                    chain.proceed(req)
                }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
                })
                .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)

    // ----- Usuario -----
    // Sólo cuentas con rol Admin (la API responde 403 a cualquier otra)
    suspend fun loginAdmin(user: String, pass: String) =
        llamar { api.loginAdmin(LoginRequest(user.trim(), pass)) }.alGuardar()

    suspend fun cerrarSesion() = sesion.cerrar()

    // PUT api/Usuario/contrasena (admin de la sesión)
    suspend fun cambiarContrasena(actual: String, nueva: String) =
        llamar { api.cambiarContrasena(CambiarContrasenaRequest(actual, nueva)) }

    // ----- Niveles -----
    suspend fun niveles() = llamar { api.niveles() }
    suspend fun nivel(id: Int) = llamar { api.nivel(id) }

    // ----- Novedades -----
    suspend fun novedades(categoria: String?, buscar: String?) =
        llamar { api.novedades(categoria, buscar?.ifBlank { null }) }

    suspend fun novedad(id: Int) = llamar { api.novedad(id) }
    suspend fun ultimaNovedad() = llamar { api.ultimaNovedad() }

    suspend fun crearNovedad(
        titulo: String, resumen: String, contenido: String, categoria: String,
        destacada: Boolean, imagen: File?
    ) = llamar {
        api.crearNovedad(
            titulo.texto(), resumen.texto(), contenido.texto(), categoria.texto(),
            destacada.toString().texto(), imagen?.parte()
        )
    }

    suspend fun actualizarNovedad(id: Int, body: NovedadUpdateRequest) =
        llamar { api.actualizarNovedad(id, body) }

    suspend fun destacarNovedad(id: Int, destacada: Boolean) =
        llamar { api.destacarNovedad(id, DestacarRequest(destacada)) }

    suspend fun cambiarImagenNovedad(id: Int, imagen: File) =
        llamar { api.cambiarImagenNovedad(id, imagen.parte()) }

    suspend fun eliminarNovedad(id: Int) = llamar { api.eliminarNovedad(id) }

    // ----- Capturas -----
    suspend fun capturas(categoria: String?, buscar: String?) =
        llamar { api.capturas(categoria, buscar?.ifBlank { null }) }

    suspend fun crearCaptura(titulo: String, categoria: String, imagen: File) =
        llamar { api.crearCaptura(titulo.texto(), categoria.texto(), imagen.parte()) }

    suspend fun actualizarCaptura(id: Int, titulo: String, categoria: String) =
        llamar { api.actualizarCaptura(id, CapturaUpdateRequest(titulo.trim(), categoria)) }

    suspend fun eliminarCaptura(id: Int) = llamar { api.eliminarCaptura(id) }

    // Descarga los bytes de una foto oficial (para guardarla en el teléfono)
    suspend fun descargarImagen(ruta: String): ByteArray? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            cliente.newCall(okhttp3.Request.Builder().url(urlImagen(ruta)!!).build()).execute().use { r ->
                if (r.isSuccessful) r.body?.bytes() else null
            }
        }.getOrNull()
    }

    // ----- Estadísticas -----
    suspend fun estadisticas() = llamar { api.estadisticas() }

    // ---------- utilidades ----------
    private suspend fun Resultado<LoginResponse>.alGuardar(): Resultado<LoginResponse> {
        if (this is Resultado.Ok) sesion.guardar(data)
        return this
    }

    private suspend fun <T> llamar(bloque: suspend () -> Response<ApiResponse<T>>): Resultado<T> =
        try {
            val r = bloque()
            val body = r.body()
            if (r.isSuccessful && body != null && body.succeded && body.result != null) {
                Resultado.Ok(body.result, body.message)
            } else if (r.isSuccessful && body != null && body.succeded) {
                Resultado.Error(body.message ?: "Sin datos", 204) // ej. todavía no hay novedades
            } else {
                if (r.code() == 401) sesion.cerrar()
                Resultado.Error(mensajeDeError(r), r.code())
            }
        } catch (e: IOException) {
            Resultado.Error("No se pudo conectar con el servidor. ¿Está corriendo CarenciaAPI?")
        } catch (e: Exception) {
            Resultado.Error(e.message ?: "Error inesperado")
        }

    private fun mensajeDeError(r: Response<*>): String {
        val texto = r.errorBody()?.string()
        val msg = runCatching { gson.fromJson(texto, ApiResponse::class.java)?.message }.getOrNull()
        return msg ?: when (r.code()) {
            401 -> "Inicia sesión para continuar"
            403 -> "No tienes permiso para esta acción"
            404 -> "No se encontró"
            else -> "Error del servidor (${r.code()})"
        }
    }

    private fun String.texto(): RequestBody = trim().toRequestBody("text/plain".toMediaType())

    private fun File.parte(): MultipartBody.Part {
        val tipo = if (extension.equals("png", true)) "image/png" else "image/jpeg"
        return MultipartBody.Part.createFormData("imagen", name, asRequestBody(tipo.toMediaType()))
    }

    companion object {
        // Las imágenes vienen como rutas relativas (/img/..., /uploads/...)
        fun urlImagen(ruta: String?): String? =
            ruta?.let { if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it }
    }
}
