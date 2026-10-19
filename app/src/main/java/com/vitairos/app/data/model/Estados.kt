package com.vitairos.app.data.model
object Estados {
    const val BORRADOR = "Borrador"
    const val EN_COLA = "En cola"
    const val ENVIANDO = "Enviando"
    const val SINCRONIZADO = "Sincronizado"
    const val ERROR = "Error"
}

val ReporteEntity.rutasAdjuntos: List<String>
    get() = rutaArchivoLocal.split("|").filter { it.isNotBlank() }
