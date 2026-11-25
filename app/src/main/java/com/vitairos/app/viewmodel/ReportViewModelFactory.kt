package com.vitairos.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitairos.app.data.network.ConexionObserver
import com.vitairos.app.repository.ReporteRepository
import com.vitairos.app.util.LocationHelper

class ReportViewModelFactory(
    private val repository: ReporteRepository,
    private val conexion: ConexionObserver,
    context: Context
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ReportViewModel(
            repository = repository,
            conexion = conexion,
            obtenerCoordenadas = { LocationHelper.obtenerCoordenadasConAltitud(appContext) }
        ) as T
}