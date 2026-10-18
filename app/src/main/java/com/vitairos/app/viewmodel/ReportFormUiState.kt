package com.vitairos.app.viewmodel

data class ReportFormUiState(

    val tipoReporte: String = "",
    val procesoAsociado: String = "",
    val areaFaena: String = "",
    val turno: String = "",
    val observaciones: String = "",
    val rutaArchivoLocal: String = "",
    val nombreUsuario: String = "Operador Terreno 01",


    val errorTipo: String? = null,
    val errorProceso: String? = null,
    val errorAdjunto: String? = null,

    val estaGuardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeErrorGeneral: String? = null
) {

    val esFormularioValido: Boolean
        get() = tipoReporte.isNotBlank() &&
                procesoAsociado.isNotBlank() &&
                rutaArchivoLocal.isNotBlank() &&
                errorTipo == null &&
                errorProceso == null &&
                errorAdjunto == null
}