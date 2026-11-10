package com.akm.carencia.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akm.carencia.CarenciaApp
import com.akm.carencia.data.Resultado
import com.akm.carencia.ui.componentes.Boton
import com.akm.carencia.ui.componentes.CampoTexto
import com.akm.carencia.ui.componentes.Volver
import com.akm.carencia.ui.theme.C
import com.akm.carencia.ui.theme.T
import kotlinx.coroutines.launch

// El administrador cambia su propia contraseña (PUT api/Usuario/contrasena)
@Composable
fun AdminContrasenaScreen(onListo: (String) -> Unit, onVolver: () -> Unit) {
    val scope = rememberCoroutineScope()
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var guardando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun guardar() {
        error = when {
            actual.isBlank() -> "Escribe tu contraseña actual"
            nueva.length < 8 -> "La nueva contraseña debe tener al menos 8 caracteres"
            nueva != confirmar -> "Las contraseñas nuevas no coinciden"
            nueva == actual -> "La nueva contraseña debe ser diferente a la actual"
            else -> null
        }
        if (error != null) return
        guardando = true
        scope.launch {
            when (val r = CarenciaApp.instancia.repo.cambiarContrasena(actual, nueva)) {
                is Resultado.Ok -> onListo(r.mensaje ?: "Contraseña actualizada")
                is Resultado.Error -> { error = r.mensaje; guardando = false }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(0.dp))
        Volver(onVolver)
        Text("Cambiar contraseña", style = T.barrio(36))
        Text(
            "Usa una contraseña de al menos 8 caracteres que sólo conozca el equipo.",
            style = T.elite(14, C.Gris, 1.5f)
        )
        CampoTexto(actual, { actual = it }, "Contraseña actual", contrasena = true)
        CampoTexto(nueva, { nueva = it }, "Nueva contraseña", contrasena = true)
        CampoTexto(confirmar, { confirmar = it }, "Repite la nueva contraseña", contrasena = true)
        error?.let { Text(it, style = T.elite(14, C.RojoClaro)) }
        Boton("Guardar", Modifier.fillMaxWidth().padding(top = 6.dp), cargando = guardando, onClick = ::guardar)
        Spacer(Modifier.height(32.dp))
    }
}
