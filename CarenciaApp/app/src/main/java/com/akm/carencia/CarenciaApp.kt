package com.akm.carencia

import android.app.Application
import com.akm.carencia.data.FotosLocales
import com.akm.carencia.data.Repositorio
import com.akm.carencia.data.SesionManager
import com.akm.carencia.notificaciones.Notificaciones
import com.akm.carencia.notificaciones.NovedadesWorker
import kotlinx.coroutines.runBlocking

class CarenciaApp : Application() {

    lateinit var sesion: SesionManager
        private set
    lateinit var repo: Repositorio
        private set
    lateinit var fotos: FotosLocales // fotos del espejo guardadas en el teléfono
        private set

    override fun onCreate() {
        super.onCreate()
        instancia = this
        sesion = SesionManager(this)
        runBlocking { sesion.cargar() } // token en memoria antes de la primera petición
        repo = Repositorio(sesion)
        fotos = FotosLocales(this)

        Notificaciones.crearCanales(this)
        NovedadesWorker.programar(this) // revisa anuncios nuevos del admin cada 15 min
    }

    companion object {
        lateinit var instancia: CarenciaApp
            private set
    }
}
