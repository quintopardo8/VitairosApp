package com.vitairos.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reportes")
data class ReporteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tipoReporte: String,
    val procesoAsociado: String,
    val fechaHora: String,
    val usuario: String,
    val areaFaena: String = "",
    val turno: String = "",
    val observaciones: String = "",
    val rutaArchivoLocal: String,
    val estado: String = "Borrador",
    val intentos: Int = 0
)