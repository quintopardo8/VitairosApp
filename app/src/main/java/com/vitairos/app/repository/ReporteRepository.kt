package com.vitairos.app.repository

import com.vitairos.app.data.local.ReporteDao
import com.vitairos.app.data.model.ReporteEntity
import kotlinx.coroutines.flow.Flow

class ReporteRepository(private val reporteDao: ReporteDao) {

    val todosLosReportes: Flow<List<ReporteEntity>> = reporteDao.obtenerTodosLosReportes()

    suspend fun obtenerReportesPorEstado(estado: String): List<ReporteEntity> {
        return reporteDao.obtenerReportesPorEstado(estado)
    }

    suspend fun guardarReporte(reporte: ReporteEntity) {
        reporteDao.insertarReporte(reporte)
    }

    suspend fun actualizarReporte(reporte: ReporteEntity) {
        reporteDao.actualizarReporte(reporte)
    }

    suspend fun eliminarReporte(reporte: ReporteEntity) {
        reporteDao.eliminarReporte(reporte)
    }

    suspend fun obtenerReportePorId(id: String): ReporteEntity? {
        return reporteDao.obtenerReportePorId(id)
    }
}