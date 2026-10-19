package com.vitairos.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vitairos.app.data.model.ReporteEntity

@Database(entities = [ReporteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reporteDao(): ReporteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reportes ADD COLUMN fechaUltimoIntento TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE reportes ADD COLUMN fechaSincronizacion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE reportes ADD COLUMN ultimoError TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vitairos_db"
                ).addMigrations(MIGRACION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}