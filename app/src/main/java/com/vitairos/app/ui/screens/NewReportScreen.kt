package com.vitairos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.ui.theme.*
import com.vitairos.app.viewmodel.ReportViewModel

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

            // Selector tipo reporte
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

            // Selector proceso
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

            // Area o faena
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

            // Turno
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

            // Observaciones
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

            // Evidencia documental (Ruta de archivo)
            Card(
                colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Evidencia Adjunta *",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitairosTextPrimary
                    )

                    if (state.rutaArchivoLocal.isNotBlank()) {
                        Text(
                            text = "Archivo seleccionado: ${state.rutaArchivoLocal.takeLast(30)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitairosTealPrimary
                        )
                    } else {
                        Text(
                            text = "No se ha adjuntado ninguna evidencia obligatoria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitairosTextSecondary
                        )
                    }

                    // Botón simular selección de archivo
                    OutlinedButton(
                        onClick = {
                            viewModel.onArchivoSeleccionado("content://vitairos/evidencia_terreno_${System.currentTimeMillis()}.jpg")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adjuntar Evidencia (Simulada)")
                    }

                    state.errorAdjunto?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón guardar reporte en cola
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