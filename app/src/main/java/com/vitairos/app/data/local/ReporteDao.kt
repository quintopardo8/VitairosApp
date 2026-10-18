package com.vitairos.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vitairos.app.data.model.ReporteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReporte(reporte: ReporteEntity)

    @Update
    suspend fun actualizarReporte(reporte: ReporteEntity)

    @Delete
    suspend fun eliminarReporte(reporte: ReporteEntity)

    @Query("SELECT * FROM reportes ORDER BY fechaHora DESC")
    fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE estado = :estado ORDER BY fechaHora DESC")
    fun obtenerReportesPorEstado(estado: String): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE id = :id LIMIT 1")
    suspend fun obtenerReportePorId(id: String): ReporteEntity?
}