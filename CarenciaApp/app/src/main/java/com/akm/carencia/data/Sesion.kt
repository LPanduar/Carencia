package com.akm.carencia.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Sesion(
    val token: String,
    val pkUsuario: Int,
    val nombre: String,
    val userName: String,
    val rol: String
) {
    val esAdmin get() = rol == "Admin"
}

private val Context.dataStore by preferencesDataStore(name = "carencia")

// Almacenamiento local del dispositivo (DataStore): sesión y última novedad notificada
class SesionManager(private val context: Context) {

    private object K {
        val Token = stringPreferencesKey("token")
        val Id = intPreferencesKey("pk_usuario")
        val Nombre = stringPreferencesKey("nombre")
        val UserName = stringPreferencesKey("user_name")
        val Rol = stringPreferencesKey("rol")
        val UltimaNovedad = intPreferencesKey("ultima_novedad_vista")
        val PermisoPedido = booleanPreferencesKey("permiso_notif_pedido")
    }

    // Copia en memoria para que el interceptor HTTP agregue el token sin bloquear
    @Volatile
    var tokenActual: String? = null
        private set

    val sesion: Flow<Sesion?> = context.dataStore.data.map { p -> p.aSesion() }

    suspend fun cargar() {
        tokenActual = context.dataStore.data.first()[K.Token]
    }

    suspend fun guardar(login: LoginResponse) {
        context.dataStore.edit {
            it[K.Token] = login.token
            it[K.Id] = login.usuario.pkUsuario
            it[K.Nombre] = login.usuario.nombre
            it[K.UserName] = login.usuario.userName
            it[K.Rol] = login.usuario.rolNombre ?: "Usuario"
        }
        tokenActual = login.token
    }

    suspend fun cerrar() {
        context.dataStore.edit {
            it.remove(K.Token); it.remove(K.Id); it.remove(K.Nombre)
            it.remove(K.UserName); it.remove(K.Rol)
        }
        tokenActual = null
    }

    suspend fun ultimaNovedadVista(): Int = context.dataStore.data.first()[K.UltimaNovedad] ?: 0

    suspend fun marcarNovedadVista(id: Int) {
        context.dataStore.edit { if ((it[K.UltimaNovedad] ?: 0) < id) it[K.UltimaNovedad] = id }
    }

    suspend fun permisoNotificacionesPedido(): Boolean = context.dataStore.data.first()[K.PermisoPedido] ?: false

    suspend fun marcarPermisoPedido() {
        context.dataStore.edit { it[K.PermisoPedido] = true }
    }

    private fun Preferences.aSesion(): Sesion? {
        val token = this[K.Token] ?: return null
        return Sesion(
            token = token,
            pkUsuario = this[K.Id] ?: 0,
            nombre = this[K.Nombre] ?: "",
            userName = this[K.UserName] ?: "",
            rol = this[K.Rol] ?: "Usuario"
        )
    }
}
