package com.vitairos.app.data.remote

import com.google.gson.annotations.SerializedName

data class GeoJsonPointDto(
    @SerializedName("type")
    val type: String = "Point",
    @SerializedName("coordinates")
    val coordinates: List<Double>
)

data class ElevationResponseDto(
    @SerializedName("elevation")
    val elevation: List<Double>? = null
)

data class TrabajadorDto(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("cargo")
    val cargo: String,
    @SerializedName("areaAsignada")
    val areaAsignada: String = ""
)

data class ReporteRemoteDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("tipoReporte")
    val tipoReporte: String,
    @SerializedName("procesoAsociado")
    val procesoAsociado: String,
    @SerializedName("fechaHora")
    val fechaHora: String,
    @SerializedName("usuario")
    val usuario: String,
    @SerializedName("areaFaena")
    val areaFaena: String,
    @SerializedName("turno")
    val turno: String,
    @SerializedName("observaciones")
    val observaciones: String,
    @SerializedName("rutaArchivoLocal")
    val rutaArchivoLocal: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("ubicacion")
    val ubicacion: GeoJsonPointDto?,
    @SerializedName("altitud")
    val altitud: Double?
)