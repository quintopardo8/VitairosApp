package com.vitairos.app.repository

import com.vitairos.app.data.local.ReporteDao
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.data.remote.ElevationApiService
import com.vitairos.app.data.remote.GeoJsonPointDto
import com.vitairos.app.data.remote.ReporteRemoteDto
import com.vitairos.app.data.remote.RetrofitClientProvider
import com.vitairos.app.data.remote.VitairosApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReporteSyncManager(
    private val reporteDao: ReporteDao,
    private val apiService: VitairosApiService = RetrofitClientProvider.crearVitairosService(),
    private val elevationService: ElevationApiService = RetrofitClientProvider.elevationService
) {

    suspend fun sincronizarReporte(reporte: ReporteEntity): Result<ReporteEntity> {
        return try {
            var reporteActual = reporte.copy(
                intentos = reporte.intentos + 1,
                fechaUltimoIntento = ahoraTexto()
            )
            reporteDao.actualizarReporte(reporteActual)

            if (reporteActual.latitud != null && reporteActual.longitud != null && reporteActual.altitud == null) {
                val elevResp = elevationService.obtenerAltitud(
                    latitude = reporteActual.latitud,
                    longitude = reporteActual.longitud
                )
                if (elevResp.isSuccessful) {
                    val altitudObtenida = elevResp.body()?.elevation?.firstOrNull()
                    if (altitudObtenida != null) {
                        reporteActual = reporteActual.copy(altitud = altitudObtenida)
                        reporteDao.actualizarReporte(reporteActual)
                    }
                }
            }

            val dto = reporteActual.toRemoteDto()

            val response = if (reporteActual.estado == "Sincronizado") {
                apiService.actualizarReporte(reporteActual.id, dto)
            } else {
                apiService.crearReporte(dto)
            }

            if (response.isSuccessful) {
                val sincronizado = reporteActual.copy(
                    estado = "Sincronizado",
                    fechaSincronizacion = ahoraTexto(),
                    ultimoError = ""
                )
                reporteDao.actualizarReporte(sincronizado)
                Result.success(sincronizado)
            } else {
                val fallo = reporteActual.copy(
                    estado = "Pendiente",
                    ultimoError = "HTTP ${response.code()}: ${response.message()}"
                )
                reporteDao.actualizarReporte(fallo)
                Result.failure(Exception(fallo.ultimoError))
            }
        } catch (e: Exception) {
            val pendiente = reporte.copy(
                estado = "Pendiente",
                fechaUltimoIntento = ahoraTexto(),
                ultimoError = e.localizedMessage ?: "Sin conexión"
            )
            reporteDao.actualizarReporte(pendiente)
            Result.failure(e)
        }
    }

    private fun ReporteEntity.toRemoteDto(): ReporteRemoteDto {
        val puntoGeoJson = if (longitud != null && latitud != null) {
            GeoJsonPointDto(
                type = "Point",
                coordinates = listOf(longitud, latitud)
            )
        } else {
            null
        }

        return ReporteRemoteDto(
            id = id,
            tipoReporte = tipoReporte,
            procesoAsociado = procesoAsociado,
            fechaHora = fechaHora,
            usuario = usuario,
            areaFaena = areaFaena,
            turno = turno,
            observaciones = observaciones,
            rutaArchivoLocal = rutaArchivoLocal,
            estado = "Sincronizado",
            ubicacion = puntoGeoJson,
            altitud = altitud
        )
    }

    private fun ahoraTexto(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}