package com.akm.carencia.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.akm.carencia.CarenciaApp
import com.akm.carencia.notificaciones.NovedadesWorker

private const val ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
private const val ANDROID_17 = 37

private fun Context.tiene(permiso: String) =
    ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED

/**
 * Permisos en tiempo de ejecución al abrir la app:
 *  - Red local (Android 17+): sin él, la app no puede llegar a CarenciaAPI en 10.0.2.2 o en la IP de la PC.
 *  - Notificaciones (Android 13+): se pide una sola vez.
 * La cámara se pide aparte, al abrir "El espejo".
 */
@Composable
fun PedirPermisosIniciales(onRedConcedida: () -> Unit) {
    val context = LocalContext.current
    val sesion = CarenciaApp.instancia.sesion
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r[ACCESS_LOCAL_NETWORK] == true) {
            onRedConcedida()
            NovedadesWorker.revisarAhora(context)
        }
    }
    LaunchedEffect(Unit) {
        val pedir = buildList {
            if (Build.VERSION.SDK_INT >= ANDROID_17 && !context.tiene(ACCESS_LOCAL_NETWORK)) add(ACCESS_LOCAL_NETWORK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !context.tiene(Manifest.permission.POST_NOTIFICATIONS) &&
                !sesion.permisoNotificacionesPedido()
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (pedir.contains(Manifest.permission.POST_NOTIFICATIONS)) sesion.marcarPermisoPedido()
        if (pedir.isNotEmpty()) launcher.launch(pedir.toTypedArray())
    }
}
