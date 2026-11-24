package com.vitairos.app

import com.vitairos.app.data.remote.ElevationResponseDto
import com.vitairos.app.data.remote.ReporteRemoteDto
import com.vitairos.app.data.remote.TrabajadorDto
import com.vitairos.app.data.remote.VitairosApiService
import com.vitairos.app.viewmodel.TrabajadorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TrabajadorViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun crearTrabajadorConNombreVacio_seteaError() = runTest {
        val vm = TrabajadorViewModel(FakeApiService())
        advanceUntilIdle()

        vm.crearTrabajador(nombre = "", cargo = "Operador", areaAsignada = "Planta A")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun crearTrabajadorValido_agregaYRefrescaLista() = runTest {
        val fakeApi = FakeApiService()
        val vm = TrabajadorViewModel(fakeApi)
        advanceUntilIdle()

        vm.crearTrabajador(nombre = "Luis Rojas", cargo = "Geólogo", areaAsignada = "Faena Norte")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.trabajadores.size)
        assertEquals("Luis Rojas", vm.uiState.value.trabajadores.first().nombre)
    }

    private class FakeApiService : VitairosApiService {
        private val items = mutableListOf<TrabajadorDto>()
        private var nextId = 1L

        override suspend fun obtenerTrabajadores(): Response<List<TrabajadorDto>> =
            Response.success(items.toList())

        override suspend fun obtenerTrabajadorPorId(id: Long): Response<TrabajadorDto> {
            val found = items.find { it.id == id }
            return if (found != null) Response.success(found)
            else Response.error(404, "{}".toResponseBody("application/json".toMediaType()))
        }

        override suspend fun crearTrabajador(trabajador: TrabajadorDto): Response<TrabajadorDto> {
            val creado = trabajador.copy(id = nextId++)
            items.add(creado)
            return Response.success(creado)
        }

        override suspend fun actualizarTrabajador(
            id: Long,
            trabajador: TrabajadorDto
        ): Response<TrabajadorDto> {
            val idx = items.indexOfFirst { it.id == id }
            return if (idx >= 0) {
                val actualizado = trabajador.copy(id = id)
                items[idx] = actualizado
                Response.success(actualizado)
            } else {
                Response.error(404, "{}".toResponseBody("application/json".toMediaType()))
            }
        }

        override suspend fun eliminarTrabajador(id: Long): Response<Unit> {
            items.removeAll { it.id == id }
            return Response.success(Unit)
        }

        override suspend fun obtenerReportes(): Response<List<ReporteRemoteDto>> =
            Response.success(emptyList())

        override suspend fun crearReporte(reporte: ReporteRemoteDto): Response<ReporteRemoteDto> =
            Response.success(reporte)

        override suspend fun actualizarReporte(
            id: String,
            reporte: ReporteRemoteDto
        ): Response<ReporteRemoteDto> = Response.success(reporte)

        override suspend fun eliminarReporte(id: String): Response<Unit> = Response.success(Unit)
    }
}