package com.akm.carencia.ui.navegacion

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.akm.carencia.ui.PedirPermisosIniciales
import com.akm.carencia.ui.componentes.BarraNavegacion
import com.akm.carencia.ui.componentes.Pestana
import androidx.compose.runtime.collectAsState
import com.akm.carencia.ui.pantallas.AdminContrasenaScreen
import com.akm.carencia.ui.pantallas.AdminFotoScreen
import com.akm.carencia.ui.pantallas.AdminLoginScreen
import com.akm.carencia.ui.pantallas.AdminPanelScreen
import com.akm.carencia.ui.pantallas.CamaraScreen
import com.akm.carencia.ui.pantallas.GaleriaScreen
import com.akm.carencia.ui.pantallas.InicioScreen
import com.akm.carencia.ui.pantallas.NivelDetalleScreen
import com.akm.carencia.ui.pantallas.NivelesScreen
import com.akm.carencia.ui.pantallas.NovedadDetalleScreen
import com.akm.carencia.ui.pantallas.NovedadFormScreen
import com.akm.carencia.ui.pantallas.NovedadesScreen
import com.akm.carencia.ui.pantallas.PerfilScreen
import com.akm.carencia.ui.pantallas.SplashScreen
import com.akm.carencia.ui.theme.C

sealed class Destino(val ruta: String) {
    data object Splash : Destino("splash")
    data object Inicio : Destino("inicio")
    data object Galeria : Destino("galeria")
    data object Niveles : Destino("niveles")
    data object Novedades : Destino("novedades")
    data object Camara : Destino("camara")
    data object Perfil : Destino("perfil")
    data object AdminLogin : Destino("admin_login")
    data object AdminPanel : Destino("admin_panel")
    data object AdminFoto : Destino("admin_foto")
    data object AdminContrasena : Destino("admin_contrasena")
    data class NivelDetalle(val id: Int) : Destino("nivel/$id")
    data class NovedadDetalle(val id: Int) : Destino("novedad/$id")
    data class NovedadForm(val id: Int = 0) : Destino("novedad_form/$id") // 0 = nueva
}

// Pantallas donde se muestra la barra flotante y qué pestaña queda activa
private fun pestanaDe(ruta: String?): Pestana? = when {
    ruta == null -> null
    ruta == "inicio" -> Pestana.Inicio
    ruta == "galeria" -> Pestana.Galeria
    ruta == "niveles" || ruta.startsWith("nivel/") -> Pestana.Niveles
    ruta == "novedades" || ruta.startsWith("novedad/") -> Pestana.Novedades
    else -> null
}

fun NavHostController.ir(destino: Destino) = navigate(destino.ruta) { launchSingleTop = true }

