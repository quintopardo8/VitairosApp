package com.vitairos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitairos.app.data.model.Estados
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.data.model.rutasAdjuntos
import com.vitairos.app.data.network.ConexionObserver
import com.vitairos.app.repository.ReporteRepository
import com.vitairos.app.util.ArchivoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(
    private val repository: ReporteRepository,
    conexion: ConexionObserver
) : ViewModel() {
    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

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

    val tiposDeReporte = listOf(
        ValidadorReporte.TIPO_TURNO,
        ValidadorReporte.TIPO_MANTENCION,
        ValidadorReporte.TIPO_SEGURIDAD,
        ValidadorReporte.TIPO_FALLA
    )

    val procesosAsociados = listOf(
        "Chancado y Molienda",
        "Transporte de Material",
        "Perforación y Tronadura",
        "Mantenimiento General"
    )

    val listaReportes: Flow<List<ReporteEntity>> = repository.todosLosReportes


    private val _uiState = MutableStateFlow(ReportFormUiState(nombreUsuario = _usuarioActivo.value))
    val uiState: StateFlow<ReportFormUiState> = _uiState.asStateFlow()

    private var reporteOriginal: ReporteEntity? = null
    private var adjuntosOriginales: Set<String> = emptySet()

    fun onTipoReporteCambiado(nuevoTipo: String) {
        _uiState.update {
            it.copy(
                tipoReporte = nuevoTipo,
                errorTipo = ValidadorReporte.tipo(nuevoTipo),
                // al cambiar el tipo cambian las reglas condicionales
                errorArea = null,
                errorTurno = null,
                errorObservaciones = null
            )
        }
    }

    fun onProcesoCambiado(nuevoProceso: String) {
        _uiState.update {
            it.copy(
                procesoAsociado = nuevoProceso,
                errorProceso = ValidadorReporte.proceso(nuevoProceso)
            )
        }
    }

    fun onAreaFaenaCambiada(nuevaArea: String) {
        _uiState.update {
            it.copy(areaFaena = nuevaArea, errorArea = ValidadorReporte.area(it.tipoReporte, nuevaArea))
        }
    }

    fun onTurnoCambiado(nuevoTurno: String) {
        _uiState.update {
            it.copy(turno = nuevoTurno, errorTurno = ValidadorReporte.turno(it.tipoReporte, nuevoTurno))
        }
    }

    fun onObservacionesCambiadas(nuevasObs: String) {
        _uiState.update {
            it.copy(observaciones = nuevasObs, errorObservaciones = ValidadorReporte.observaciones("", nuevasObs))
        }
    }

    fun onConfirmaLegibilidadCambiada(confirmado: Boolean) {
        _uiState.update { it.copy(confirmaLegibilidad = confirmado, errorConfirmacion = null) }
    }


    fun agregarAdjunto(ruta: String) {
        _uiState.update {
            if (it.adjuntos.size >= ArchivoUtils.MAX_ADJUNTOS) it
            else it.copy(
                adjuntos = it.adjuntos + ruta,
                errorAdjunto = null,
                confirmaLegibilidad = false
            )
        }
    }

    fun mostrarErrorAdjunto(mensaje: String) {
        _uiState.update { it.copy(errorAdjunto = mensaje) }
    }

    fun quitarAdjunto(ruta: String) {
        _uiState.update {
            val restantes = it.adjuntos - ruta
            it.copy(
                adjuntos = restantes,
                confirmaLegibilidad = if (restantes.isEmpty()) false else it.confirmaLegibilidad
            )
        }
        if (ruta !in adjuntosOriginales) eliminarArchivos(listOf(ruta))
    }


    fun cargarParaEdicion(id: String) {
        viewModelScope.launch {
            val reporte = repository.obtenerReportePorId(id) ?: return@launch
            reporteOriginal = reporte
            val rutas = reporte.rutasAdjuntos
            adjuntosOriginales = rutas.toSet()
            _uiState.value = ReportFormUiState(
                tipoReporte = reporte.tipoReporte,
                procesoAsociado = reporte.procesoAsociado,
                areaFaena = reporte.areaFaena,
                turno = reporte.turno,
                observaciones = reporte.observaciones,
                adjuntos = rutas,
                confirmaLegibilidad = rutas.isNotEmpty(),
                nombreUsuario = reporte.usuario,
                editandoId = reporte.id
            )
        }
    }

    private fun validarFormulario(f: ReportFormUiState, borrador: Boolean): ReportFormUiState =
        if (borrador) {
            f.copy(
                errorTipo = ValidadorReporte.tipo(f.tipoReporte),
                errorProceso = ValidadorReporte.proceso(f.procesoAsociado),
                errorArea = null,
                errorTurno = null,
                errorObservaciones = ValidadorReporte.observaciones("", f.observaciones),
                errorAdjunto = null,
                errorConfirmacion = null
            )
        } else {
            f.copy(
                errorTipo = ValidadorReporte.tipo(f.tipoReporte),
                errorProceso = ValidadorReporte.proceso(f.procesoAsociado),
                errorArea = ValidadorReporte.area(f.tipoReporte, f.areaFaena),
                errorTurno = ValidadorReporte.turno(f.tipoReporte, f.turno),
                errorObservaciones = ValidadorReporte.observaciones(f.tipoReporte, f.observaciones),
                errorAdjunto = ValidadorReporte.adjuntos(f.adjuntos),
                errorConfirmacion =
                    if (f.adjuntos.isEmpty()) null else ValidadorReporte.confirmacion(f.confirmaLegibilidad)
            )
        }

    fun guardar(comoBorrador: Boolean) {
        if (_uiState.value.estaGuardando) return

        val validado = validarFormulario(_uiState.value, comoBorrador)
        _uiState.value = validado
        if (validado.hayErrores) {
            _mensajes.trySend("Revise los campos marcados en rojo")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(estaGuardando = true) }
            val formulario = _uiState.value

            val original = reporteOriginal?.let { repository.obtenerReportePorId(it.id) }
            if (original != null &&
                (original.estado == Estados.ENVIANDO || original.estado == Estados.SINCRONIZADO)
            ) {
                _mensajes.trySend("El reporte ya está en envío y no puede editarse")
                _uiState.update { it.copy(estaGuardando = false, guardadoExitoso = true) }
                return@launch
            }

            val estadoFinal = if (comoBorrador) Estados.BORRADOR else Estados.EN_COLA

            if (original != null) {
                repository.actualizarReporte(
                    original.copy(
                        tipoReporte = formulario.tipoReporte,
                        procesoAsociado = formulario.procesoAsociado,
                        areaFaena = formulario.areaFaena,
                        turno = formulario.turno,
                        observaciones = formulario.observaciones,
                        rutaArchivoLocal = formulario.rutaArchivoLocal,
                        estado = estadoFinal
                    )
                )
                eliminarArchivos(adjuntosOriginales - formulario.adjuntos.toSet())
            } else {
                val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                repository.guardarReporte(
                    ReporteEntity(
                        tipoReporte = formulario.tipoReporte,
                        procesoAsociado = formulario.procesoAsociado,
                        fechaHora = fecha,
                        usuario = _usuarioActivo.value,
                        areaFaena = formulario.areaFaena,
                        turno = formulario.turno,
                        observaciones = formulario.observaciones,
                        rutaArchivoLocal = formulario.rutaArchivoLocal,
                        estado = estadoFinal,
                        intentos = 0
                    )
                )
            }

            _mensajes.trySend(
                if (comoBorrador) "Borrador guardado" else "Reporte guardado en la cola local"
            )
            _uiState.update { it.copy(estaGuardando = false, guardadoExitoso = true) }
        }
    }

    fun cancelarFormulario() {
        val nuevos = _uiState.value.adjuntos.filter { it !in adjuntosOriginales }
        eliminarArchivos(nuevos)
        reiniciarFormulario()
    }

    fun reiniciarFormulario() {
        reporteOriginal = null
        adjuntosOriginales = emptySet()
        _uiState.value = ReportFormUiState(nombreUsuario = _usuarioActivo.value)
    }


    fun eliminarReporte(reporte: ReporteEntity) {
        if (reporte.estado == Estados.SINCRONIZADO || reporte.estado == Estados.ENVIANDO) return
        viewModelScope.launch {
            repository.eliminarReporte(reporte)
            eliminarArchivos(reporte.rutasAdjuntos)
            _mensajes.trySend("Reporte eliminado")
        }
    }

    fun enviarBorradorACola(reporte: ReporteEntity) {
        val problema = validarEntidad(reporte)
        if (problema != null) {
            _mensajes.trySend("Borrador incompleto: $problema")
            return
        }
        viewModelScope.launch {
            repository.actualizarReporte(reporte.copy(estado = Estados.EN_COLA))
            _mensajes.trySend("Reporte enviado a la cola")
        }
    }

    private fun validarEntidad(r: ReporteEntity): String? =
        ValidadorReporte.tipo(r.tipoReporte)
            ?: ValidadorReporte.proceso(r.procesoAsociado)
            ?: ValidadorReporte.area(r.tipoReporte, r.areaFaena)
            ?: ValidadorReporte.turno(r.tipoReporte, r.turno)
            ?: ValidadorReporte.observaciones(r.tipoReporte, r.observaciones)
            ?: ValidadorReporte.adjuntos(r.rutasAdjuntos)

    private fun eliminarArchivos(rutas: Collection<String>) {
        if (rutas.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            rutas.forEach { runCatching { File(it).delete() } }
        }
    }

    private val _redReal = MutableStateFlow(true)
    private val _sinConexionSimulada = MutableStateFlow(false)
    val sinConexionSimulada: StateFlow<Boolean> = _sinConexionSimulada.asStateFlow()

    val hayConexion: StateFlow<Boolean> =
        combine(_redReal, _sinConexionSimulada) { red, simulada -> red && !simulada }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun alternarConexionSimulada() {
        _sinConexionSimulada.value = !_sinConexionSimulada.value
    }

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val esperasReintento = listOf(5_000L, 15_000L, 30_000L)

    fun sincronizarCola() {
        if (_sincronizando.value) return
        _sincronizando.value = true
        viewModelScope.launch {
            try {
                val pendientes = repository.obtenerReportesPorEstado(Estados.EN_COLA) +
                        repository.obtenerReportesPorEstado(Estados.ERROR)
                sincronizar(pendientes)
            } finally {
                _sincronizando.value = false
            }
        }
    }

    fun reintentarReporte(reporte: ReporteEntity) {
        if (_sincronizando.value) return
        _sincronizando.value = true
        viewModelScope.launch {
            try {
                sincronizar(listOf(reporte))
            } finally {
                _sincronizando.value = false
            }
        }
    }

    private suspend fun sincronizar(lista: List<ReporteEntity>) {
        if (lista.isEmpty()) return
        if (!hayConexion.value) {
            _mensajes.trySend("Sin conexión: se reintentará automáticamente (5, 15 y 30 s)")
        }
        val resultados = coroutineScope {
            lista.map { reporte -> async { enviarConReintentos(reporte) } }.awaitAll()
        }
        val enviados = resultados.count { it }
        val fallidos = resultados.size - enviados
        _mensajes.trySend(
            if (fallidos == 0) "Sincronización completa: $enviados reporte(s) enviado(s)"
            else "$enviados enviado(s), $fallidos en Error: use Reintentar"
        )
    }

    private fun ahora(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    private suspend fun enviarConReintentos(inicial: ReporteEntity): Boolean {
        var intentos = inicial.intentos

        for (i in 0..esperasReintento.size) {
            val antes = repository.obtenerReportePorId(inicial.id) ?: return false
            repository.actualizarReporte(
                antes.copy(estado = Estados.ENVIANDO, intentos = intentos, fechaUltimoIntento = ahora())
            )

            delay(1200)
            intentos++
            val respuestaOk = hayConexion.value

            val despues = repository.obtenerReportePorId(inicial.id) ?: return false
            if (respuestaOk) {
                repository.actualizarReporte(
                    despues.copy(
                        estado = Estados.SINCRONIZADO,
                        intentos = intentos,
                        fechaSincronizacion = ahora(),
                        ultimoError = ""
                    )
                )
                return true
            }
            if (i < esperasReintento.size) {
                repository.actualizarReporte(
                    despues.copy(
                        estado = Estados.EN_COLA,
                        intentos = intentos,
                        ultimoError = "Sin conexión con la API"
                    )
                )
                delay(esperasReintento[i])
            }
        }

        val cierre = repository.obtenerReportePorId(inicial.id) ?: return false
        repository.actualizarReporte(
            cierre.copy(
                estado = Estados.ERROR,
                intentos = intentos,
                ultimoError = "Sin conexión: se agotaron los reintentos"
            )
        )
        return false
    }

    init {
        viewModelScope.launch {
            repository.obtenerReportesPorEstado(Estados.ENVIANDO).forEach {
                repository.actualizarReporte(it.copy(estado = Estados.EN_COLA))
            }
        }

        viewModelScope.launch {
            conexion.enLinea.collect { _redReal.value = it }
        }

        viewModelScope.launch {
            var anterior = true
            hayConexion.collect { ahora ->
                if (ahora && !anterior) {
                    _mensajes.trySend("Conexión recuperada: sincronizando...")
                    sincronizarCola()
                }
                anterior = ahora
            }
        }
    }
}
