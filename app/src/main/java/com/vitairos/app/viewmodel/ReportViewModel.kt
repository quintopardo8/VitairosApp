package com.vitairos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.repository.ReporteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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

    val usuariosDisponibles = listOf(
        "Carlos Mendoza - Op. Faena A",
        "Ana Soto - Op. Faena B",
        "Jorge Valenzuela - Mantención Mecánica",
        "Marcela Gómez - Prevención de Riesgos"
    )

    private val _usuarioActivo = MutableStateFlow(usuariosDisponibles.first())
    val usuarioActivo: StateFlow<String> = _usuarioActivo.asStateFlow()

    fun seleccionarUsuario(usuario: String) {
        _usuarioActivo.value = usuario
        _uiState.update { it.copy(nombreUsuario = usuario) }
    }

    val listaReportes: Flow<List<ReporteEntity>> = repository.todosLosReportes

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
        _uiState.update {
            it.copy(
                tipoReporte = nuevoTipo,
                errorTipo = if (nuevoTipo.isBlank()) "Debe seleccionar un tipo de reporte" else null
            )
        }
    }

    fun onProcesoCambiado(nuevoProceso: String) {
        _uiState.update {
            it.copy(
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
        _uiState.update {
            it.copy(
                rutaArchivoLocal = rutaUri,
                errorAdjunto = if (rutaUri.isBlank()) "Debe adjuntar al menos una evidencia documental" else null
            )
        }
    }

    fun guardarReporte() {
        val estado = _uiState.value
        if (!estado.esFormularioValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(estaGuardando = true) }

            val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fechaActual = formatoFecha.format(Date())

            val nuevoReporte = ReporteEntity(
                tipoReporte = estado.tipoReporte,
                procesoAsociado = estado.procesoAsociado,
                fechaHora = fechaActual,
                usuario = _usuarioActivo.value,
                areaFaena = estado.areaFaena,
                turno = estado.turno,
                observaciones = estado.observaciones,
                rutaArchivoLocal = estado.rutaArchivoLocal,
                estado = "En cola",
                intentos = 0
            )

            repository.guardarReporte(nuevoReporte)

            _uiState.update {
                it.copy(
                    estaGuardando = false,
                    guardadoExitoso = true
                )
            }
        }
    }

    fun reiniciarFormulario() {
        _uiState.value = ReportFormUiState(nombreUsuario = _usuarioActivo.value)
    }

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    fun sincronizarCola() {
        viewModelScope.launch {
            _sincronizando.value = true

            val pendientes = repository.obtenerReportesPorEstado("En cola")

            pendientes.forEach { reporte ->
                repository.actualizarReporte(reporte.copy(estado = "Enviando"))
                delay(1200)

                repository.actualizarReporte(
                    reporte.copy(
                        estado = "Sincronizado",
                        intentos = reporte.intentos + 1
                    )
                )
            }

            _sincronizando.value = false
        }
    }

    fun eliminarReporte(reporte: ReporteEntity) {
        if (reporte.estado != "Sincronizado") {
            viewModelScope.launch {
                repository.eliminarReporte(reporte)
            }
        }
    }
}