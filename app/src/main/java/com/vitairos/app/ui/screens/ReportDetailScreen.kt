package com.vitairos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vitairos.app.data.model.Estados
import com.vitairos.app.data.model.rutasAdjuntos
import com.vitairos.app.ui.components.BadgeEstado
import com.vitairos.app.ui.components.RecogerMensajes
import com.vitairos.app.ui.theme.StatusError
import com.vitairos.app.ui.theme.VitairosDarkBackground
import com.vitairos.app.ui.theme.VitairosSurfaceDark
import com.vitairos.app.ui.theme.VitairosSurfaceVariant
import com.vitairos.app.ui.theme.VitairosTealPrimary
import com.vitairos.app.ui.theme.VitairosTextPrimary
import com.vitairos.app.ui.theme.VitairosTextSecondary
import com.vitairos.app.viewmodel.ReportViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ReportViewModel,
    reporteId: String,
    onNavigateBack: () -> Unit,
    onEditar: (String) -> Unit
) {
    val lista by viewModel.listaReportes.collectAsStateWithLifecycle(initialValue = emptyList())
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    val reporte = lista.firstOrNull { it.id == reporteId }

    val snackbarHost = remember { SnackbarHostState() }
    RecogerMensajes(viewModel, snackbarHost)

    var confirmarEliminar by remember { mutableStateOf(false) }

    if (confirmarEliminar && reporte != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar reporte", color = VitairosTextPrimary) },
            text = {
                Text(
                    "¿Desea eliminar este reporte? Esta acción no se puede deshacer.",
                    color = VitairosTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminarReporte(reporte)
                    onNavigateBack()
                }) { Text("Eliminar", color = StatusError) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text("Cancelar", color = VitairosTextPrimary)
                }
            },
            containerColor = VitairosSurfaceDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte", color = VitairosTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        containerColor = VitairosDarkBackground
    ) { padding ->
        if (reporte == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Reporte no encontrado", color = VitairosTextSecondary)
            }
            return@Scaffold
        }

        val editable = reporte.estado == Estados.BORRADOR ||
                reporte.estado == Estados.EN_COLA ||
                reporte.estado == Estados.ERROR

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reporte.tipoReporte.ifBlank { "(sin tipo)" },
                    style = MaterialTheme.typography.titleLarge,
                    color = VitairosTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                BadgeEstado(reporte.estado)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilaDato("ID", reporte.id)
                    FilaDato("Proceso", reporte.procesoAsociado.ifBlank { "-" })
                    FilaDato("Fecha y hora", reporte.fechaHora)
                    FilaDato("Operador", reporte.usuario)
                    FilaDato("Área / Faena", reporte.areaFaena.ifBlank { "-" })
                    FilaDato("Turno", reporte.turno.ifBlank { "-" })
                    FilaDato("Observaciones", reporte.observaciones.ifBlank { "-" })
                    FilaDato("Intentos de envío", reporte.intentos.toString())
                    FilaDato("Último intento de envío", reporte.fechaUltimoIntento.ifBlank { "-" })
                    FilaDato("Sincronizado el", reporte.fechaSincronizacion.ifBlank { "-" })
                    if (reporte.ultimoError.isNotBlank()) FilaDato("Último error", reporte.ultimoError)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val adjuntos = reporte.rutasAdjuntos
                    Text(
                        text = "Adjuntos (${adjuntos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitairosTextPrimary
                    )
                    if (adjuntos.isEmpty()) {
                        Text("Sin adjuntos", color = VitairosTextSecondary)
                    }
                    adjuntos.forEach { ruta ->
                        if (ruta.endsWith(".pdf", ignoreCase = true)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = VitairosTealPrimary
                                )
                                Text(
                                    text = File(ruta).name.takeLast(30),
                                    color = VitairosTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            AsyncImage(
                                model = File(ruta),
                                contentDescription = "Documento adjunto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VitairosSurfaceVariant)
                            )
                        }
                    }
                }
            }

            when (reporte.estado) {
                Estados.SINCRONIZADO -> Text(
                    text = "Reporte sincronizado: queda como registro de trazabilidad y no puede editarse ni eliminarse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitairosTextSecondary
                )

                Estados.ENVIANDO -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VitairosTealPrimary,
                        strokeWidth = 2.dp
                    )
                    Text("Envío en curso...", color = VitairosTextSecondary)
                }

                else -> Unit
            }

            if (reporte.estado == Estados.BORRADOR) {
                Button(
                    onClick = { viewModel.enviarBorradorACola(reporte) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitairosTealPrimary,
                        contentColor = VitairosDarkBackground
                    )
                ) { Text("Enviar a la cola") }
            }

            if (reporte.estado == Estados.EN_COLA || reporte.estado == Estados.ERROR) {
                Button(
                    onClick = { viewModel.reintentarReporte(reporte) },
                    enabled = !sincronizando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitairosTealPrimary,
                        contentColor = VitairosDarkBackground
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (reporte.estado == Estados.ERROR) "Reintentar envío" else "Enviar ahora")
                }
            }

            if (editable) {
                OutlinedButton(
                    onClick = { onEditar(reporte.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { confirmarEliminar = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = VitairosTealPrimary
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = VitairosTextPrimary
        )
    }
}
