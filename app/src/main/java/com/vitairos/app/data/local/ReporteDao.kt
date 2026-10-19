package com.vitairos.app.data.local

import androidx.room.*
import com.vitairos.app.data.model.ReporteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes ORDER BY fechaHora DESC")
    fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE estado = :estado")
    suspend fun obtenerReportesPorEstado(estado: String): List<ReporteEntity>

    @Query("SELECT * FROM reportes WHERE id = :id LIMIT 1")
    suspend fun obtenerReportePorId(id: String): ReporteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReporte(reporte: ReporteEntity)

    @Update
    suspend fun actualizarReporte(reporte: ReporteEntity)

    @Delete
    suspend fun eliminarReporte(reporte: ReporteEntity)
}