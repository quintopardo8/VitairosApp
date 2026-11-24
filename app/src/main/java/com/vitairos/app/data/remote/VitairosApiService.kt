package com.vitairos.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ElevationApiService {
    @GET("v1/elevation")
    suspend fun obtenerAltitud(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ElevationResponseDto>
}

interface VitairosApiService {
    //microservicio trabajadores
    @GET("api/trabajadores")
    suspend fun obtenerTrabajadores(): Response<List<TrabajadorDto>>

    @GET("api/trabajadores/{id}")
    suspend fun obtenerTrabajadorPorId(@Path("id") id: Long): Response<TrabajadorDto>

    @POST("api/trabajadores")
    suspend fun crearTrabajador(@Body trabajador: TrabajadorDto): Response<TrabajadorDto>

    @PUT("api/trabajadores/{id}")
    suspend fun actualizarTrabajador(
        @Path("id") id: Long,
        @Body trabajador: TrabajadorDto
    ): Response<TrabajadorDto>

    @DELETE("api/trabajadores/{id}")
    suspend fun eliminarTrabajador(@Path("id") id: Long): Response<Unit>

    //microservicio reportes
    @GET("api/reportes")
    suspend fun obtenerReportes(): Response<List<ReporteRemoteDto>>

    @POST("api/reportes")
    suspend fun crearReporte(@Body reporte: ReporteRemoteDto): Response<ReporteRemoteDto>

    @PUT("api/reportes/{id}")
    suspend fun actualizarReporte(
        @Path("id") id: String,
        @Body reporte: ReporteRemoteDto
    ): Response<ReporteRemoteDto>

    @DELETE("api/reportes/{id}")
    suspend fun eliminarReporte(@Path("id") id: String): Response<Unit>
}

object RetrofitClientProvider {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val elevationService: ElevationApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ElevationApiService::class.java)
    }

    fun crearTrabajadoresService(
        baseUrl: String = "http://127.0.0.1:8081/"
    ): VitairosApiService = crearVitairosService(baseUrl)

    fun crearReportesService(
        baseUrl: String = "http://127.0.0.1:8082/"
    ): VitairosApiService = crearVitairosService(baseUrl)

    fun crearVitairosService(
        baseUrl: String = "http://127.0.0.1:8082/"
    ): VitairosApiService {
        val normalizada = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizada)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VitairosApiService::class.java)
    }
}