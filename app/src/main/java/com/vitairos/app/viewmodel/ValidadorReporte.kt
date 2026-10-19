package com.vitairos.app.viewmodel

object ValidadorReporte {

    const val TIPO_TURNO = "Reporte de Turno"
    const val TIPO_MANTENCION = "Mantención Preventiva"
    const val TIPO_SEGURIDAD = "Inspección de Seguridad"
    const val TIPO_FALLA = "Falla Operacional"

    const val MAX_OBSERVACIONES = 300
    const val MIN_OBSERVACIONES_FALLA = 10

    fun tipo(valor: String): String? =
        if (valor.isBlank()) "Debe seleccionar un tipo de reporte" else null

    fun proceso(valor: String): String? =
        if (valor.isBlank()) "Debe seleccionar un proceso operacional" else null

    fun area(tipo: String, valor: String): String? =
        if (tipo == TIPO_SEGURIDAD && valor.isBlank())
            "El área es obligatoria en una inspección de seguridad" else null

    fun turno(tipo: String, valor: String): String? =
        if (tipo == TIPO_TURNO && valor.isBlank())
            "El turno es obligatorio en un reporte de turno" else null

    fun observaciones(tipo: String, valor: String): String? = when {
        valor.length > MAX_OBSERVACIONES -> "Máximo $MAX_OBSERVACIONES caracteres"
        tipo == TIPO_FALLA && valor.trim().length < MIN_OBSERVACIONES_FALLA ->
            "Describa la falla (mínimo $MIN_OBSERVACIONES_FALLA caracteres)"
        else -> null
    }

    fun adjuntos(lista: List<String>): String? =
        if (lista.isEmpty()) "Debe adjuntar al menos un documento" else null

    fun confirmacion(confirmado: Boolean): String? =
        if (!confirmado) "Debe confirmar que el documento es legible" else null
}
