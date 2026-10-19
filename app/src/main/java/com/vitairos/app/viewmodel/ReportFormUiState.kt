package com.vitairos.app.viewmodel

data class ReportFormUiState(
    val tipoReporte: String = "",
    val procesoAsociado: String = "",
    val areaFaena: String = "",
    val turno: String = "",
    val observaciones: String = "",
    val adjuntos: List<String> = emptyList(),
    val confirmaLegibilidad: Boolean = false,
    val nombreUsuario: String = "",
    val editandoId: String? = null,

    val errorTipo: String? = null,
    val errorProceso: String? = null,
    val errorArea: String? = null,
    val errorTurno: String? = null,
    val errorObservaciones: String? = null,
    val errorAdjunto: String? = null,
    val errorConfirmacion: String? = null,

    val estaGuardando: Boolean = false,
    val guardadoExitoso: Boolean = false
) {
    /** Valor que se guarda en la columna rutaArchivoLocal de Room. */
    val rutaArchivoLocal: String
        get() = adjuntos.joinToString("|")

    val hayErrores: Boolean
        get() = listOf(
            errorTipo, errorProceso, errorArea, errorTurno,
            errorObservaciones, errorAdjunto, errorConfirmacion
        ).any { it != null }
}
