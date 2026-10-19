package com.vitairos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitairos.app.data.model.Estados
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.data.model.rutasAdjuntos
import com.vitairos.app.ui.theme.StatusDraft
import com.vitairos.app.ui.theme.StatusError
import com.vitairos.app.ui.theme.StatusQueued
import com.vitairos.app.ui.theme.StatusSending
import com.vitairos.app.ui.theme.StatusSynced
import com.vitairos.app.ui.theme.VitairosSurfaceDark
import com.vitairos.app.ui.theme.VitairosTextPrimary
import com.vitairos.app.ui.theme.VitairosTextSecondary
import com.vitairos.app.viewmodel.ReportViewModel
import java.io.File
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween

fun colorEstado(estado: String): Color = when (estado) {
    Estados.SINCRONIZADO -> StatusSynced
    Estados.ENVIANDO -> StatusSending
    Estados.ERROR -> StatusError
    Estados.EN_COLA -> StatusQueued
    else -> StatusDraft
}

@Composable
fun BadgeEstado(estado: String) {
    val color by animateColorAsState(
        targetValue = colorEstado(estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstado"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = estado, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TextoError(mensaje: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = StatusError,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = mensaje, color = StatusError, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RecogerMensajes(viewModel: ReportViewModel, host: SnackbarHostState) {
    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { host.showSnackbar(it) }
    }
}

@Composable
fun TarjetaReporte(
    reporte: ReporteEntity,
    permiteEliminar: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    val adjuntos = reporte.rutasAdjuntos

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar reporte", color = VitairosTextPrimary) },
            text = {
                Text(
                    "¿Desea eliminar este reporte? Esta acción no se puede deshacer.",
                    color = VitairosTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    onEliminar()
                }) { Text("Eliminar", color = StatusError) }
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
        onClick = onClick,
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
                    text = reporte.tipoReporte.ifBlank { "(sin tipo)" },
                    style = MaterialTheme.typography.titleMedium,
                    color = VitairosTextPrimary
                )
                BadgeEstado(reporte.estado)
            }

            Text(
                text = "ID: ${reporte.id.take(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = VitairosTextSecondary
            )
            Text(
                text = "Proceso: ${reporte.procesoAsociado.ifBlank { "-" }}",
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

            val referencia = if (adjuntos.isEmpty()) "sin adjuntos"
            else "${adjuntos.size} · ${File(adjuntos.first()).name.takeLast(18)}"
            Text(
                text = "Adjuntos: $referencia",
                style = MaterialTheme.typography.bodySmall,
                color = VitairosTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${reporte.fechaHora} · intentos: ${reporte.intentos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitairosTextSecondary
                )
                if (permiteEliminar &&
                    reporte.estado != Estados.SINCRONIZADO &&
                    reporte.estado != Estados.ENVIANDO
                ) {
                    TextButton(
                        onClick = { mostrarConfirmacion = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusError)
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}
