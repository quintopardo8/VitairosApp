package com.vitairos.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.ui.theme.*
import com.vitairos.app.viewmodel.ReportViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var expandirTipo by remember { mutableStateOf(false) }
    var expandirProceso by remember { mutableStateOf(false) }

    var uriFotoTemporal by remember { mutableStateOf<Uri?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { capturaExitosa ->
        if (capturaExitosa && uriFotoTemporal != null) {
            viewModel.onArchivoSeleccionado(uriFotoTemporal.toString())
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val archivo = File(context.filesDir, "evidencia_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "com.vitairos.app.fileprovider",
                archivo
            )
            uriFotoTemporal = uri
            camaraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Se necesita el permiso de cámara para adjuntar evidencia", Toast.LENGTH_LONG).show()
        }
    }



    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uriSeleccionada ->
        uriSeleccionada?.let {
            viewModel.onArchivoSeleccionado(it.toString())
        }
    }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            Toast.makeText(context, "Reporte guardado en cola localmente", Toast.LENGTH_SHORT).show()
            viewModel.reiniciarFormulario()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reporte Operacional", color = VitairosTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = VitairosTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VitairosSurfaceDark
                )
            )
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
            Spacer(modifier = Modifier.height(8.dp))

            // selector tipo reporte
            ExposedDropdownMenuBox(
                expanded = expandirTipo,
                onExpandedChange = { expandirTipo = !expandirTipo }
            ) {
                OutlinedTextField(
                    value = state.tipoReporte,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Reporte *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirTipo) },
                    isError = state.errorTipo != null,
                    supportingText = {
                        state.errorTipo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VitairosTextPrimary,
                        unfocusedTextColor = VitairosTextPrimary,
                        focusedBorderColor = VitairosTealPrimary,
                        unfocusedBorderColor = VitairosSurfaceVariant
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandirTipo,
                    onDismissRequest = { expandirTipo = false },
                    modifier = Modifier.background(VitairosSurfaceDark)
                ) {
                    viewModel.tiposDeReporte.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = VitairosTextPrimary) },
                            onClick = {
                                viewModel.onTipoReporteCambiado(opcion)
                                expandirTipo = false
                            }
                        )
                    }
                }
            }

            // selector proceso
            ExposedDropdownMenuBox(
                expanded = expandirProceso,
                onExpandedChange = { expandirProceso = !expandirProceso }
            ) {
                OutlinedTextField(
                    value = state.procesoAsociado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Proceso Operacional *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirProceso) },
                    isError = state.errorProceso != null,
                    supportingText = {
                        state.errorProceso?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VitairosTextPrimary,
                        unfocusedTextColor = VitairosTextPrimary,
                        focusedBorderColor = VitairosTealPrimary,
                        unfocusedBorderColor = VitairosSurfaceVariant
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandirProceso,
                    onDismissRequest = { expandirProceso = false },
                    modifier = Modifier.background(VitairosSurfaceDark)
                ) {
                    viewModel.procesosAsociados.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = VitairosTextPrimary) },
                            onClick = {
                                viewModel.onProcesoCambiado(opcion)
                                expandirProceso = false
                            }
                        )
                    }
                }
            }

            // area o faena
            OutlinedTextField(
                value = state.areaFaena,
                onValueChange = { viewModel.onAreaFaenaCambiada(it) },
                label = { Text("Área / Sector de Faena") },
                placeholder = { Text("Ej: Planta Concentradora Sector B") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VitairosTextPrimary,
                    unfocusedTextColor = VitairosTextPrimary,
                    focusedBorderColor = VitairosTealPrimary,
                    unfocusedBorderColor = VitairosSurfaceVariant
                )
            )

            // turno
            OutlinedTextField(
                value = state.turno,
                onValueChange = { viewModel.onTurnoCambiado(it) },
                label = { Text("Turno Operacional") },
                placeholder = { Text("Ej: Turno 7x7 Día") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VitairosTextPrimary,
                    unfocusedTextColor = VitairosTextPrimary,
                    focusedBorderColor = VitairosTealPrimary,
                    unfocusedBorderColor = VitairosSurfaceVariant
                )
            )

            // observaciones
            OutlinedTextField(
                value = state.observaciones,
                onValueChange = { viewModel.onObservacionesCambiadas(it) },
                label = { Text("Observaciones / Notas de Terreno") },
                placeholder = { Text("Describa novedades o anomalías observadas...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VitairosTextPrimary,
                    unfocusedTextColor = VitairosTextPrimary,
                    focusedBorderColor = VitairosTealPrimary,
                    unfocusedBorderColor = VitairosSurfaceVariant
                )
            )

            // evidencia documental
            Card(
                colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Evidencia Fotográfica *",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitairosTextPrimary
                    )

                    if (state.rutaArchivoLocal.isNotBlank()) {
                        Text(
                            text = "Adjunto: ${state.rutaArchivoLocal.takeLast(35)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitairosTealPrimary
                        )
                    } else {
                        Text(
                            text = "Adjunte al menos una fotografía o archivo de respaldo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitairosTextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // botón cámara
                        OutlinedButton(
                            onClick = {
                                val tienePermiso = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (tienePermiso) {
                                    val archivo = File(context.filesDir, "evidencia_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "com.vitairos.app.fileprovider",
                                        archivo
                                    )
                                    uriFotoTemporal = uri
                                    camaraLauncher.launch(uri)
                                } else {
                                    permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cámara")
                        }

                        // botón galería
                        OutlinedButton(
                            onClick = {
                                galeriaLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }

                    state.errorAdjunto?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // botón guardar en cola local
            Button(
                onClick = { viewModel.guardarReporte() },
                enabled = state.esFormularioValido && !state.estaGuardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitairosTealPrimary,
                    contentColor = VitairosDarkBackground,
                    disabledContainerColor = VitairosSurfaceVariant,
                    disabledContentColor = VitairosTextSecondary
                )
            ) {
                if (state.estaGuardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = VitairosDarkBackground
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar en Cola Local")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}