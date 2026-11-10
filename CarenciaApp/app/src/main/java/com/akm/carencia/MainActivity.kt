package com.akm.carencia

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.akm.carencia.notificaciones.Notificaciones
import com.akm.carencia.ui.navegacion.AppNavegacion
import com.akm.carencia.ui.navegacion.Destino
import com.akm.carencia.ui.theme.CarenciaTheme

class MainActivity : ComponentActivity() {

    // Destino pedido por una notificación (novedad o galería)
    private val destinoPendiente = mutableStateOf<Destino?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        leerIntent(intent)
        setContent {
            CarenciaTheme {
                AppNavegacion(
                    destinoPendiente = destinoPendiente.value,
                    onDestinoAtendido = { destinoPendiente.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerIntent(intent)
    }

    private fun leerIntent(intent: Intent?) {
        val novedad = intent?.getIntExtra(Notificaciones.EXTRA_NOVEDAD_ID, 0) ?: 0
        val irA = intent?.getStringExtra(Notificaciones.EXTRA_IR_A)
        destinoPendiente.value = when {
            novedad > 0 -> Destino.NovedadDetalle(novedad)
            irA == "galeria" -> Destino.Galeria
            else -> destinoPendiente.value
        }
        intent?.removeExtra(Notificaciones.EXTRA_NOVEDAD_ID)
        intent?.removeExtra(Notificaciones.EXTRA_IR_A)
    }
}
