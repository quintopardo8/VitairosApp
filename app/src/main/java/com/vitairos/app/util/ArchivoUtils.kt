package com.vitairos.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
object ArchivoUtils {

    const val MAX_BYTES = 10L * 1024 * 1024
    const val MAX_ADJUNTOS = 5
    private val TIPOS_PERMITIDOS = setOf("image/jpeg", "image/png", "application/pdf")

    sealed class Resultado {
        data class Ok(val ruta: String) : Resultado()
        data class Error(val mensaje: String) : Resultado()
    }
    suspend fun importar(context: Context, uri: Uri): Resultado = withContext(Dispatchers.IO) {
        val mime = context.contentResolver.getType(uri)
        if (mime == null || mime !in TIPOS_PERMITIDOS) {
            return@withContext Resultado.Error("Formato no permitido. Use JPG, PNG o PDF")
        }

        val extension = when (mime) {
            "image/png" -> "png"
            "application/pdf" -> "pdf"
            else -> "jpg"
        }
        val carpeta = File(context.filesDir, "adjuntos").apply { mkdirs() }
        val destino = File(carpeta, "${UUID.randomUUID()}.$extension")

        try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: return@withContext Resultado.Error("No se pudo leer el archivo")
            entrada.use { input -> destino.outputStream().use { salida -> input.copyTo(salida) } }
        } catch (e: Exception) {
            destino.delete()
            return@withContext Resultado.Error("No se pudo leer el archivo")
        }

        validarLocal(destino)
    }
    fun validarLocal(archivo: File): Resultado = when {
        archivo.length() == 0L -> {
            archivo.delete()
            Resultado.Error("El archivo está vacío")
        }
        archivo.length() > MAX_BYTES -> {
            archivo.delete()
            Resultado.Error("El archivo supera el máximo de 10 MB")
        }
        else -> Resultado.Ok(archivo.absolutePath)
    }
}
