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
    val nombre: String? = null,
    @SerializedName("cargo")
    val cargo: String? = null,
    @SerializedName("areaAsignada")
    val areaAsignada: String? = ""
)

data class ReporteRemoteDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("tipoReporte")
    val tipoReporte: String? = null,
    @SerializedName("procesoAsociado")
    val procesoAsociado: String? = null,
    @SerializedName("fechaHora")
    val fechaHora: String? = null,
    @SerializedName("usuario")
    val usuario: String? = null,
    @SerializedName("areaFaena")
    val areaFaena: String? = null,
    @SerializedName("turno")
    val turno: String? = null,
    @SerializedName("observaciones")
    val observaciones: String? = null,
    @SerializedName("ubicacion")
    val ubicacion: GeoJsonPointDto? = null,
    @SerializedName("altitud")
    val altitud: Double? = null
)