package com.akm.carencia.notificaciones

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.akm.carencia.CarenciaApp
import com.akm.carencia.data.Resultado
import java.util.concurrent.TimeUnit

/**
 * Revisa en segundo plano GET api/Novedad/ultima. Si el admin publicó algo
 * más nuevo que lo último que vio este teléfono, lanza una notificación.
 * Funciona aunque la app esté cerrada (WorkManager, mínimo cada 15 min).
 */
class NovedadesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CarenciaApp
        val ultima = when (val r = app.repo.ultimaNovedad()) {
            is Resultado.Ok -> r.data
            is Resultado.Error -> return if (r.codigo == null) Result.retry() else Result.success()
        }

        val vista = app.sesion.ultimaNovedadVista()
        if (vista == 0) {
            // Primera ejecución: sólo recordamos la más reciente para no avisar de las viejas
            app.sesion.marcarNovedadVista(ultima.pkNovedad)
        } else if (ultima.pkNovedad > vista) {
            Notificaciones.novedadPublicada(applicationContext, ultima.pkNovedad, ultima.titulo, ultima.resumen)
            app.sesion.marcarNovedadVista(ultima.pkNovedad)
        }
        return Result.success()
    }

    companion object {
        private const val PERIODICO = "novedades_periodico"
        private const val AHORA = "novedades_ahora"

        private val conRed = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun programar(context: Context) {
            val req = PeriodicWorkRequestBuilder<NovedadesWorker>(15, TimeUnit.MINUTES)
                .setConstraints(conRed)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODICO, ExistingPeriodicWorkPolicy.KEEP, req)
        }

        // Para la demo: revisa en este momento (botón en Perfil y tras publicar)
        fun revisarAhora(context: Context) {
            val req = OneTimeWorkRequestBuilder<NovedadesWorker>().setConstraints(conRed).build()
            WorkManager.getInstance(context).enqueueUniqueWork(AHORA, ExistingWorkPolicy.REPLACE, req)
        }
    }
}
