package com.vitairos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.repository.ReporteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val repository: ReporteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFormUiState())
    val uiState: StateFlow<ReportFormUiState> = _uiState.asStateFlow()

    val tiposDeReporte = listOf(
        "Reporte de Turno",
        "Mantención Preventiva",
        "Inspección de Seguridad",
        "Falla Operacional"
    )

    val procesosAsociados = listOf(
        "Chancado y Molienda",
        "Transporte de Material",
        "Perforación y Tronadura",
        "Mantenimiento General"
    )

    fun onTipoReporteCambiado(nuevoTipo: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                tipoReporte = nuevoTipo,
                errorTipo = if (nuevoTipo.isBlank()) "Debe seleccionar un tipo de reporte" else null
            )
        }
    }

    fun onProcesoCambiado(nuevoProceso: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                procesoAsociado = nuevoProceso,
                errorProceso = if (nuevoProceso.isBlank()) "Debe seleccionar un proceso operacional" else null
            )
        }
    }

    fun onAreaFaenaCambiada(nuevaArea: String) {
        _uiState.update { it.copy(areaFaena = nuevaArea) }
    }

    fun onTurnoCambiado(nuevoTurno: String) {
        _uiState.update { it.copy(turno = nuevoTurno) }
    }

    fun onObservacionesCambiadas(nuevasObs: String) {
        _uiState.update { it.copy(observaciones = nuevasObs) }
    }

    fun onArchivoSeleccionado(rutaUri: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                rutaArchivoLocal = rutaUri,
                errorAdjunto = if (rutaUri.isBlank()) "Debe adjuntar al menos una evidencia documental" else null
            )
        }
    }

    // guardado en room
    fun guardarReporte() {
        val estado = _uiState.value

        // validacion antes de
        if (!estado.esFormularioValido) {
            _uiState.update {
                it.copy(
                    errorTipo = if (it.tipoReporte.isBlank()) "Campo requerido" else null,
                    errorProceso = if (it.procesoAsociado.isBlank()) "Campo requerido" else null,
                    errorAdjunto = if (it.rutaArchivoLocal.isBlank()) "Adjunto requerido" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(estaGuardando = true) }

            // formateo fecha
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fechaActual = formatoFecha.format(Date())

            // crear la entidad
            val nuevoReporte = ReporteEntity(
                tipoReporte = estado.tipoReporte,
                procesoAsociado = estado.procesoAsociado,
                fechaHora = fechaActual,
                usuario = estado.nombreUsuario,
                areaFaena = estado.areaFaena,
                turno = estado.turno,
                observaciones = estado.observaciones,
                rutaArchivoLocal = estado.rutaArchivoLocal,
                estado = "En cola",
                intentos = 0
            )

            repository.guardarReporte(nuevoReporte)

            // ACTUalizar estado para feedback visual
            _uiState.update {
                it.copy(
                    estaGuardando = false,
                    guardadoExitoso = true
                )
            }
        }
    }


    fun reiniciarFormulario() {
        _uiState.value = ReportFormUiState()
    }

    //flujo desde el repositorio
    val listaReportes: kotlinx.coroutines.flow.Flow<List<ReporteEntity>> = repository.todosLosReportes

    //botón de sincronización
    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    //SIMULACION CON REINTENTOS PARA MVP
    fun sincronizarCola() {
        viewModelScope.launch {
            _sincronizando.value = true

            // buscando reportes que necesitan envío
            val pendientes = repository.obtenerReportesPorEstado("En cola")

            // retraso 2 seg simulado
            kotlinx.coroutines.delay(2000)

            // marcados como sincronizados localmente en Room
            // En caso real se llama a API REST
            _sincronizando.value = false
        }
    }
}