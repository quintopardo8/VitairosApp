package com.vitairos.app.repository

import com.vitairos.app.data.local.ReporteDao
import com.vitairos.app.data.model.ReporteEntity
import kotlinx.coroutines.flow.Flow

class ReporteRepository(private val reporteDao: ReporteDao) {

    // Listado de reportes
    val todosLosReportes: Flow<List<ReporteEntity>> = reporteDao.obtenerTodosLosReportes()

    // Reportes filtrados
    fun obtenerReportesPorEstado(estado: String): Flow<List<ReporteEntity>> {
        return reporteDao.obtenerReportesPorEstado(estado)
    }

    // Guardar reporte
    suspend fun guardarReporte(reporte: ReporteEntity) {
        reporteDao.insertarReporte(reporte)
    }

    // Actualizar
    suspend fun actualizarReporte(reporte: ReporteEntity) {
        reporteDao.actualizarReporte(reporte)
    }

    // Eliminar
    suspend fun eliminarReporte(reporte: ReporteEntity) {
        reporteDao.eliminarReporte(reporte)
    }

    // Buscar x id
    suspend fun obtenerReportePorId(id: String): ReporteEntity? {
        return reporteDao.obtenerReportePorId(id)
    }
}