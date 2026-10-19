package com.vitairos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitairos.app.data.network.ConexionObserver
import com.vitairos.app.repository.ReporteRepository

class ReportViewModelFactory(
    private val repository: ReporteRepository,
    private val conexion: ConexionObserver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ReportViewModel(repository, conexion) as T
}
