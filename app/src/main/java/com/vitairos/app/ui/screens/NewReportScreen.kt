package com.vitairos.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vitairos.app.ui.components.RecogerMensajes
import com.vitairos.app.ui.components.TextoError
import com.vitairos.app.ui.theme.StatusError
import com.vitairos.app.ui.theme.VitairosDarkBackground
import com.vitairos.app.ui.theme.VitairosSurfaceDark
import com.vitairos.app.ui.theme.VitairosSurfaceVariant
import com.vitairos.app.ui.theme.VitairosTealPrimary
import com.vitairos.app.ui.theme.VitairosTextPrimary
import com.vitairos.app.ui.theme.VitairosTextSecondary
import com.vitairos.app.util.ArchivoUtils
import com.vitairos.app.viewmodel.ReportViewModel
import com.vitairos.app.viewmodel.ValidadorReporte
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    viewModel: ReportViewModel,
    reporteId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var rutaCamara by rememberSaveable { mutableStateOf<String?>(null) }
    var rutaPrevia by remember { mutableStateOf<String?>(null) }

    RecogerMensajes(viewModel, snackbarHost)

    LaunchedEffect(reporteId) {
        if (reporteId != null && state.editandoId != reporteId) {
            viewModel.cargarParaEdicion(reporteId)
        }
    }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.reiniciarFormulario()
            onNavigateBack()
        }
    }

    fun salir() {
        viewModel.cancelarFormulario()
        onNavigateBack()
    }
    BackHandler { salir() }


    fun importar(uri: Uri) {
        scope.launch {
            when (val resultado = ArchivoUtils.importar(context, uri)) {
                is ArchivoUtils.Resultado.Ok -> viewModel.agregarAdjunto(resultado.ruta)
                is ArchivoUtils.Resultado.Error -> viewModel.mostrarErrorAdjunto(resultado.mensaje)
            }
        }
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { capturaExitosa ->
        val ruta = rutaCamara
        rutaCamara = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (capturaExitosa) {
                when (val resultado = ArchivoUtils.validarLocal(archivo)) {
                    is ArchivoUtils.Resultado.Ok -> viewModel.agregarAdjunto(resultado.ruta)
                    is ArchivoUtils.Resultado.Error -> viewModel.mostrarErrorAdjunto(resultado.mensaje)
                }
            } else {
                archivo.delete()   // el usuario canceló la captura
            }
        }
    }

    fun lanzarCamara() {
        val archivo = File(context.filesDir, "evidencia_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "com.vitairos.app.fileprovider", archivo)
        rutaCamara = archivo.absolutePath
        camaraLauncher.launch(uri)
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzarCamara()
        } else {
            scope.launch {
                val respuesta = snackbarHost.showSnackbar(
                    message = "Se necesita el permiso de cámara para capturar evidencia",
                    actionLabel = "Ajustes",
                    duration = SnackbarDuration.Long
                )
                if (respuesta == SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            }
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { importar(it) } }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { importar(it) } }

    rutaPrevia?.let { ruta ->
        AlertDialog(
            onDismissRequest = { rutaPrevia = null },
            title = { Text("Vista previa", color = VitairosTextPrimary) },
            text = {
                AsyncImage(
                    model = File(ruta),
                    contentDescription = "Vista previa ampliada del documento",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            },
            confirmButton = {
                TextButton(onClick = { rutaPrevia = null }) {
                    Text("Cerrar", color = VitairosTealPrimary)
                }
            },
            containerColor = VitairosSurfaceDark
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.editandoId != null) "Editar Reporte" else "Nuevo Reporte Operacional",
                        color = VitairosTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { salir() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = VitairosTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VitairosSurfaceDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },

        bottomBar = {
            Surface(color = VitairosSurfaceDark, tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.guardar(comoBorrador = true) },
                        enabled = !state.estaGuardando,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                    ) {
                        Text("Guardar borrador")
                    }

                    Button(
                        onClick = { viewModel.guardar(comoBorrador = false) },
                        enabled = !state.estaGuardando,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VitairosTealPrimary,
                            contentColor = VitairosDarkBackground,
                            disabledContainerColor = VitairosSurfaceVariant,
                            disabledContentColor = VitairosTextSecondary
                        )
                    ) {
                        if (state.estaGuardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = VitairosDarkBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar en cola")
                        }
                    }
                }
            }
        },
        containerColor = VitairosDarkBackground
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CampoSelector(
                label = "Tipo de Reporte *",
                valor = state.tipoReporte,
                opciones = viewModel.tiposDeReporte,
                error = state.errorTipo,
                onSeleccion = viewModel::onTipoReporteCambiado
            )

            CampoSelector(
                label = "Proceso Operacional *",
                valor = state.procesoAsociado,
                opciones = viewModel.procesosAsociados,
                error = state.errorProceso,
                onSeleccion = viewModel::onProcesoCambiado
            )

            CampoTexto(
                label = if (state.tipoReporte == ValidadorReporte.TIPO_SEGURIDAD)
                    "Área / Sector de Faena *" else "Área / Sector de Faena",
                valor = state.areaFaena,
                placeholder = "Ej: Planta Concentradora Sector B",
                error = state.errorArea,
                onCambio = viewModel::onAreaFaenaCambiada
            )

            CampoTexto(
                label = if (state.tipoReporte == ValidadorReporte.TIPO_TURNO)
                    "Turno Operacional *" else "Turno Operacional",
                valor = state.turno,
                placeholder = "Ej: Turno 7x7 Día",
                error = state.errorTurno,
                onCambio = viewModel::onTurnoCambiado
            )

            CampoTexto(
                label = if (state.tipoReporte == ValidadorReporte.TIPO_FALLA)
                    "Observaciones / Notas de Terreno *" else "Observaciones / Notas de Terreno",
                valor = state.observaciones,
                placeholder = "Describa novedades o anomalías observadas...",
                error = state.errorObservaciones,
                minLines = 3,
                maxLines = 5,
                contador = "${state.observaciones.length}/${ValidadorReporte.MAX_OBSERVACIONES}",
                onCambio = viewModel::onObservacionesCambiadas
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Documentos adjuntos * (${state.adjuntos.size}/${ArchivoUtils.MAX_ADJUNTOS})",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitairosTextPrimary
                    )

                    if (state.adjuntos.isEmpty()) {
                        Text(
                            text = "Capture una fotografía o adjunte una imagen (JPG/PNG) o un PDF de hasta 10 MB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitairosTextSecondary
                        )
                    } else {
                        state.adjuntos.forEach { ruta ->
                            FilaAdjunto(
                                ruta = ruta,
                                onVer = { rutaPrevia = ruta },
                                onQuitar = { viewModel.quitarAdjunto(ruta) }
                            )
                        }
                    }

                    val puedeAgregar = state.adjuntos.size < ArchivoUtils.MAX_ADJUNTOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val tienePermiso = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (tienePermiso) lanzarCamara()
                                else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                            },
                            enabled = puedeAgregar,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cámara")
                        }

                        OutlinedButton(
                            onClick = { galeriaLauncher.launch("image/*") },
                            enabled = puedeAgregar,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería")
                        }

                        OutlinedButton(
                            onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                            enabled = puedeAgregar,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                    }

                    state.errorAdjunto?.let { TextoError(it) }

                    // Confirmación de calidad básica (vista previa + confirmación del usuario)
                    if (state.adjuntos.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = state.confirmaLegibilidad,
                                onCheckedChange = viewModel::onConfirmaLegibilidadCambiada,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VitairosTealPrimary,
                                    checkmarkColor = VitairosDarkBackground,
                                    uncheckedColor = VitairosTextSecondary
                                )
                            )
                            Text(
                                text = "Confirmo que los documentos son legibles, están bien orientados y corresponden a este reporte",
                                style = MaterialTheme.typography.bodySmall,
                                color = VitairosTextPrimary
                            )
                        }
                        state.errorConfirmacion?.let { TextoError(it) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun colorsCampo() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = VitairosTextPrimary,
    unfocusedTextColor = VitairosTextPrimary,
    focusedBorderColor = VitairosTealPrimary,
    unfocusedBorderColor = VitairosSurfaceVariant,
    focusedLabelColor = VitairosTealPrimary,
    unfocusedLabelColor = VitairosTextSecondary,
    cursorColor = VitairosTealPrimary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoSelector(
    label: String,
    valor: String,
    opciones: List<String>,
    error: String?,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = if (error != null) {
                { TextoError(error) }
            } else null,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = colorsCampo()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier.background(VitairosSurfaceDark)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = VitairosTextPrimary) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    placeholder: String,
    error: String?,
    onCambio: (String) -> Unit,
    minLines: Int = 1,
    maxLines: Int = 1,
    contador: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = error != null,
        trailingIcon = if (error != null) {
            {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Campo con error",
                    tint = StatusError
                )
            }
        } else null,
        supportingText = when {
            error != null -> {
                { TextoError(error) }
            }
            contador != null -> {
                { Text(contador, color = VitairosTextSecondary) }
            }
            else -> null
        },
        minLines = minLines,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        colors = colorsCampo()
    )
}

@Composable
private fun FilaAdjunto(
    ruta: String,
    onVer: () -> Unit,
    onQuitar: () -> Unit
) {
    val archivo = remember(ruta) { File(ruta) }
    val esPdf = ruta.endsWith(".pdf", ignoreCase = true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VitairosDarkBackground)
                .clickable(enabled = !esPdf, onClick = onVer),
            contentAlignment = Alignment.Center
        ) {
            if (esPdf) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Documento PDF",
                    tint = VitairosTealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                AsyncImage(
                    model = archivo,
                    contentDescription = "Miniatura del documento adjunto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = archivo.name.takeLast(24),
                style = MaterialTheme.typography.bodyMedium,
                color = VitairosTextPrimary
            )
            Text(
                text = "${archivo.length() / 1024} KB · " + if (esPdf) "PDF" else "Imagen (toque para ampliar)",
                style = MaterialTheme.typography.bodySmall,
                color = VitairosTealPrimary
            )
        }

        IconButton(onClick = onQuitar) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar adjunto",
                tint = StatusError
            )
        }
    }
}
