package com.akm.carencia.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.akm.carencia.MainActivity
import com.akm.carencia.R

// Componente del dispositivo: notificaciones locales
object Notificaciones {
    const val CANAL_NOVEDADES = "novedades"
    const val CANAL_CAPTURAS = "capturas"
    const val EXTRA_NOVEDAD_ID = "novedad_id"
    const val EXTRA_IR_A = "ir_a"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CANAL_NOVEDADES, "Novedades del juego", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Anuncios y actualizaciones publicados por el equipo de Carencia" }
        )
        nm.createNotificationChannel(
            NotificationChannel(CANAL_CAPTURAS, "Mis capturas", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Confirmación cuando tu foto se guarda en la galería" }
        )
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun novedadPublicada(context: Context, id: Int, titulo: String, resumen: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOVEDAD_ID, id)
        }
        mostrar(
            context, CANAL_NOVEDADES, 1000 + id,
            titulo = "Nueva novedad: $titulo",
            texto = resumen,
            intent = intent
        )
    }

    fun capturaGuardada(context: Context, titulo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_IR_A, "galeria")
        }
        mostrar(
            context, CANAL_CAPTURAS, 2000,
            titulo = "El espejo guardó tu captura",
            texto = "\"$titulo\" ya está en Galería → Mis fotos.",
            intent = intent
        )
    }

    private fun mostrar(context: Context, canal: String, id: Int, titulo: String, texto: String, intent: Intent) {
        if (!tienePermiso(context)) return
        val pending = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(ContextCompat.getColor(context, R.color.rojo))
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (_: SecurityException) {
            // El usuario revocó el permiso
        }
    }
}
