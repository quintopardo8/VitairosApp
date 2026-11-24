package com.vitairos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitairos.app.data.remote.RetrofitClientProvider
import com.vitairos.app.data.remote.TrabajadorDto
import com.vitairos.app.data.remote.VitairosApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrabajadorUiState(
    val trabajadores: List<TrabajadorDto> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class TrabajadorViewModel(
    private val apiService: VitairosApiService = RetrofitClientProvider.crearTrabajadoresService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrabajadorUiState())
    val uiState: StateFlow<TrabajadorUiState> = _uiState.asStateFlow()

    init {
        cargarTrabajadores()
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }

    fun cargarTrabajadores() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val resp = apiService.obtenerTrabajadores()
                if (resp.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            trabajadores = resp.body().orEmpty(),
                            cargando = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Error al cargar (${resp.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.localizedMessage ?: "Sin conexión con el servicio de trabajadores"
                    )
                }
            }
        }
    }

    fun crearTrabajador(nombre: String, cargo: String, areaAsignada: String) {
        val nombreLimpio = nombre.trim()
        val cargoLimpio = cargo.trim()
        if (nombreLimpio.isEmpty() || cargoLimpio.isEmpty()) {
            _uiState.update { it.copy(error = "Nombre y cargo son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val dto = TrabajadorDto(
                    id = null,
                    nombre = nombreLimpio,
                    cargo = cargoLimpio,
                    areaAsignada = areaAsignada.trim()
                )
                val resp = apiService.crearTrabajador(dto)
                if (resp.isSuccessful) {
                    _uiState.update { it.copy(mensaje = "Trabajador creado") }
                    cargarTrabajadores()
                } else {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudo crear (${resp.code()})")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al crear")
                }
            }
        }
    }

    fun actualizarTrabajador(id: Long, nombre: String, cargo: String, areaAsignada: String) {
        val nombreLimpio = nombre.trim()
        val cargoLimpio = cargo.trim()
        if (nombreLimpio.isEmpty() || cargoLimpio.isEmpty()) {
            _uiState.update { it.copy(error = "Nombre y cargo son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val dto = TrabajadorDto(
                    id = id,
                    nombre = nombreLimpio,
                    cargo = cargoLimpio,
                    areaAsignada = areaAsignada.trim()
                )
                val resp = apiService.actualizarTrabajador(id, dto)
                if (resp.isSuccessful) {
                    _uiState.update { it.copy(mensaje = "Trabajador actualizado") }
                    cargarTrabajadores()
                } else {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudo actualizar (${resp.code()})")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al actualizar")
                }
            }
        }
    }

    fun eliminarTrabajador(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val resp = apiService.eliminarTrabajador(id)
                if (resp.isSuccessful) {
                    _uiState.update { it.copy(mensaje = "Trabajador eliminado") }
                    cargarTrabajadores()
                } else {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudo eliminar (${resp.code()})")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al eliminar")
                }
            }
        }
    }
}