private fun NavHostController.irAPestana(p: Pestana) = navigate(p.ruta) {
    popUpTo(Destino.Inicio.ruta) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AppNavegacion(destinoPendiente: Destino?, onDestinoAtendido: () -> Unit) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
        ?.replace("{id}", entrada?.arguments?.getInt("id")?.toString() ?: "")
    val pestana = pestanaDe(rutaActual)

    // Abrir lo que pidió una notificación (cuando ya pasó el splash)
    LaunchedEffect(destinoPendiente, rutaActual) {
        if (destinoPendiente != null && rutaActual != null && rutaActual != Destino.Splash.ruta) {
            nav.ir(destinoPendiente)
            onDestinoAtendido()
        }
    }

    if (rutaActual != null && rutaActual != Destino.Splash.ruta) {
        PedirPermisosIniciales(onRedConcedida = {})
    }

    Box(Modifier.fillMaxSize().background(C.Fondo)) {
        NavHost(
            navController = nav,
            startDestination = Destino.Splash.ruta,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(Destino.Splash.ruta) {
                SplashScreen(onListo = {
                    nav.navigate(Destino.Inicio.ruta) { popUpTo(Destino.Splash.ruta) { inclusive = true } }
                })
            }
            composable(Destino.Inicio.ruta) {
                InicioScreen(
                    onVerGaleria = { nav.irAPestana(Pestana.Galeria) },
                    onPerfil = { nav.ir(Destino.Perfil) }
                )
            }
            composable(Destino.Galeria.ruta) {
                GaleriaScreen(onCamara = { nav.ir(Destino.Camara) })
            }
            composable(Destino.Camara.ruta) {
                CamaraScreen(onCerrar = { nav.popBackStack() })
            }
            composable(Destino.Niveles.ruta) {
                NivelesScreen(onNivel = { nav.ir(Destino.NivelDetalle(it)) })
            }
            composable(
                "nivel/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { e ->
                NivelDetalleScreen(
                    id = e.arguments?.getInt("id") ?: 0,
                    onVolver = { nav.popBackStack() },
                    onNivel = { nav.navigate(Destino.NivelDetalle(it).ruta) { popUpTo(Destino.Niveles.ruta) } },
                    onVerNiveles = { nav.popBackStack(Destino.Niveles.ruta, inclusive = false) },
                    onVerGaleria = { nav.irAPestana(Pestana.Galeria) }
                )
            }
            composable(Destino.Novedades.ruta) {
                NovedadesScreen(
                    onNovedad = { nav.ir(Destino.NovedadDetalle(it)) },
                    onNueva = { nav.ir(Destino.NovedadForm()) }
                )
            }
            composable(
                "novedad/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { e ->
                NovedadDetalleScreen(
                    id = e.arguments?.getInt("id") ?: 0,
                    onVolver = { if (!nav.popBackStack()) nav.irAPestana(Pestana.Novedades) },
                    onEditar = { nav.ir(Destino.NovedadForm(it)) }
                )
            }
            composable(
                "novedad_form/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { e ->
                NovedadFormScreen(
                    id = e.arguments?.getInt("id") ?: 0,
                    onListo = { nav.popBackStack() }
                )
            }
            composable(Destino.Perfil.ruta) {
                PerfilScreen(
                    onVolver = { nav.popBackStack() },
                    onAdmin = { nav.ir(Destino.AdminLogin) },
                    onPanel = { nav.ir(Destino.AdminPanel) }
                )
            }

            // ---------- Administración ----------
            composable(Destino.AdminLogin.ruta) {
                AdminLoginScreen(
                    onListo = {
                        nav.navigate(Destino.AdminPanel.ruta) {
                            popUpTo(Destino.AdminLogin.ruta) { inclusive = true }
                        }
                    },
                    onVolver = { nav.popBackStack() }
                )
            }
            composable(Destino.AdminFoto.ruta) {
                AdminFotoScreen(onListo = { nav.popBackStack() })
            }
            composable(Destino.AdminContrasena.ruta) {
                AdminContrasenaScreen(
                    onListo = { mensaje ->
                        nav.previousBackStackEntry?.savedStateHandle?.set("aviso", mensaje)
                        nav.popBackStack()
                    },
                    onVolver = { nav.popBackStack() }
                )
            }
            composable(Destino.AdminPanel.ruta) { entrada ->
                val aviso by entrada.savedStateHandle.getStateFlow<String?>("aviso", null).collectAsState()
                AdminPanelScreen(
                    avisoExterno = aviso,
                    onAvisoMostrado = { entrada.savedStateHandle["aviso"] = null },
                    onContrasena = { nav.ir(Destino.AdminContrasena) },
                    onVolver = { nav.popBackStack() },
                    onPublicar = { nav.ir(Destino.NovedadForm()) },
                    onSubirFoto = { nav.ir(Destino.AdminFoto) },
                    onEditar = { nav.ir(Destino.NovedadForm(it)) },
                    onVer = { nav.ir(Destino.NovedadDetalle(it)) },
                    onSalir = {
                        nav.navigate(Destino.AdminLogin.ruta) {
                            popUpTo(Destino.AdminPanel.ruta) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Velo bajo la barra de estado para que el contenido al hacer scroll no choque con el reloj
        if (rutaActual != Destino.Splash.ruta) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Brush.verticalGradient(listOf(C.Fondo.copy(alpha = 0.85f), C.Fondo.copy(alpha = 0.4f))))
            )
        }

        AnimatedVisibility(
            visible = pestana != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            BarraNavegacion(activa = pestana, onClick = { nav.irAPestana(it) })
        }
    }
}
