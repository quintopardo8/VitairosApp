package com.vitairos.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class CoordenadasActuales(
    val latitud: Double,
    val longitud: Double
)

object LocationHelper {

    fun tienePermisoUbicacion(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerCoordenadas(context: Context): CoordenadasActuales? {
        if (!tienePermisoUbicacion(context)) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(
                        CoordenadasActuales(
                            latitud = location.latitude,
                            longitud = location.longitude
                        )
                    )
                } else {
                    fusedClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                cont.resume(
                                    CoordenadasActuales(
                                        latitud = lastLoc.latitude,
                                        longitud = lastLoc.longitude
                                    )
                                )
                            } else {
                                cont.resume(null)
                            }
                        }
                        .addOnFailureListener { cont.resume(null) }
                }
            }.addOnFailureListener {
                cont.resume(null)
            }

            cont.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}