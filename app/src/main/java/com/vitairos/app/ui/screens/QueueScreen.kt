package com.vitairos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.ui.theme.*
import com.vitairos.app.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: ReportViewModel,
    onNavigateToNewReport: () -> Unit
) {
    val listaReportes by viewModel.listaReportes.collectAsStateWithLifecycle(initialValue = emptyList())
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    val operadorActual by viewModel.usuarioActivo.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cola e Historial", color = VitairosTextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(operadorActual, color = VitairosTealPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sincronizarCola() },
                        enabled = !sincronizando && listaReportes.any { it.estado == "En cola" }
                    ) {
                        if (sincronizando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = VitairosTealPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar",
                                tint = if (listaReportes.any { it.estado == "En cola" }) VitairosTealPrimary else VitairosTextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VitairosSurfaceDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewReport,
                containerColor = VitairosTealPrimary,
                contentColor = VitairosDarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
            }
        },
        containerColor = VitairosDarkBackground
    ) { paddingValores ->
        if (listaReportes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay reportes registrados en la base local.",
                    color = VitairosTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(listaReportes, key = { it.id }) { reporte ->
                    ItemReporteCard(
                        reporte = reporte,
                        onEliminar = { viewModel.eliminarReporte(reporte) }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemReporteCard(
    reporte: ReporteEntity,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val colorBadge = when (reporte.estado) {
        "Sincronizado" -> StatusSynced
        "Enviando" -> StatusSending
        "Error" -> StatusError
        "En cola" -> StatusQueued
        else -> StatusDraft
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar reporte", color = VitairosTextPrimary) },
            text = { Text("¿Desea eliminar este reporte en cola? Esta acción no se puede deshacer.", color = VitairosTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onEliminar()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = VitairosTextPrimary)
                }
            },
            containerColor = VitairosSurfaceDark
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reporte.tipoReporte,
                    style = MaterialTheme.typography.titleMedium,
                    color = VitairosTextPrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorBadge.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reporte.estado,
                        color = colorBadge,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Text(
                text = "Proceso: ${reporte.procesoAsociado}",
                style = MaterialTheme.typography.bodyMedium,
                color = VitairosTextSecondary
            )

            Text(
                text = "Operador: ${reporte.usuario}",
                style = MaterialTheme.typography.bodySmall,
                color = VitairosTextSecondary
            )

            if (reporte.areaFaena.isNotBlank()) {
                Text(
                    text = "Área: ${reporte.areaFaena}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitairosTextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reporte.fechaHora,
                    style = MaterialTheme.typography.bodySmall,
                    color = VitairosTextSecondary
                )

                if (reporte.estado != "Sincronizado" && reporte.estado != "Enviando") {
                    TextButton(
                        onClick = { mostrarConfirmacion = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}