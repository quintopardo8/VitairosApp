package com.vitairos.app

import com.vitairos.app.viewmodel.ValidadorReporte
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorReporteTest {

    @Test
    fun tipoValido_noRetornaError() {
        assertNull(ValidadorReporte.tipo(ValidadorReporte.TIPO_TURNO))
    }

    @Test
    fun tipoVacio_retornaError() {
        assertNotNull(ValidadorReporte.tipo(""))
    }

    @Test
    fun procesoValido_noRetornaError() {
        assertNull(ValidadorReporte.proceso("Chancado y Molienda"))
    }

    @Test
    fun procesoVacio_retornaError() {
        assertNotNull(ValidadorReporte.proceso("   "))
    }

    @Test
    fun confirmacionEnFalse_retornaError() {
        assertNotNull(ValidadorReporte.confirmacion(false))
        assertNull(ValidadorReporte.confirmacion(true))
    }
}