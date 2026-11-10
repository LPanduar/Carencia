package com.akm.carencia.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// Foto tomada con "El espejo". Vive sólo en este teléfono (no se sube a la API).
data class FotoLocal(
    val id: String,
    val titulo: String,
    val archivo: String,
    val fecha: Long
)

/**
 * Componente ALMACENAMIENTO del dispositivo:
 * guarda las fotos del espejo en la memoria interna de la app (filesDir/espejo)
 * con un índice JSON para sus títulos. CRUD local: listar, guardar, renombrar, eliminar.
 */
class FotosLocales(private val context: Context) {
    private val gson = Gson()
    private val carpeta = File(context.filesDir, "espejo").apply { mkdirs() }
    private val indice = File(carpeta, "indice.json")
    private val tipoLista = object : TypeToken<List<FotoLocal>>() {}.type

    fun archivoDe(foto: FotoLocal) = File(carpeta, foto.archivo)

    suspend fun listar(): List<FotoLocal> = withContext(Dispatchers.IO) {
        leer().filter { archivoDe(it).exists() }.sortedByDescending { it.fecha }
    }

    suspend fun guardar(foto: Bitmap, titulo: String): FotoLocal = withContext(Dispatchers.IO) {
        val nueva = FotoLocal(UUID.randomUUID().toString(), titulo.trim(), "${System.currentTimeMillis()}.jpg", System.currentTimeMillis())
        File(carpeta, nueva.archivo).outputStream().use { foto.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        escribir(leer() + nueva)
        nueva
    }

    suspend fun renombrar(id: String, titulo: String) = withContext(Dispatchers.IO) {
        escribir(leer().map { if (it.id == id) it.copy(titulo = titulo.trim()) else it })
    }

    suspend fun eliminar(id: String) = withContext(Dispatchers.IO) {
        val lista = leer()
        lista.firstOrNull { it.id == id }?.let { archivoDe(it).delete() }
        escribir(lista.filterNot { it.id == id })
    }

    @Synchronized
    private fun leer(): List<FotoLocal> =
        if (!indice.exists()) emptyList()
        else runCatching { gson.fromJson<List<FotoLocal>>(indice.readText(), tipoLista) }.getOrNull().orEmpty()

    @Synchronized
    private fun escribir(lista: List<FotoLocal>) = indice.writeText(gson.toJson(lista))
}

/**
 * Copia una imagen a la galería del teléfono (Imágenes/Carencia).
 * Android 10+: MediaStore, sin permisos. Android 7–9: requiere WRITE_EXTERNAL_STORAGE.
 */
object GaleriaTelefono {

    fun necesitaPermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED

    suspend fun guardar(context: Context, bytes: ByteArray, nombre: String): Boolean = withContext(Dispatchers.IO) {
        val limpio = nombre.replace(Regex("[^A-Za-z0-9áéíóúñÁÉÍÓÚÑ _-]"), "").trim().ifBlank { "carencia" }
        val esPng = bytes.size > 4 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()
        val mime = if (esPng) "image/png" else "image/jpeg"
        val archivo = "${limpio}_${System.currentTimeMillis()}.${if (esPng) "png" else "jpg"}"
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val valores = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, archivo)
                    put(MediaStore.Images.Media.MIME_TYPE, mime)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Carencia")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)!!
                resolver.openOutputStream(uri)!!.use { it.write(bytes) }
                valores.clear(); valores.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, valores, null, null)
            } else {
                @Suppress("DEPRECATION")
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Carencia")
                dir.mkdirs()
                val destino = File(dir, archivo)
                destino.writeBytes(bytes)
                MediaScannerConnection.scanFile(context, arrayOf(destino.absolutePath), arrayOf(mime), null)
            }
            true
        }.getOrDefault(false)
    }
}
