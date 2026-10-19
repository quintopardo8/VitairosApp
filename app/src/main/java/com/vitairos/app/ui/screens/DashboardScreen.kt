package com.vitairos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.ui.theme.*
import com.vitairos.app.viewmodel.ReportViewModel

enum class DashboardTab(val titulo: String, val icono: ImageVector) {
    RESUMEN("Inicio", Icons.Default.Dashboard),
    COLA("Cola", Icons.Default.List),
    HISTORIAL("Historial", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReportViewModel,
    onNavigateToNewReport: () -> Unit,
    onLogout: () -> Unit
) {
    var pestañaActual by remember { mutableStateOf(DashboardTab.RESUMEN) }

    val listaReportes by viewModel.listaReportes.collectAsStateWithLifecycle(initialValue = emptyList())
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    val operadorActual by viewModel.usuarioActivo.collectAsStateWithLifecycle()

    val reportesEnCola = listaReportes.filter { it.estado == "En cola" || it.estado == "Enviando" }
    val reportesSincronizados = listaReportes.filter { it.estado == "Sincronizado" }
    val reportesConError = listaReportes.filter { it.estado == "Error" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VITAIROS Industrial AI",
                            color = VitairosTextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = operadorActual,
                            color = VitairosTealPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sincronizarCola() },
                        enabled = !sincronizando && (reportesEnCola.isNotEmpty() || reportesConError.isNotEmpty())
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
                                tint = if (reportesEnCola.isNotEmpty() || reportesConError.isNotEmpty()) VitairosTealPrimary else VitairosTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = VitairosTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VitairosSurfaceDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VitairosSurfaceDark
            ) {
                DashboardTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = pestañaActual == tab,
                        onClick = { pestañaActual = tab },
                        icon = { Icon(tab.icono, contentDescription = tab.titulo) },
                        label = { Text(tab.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VitairosDarkBackground,
                            selectedTextColor = VitairosTealPrimary,
                            indicatorColor = VitairosTealPrimary,
                            unselectedIconColor = VitairosTextSecondary,
                            unselectedTextColor = VitairosTextSecondary
                        ))

                    }
                }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores)
            ) {
                when (pestañaActual) {
                    DashboardTab.RESUMEN -> VistaResumenDashboard(
                        enColaCount = reportesEnCola.size,
                        sincronizadosCount = reportesSincronizados.size,
                        errorCount = reportesConError.size,
                        onCrearReporte = onNavigateToNewReport,
                        onSincronizar = { viewModel.sincronizarCola() },
                        sincronizando = sincronizando
                    )
                    DashboardTab.COLA -> VistaCola(
                        reportes = reportesEnCola + reportesConError,
                        onEliminar = { viewModel.eliminarReporte(it) }
                    )
                    DashboardTab.HISTORIAL -> VistaHistorial(
                        reportes = reportesSincronizados
                    )
                }
            }
        }
}

@Composable
fun VistaResumenDashboard(
    enColaCount: Int,
    sincronizadosCount: Int,
    errorCount: Int,
    onCrearReporte: () -> Unit,
    onSincronizar: () -> Unit,
    sincronizando: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Centro de Operaciones Terreno",
            style = MaterialTheme.typography.titleLarge,
            color = VitairosTextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaMetrica(
                titulo = "En Cola",
                valor = enColaCount.toString(),
                color = StatusQueued,
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Sincronizados",
                valor = sincronizadosCount.toString(),
                color = StatusSynced,
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Errores",
                valor = errorCount.toString(),
                color = StatusError,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCrearReporte,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitairosTealPrimary,
                contentColor = VitairosDarkBackground
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Nuevo Reporte")
        }

        OutlinedButton(
            onClick = onSincronizar,
            enabled = !sincronizando && (enColaCount > 0 || errorCount > 0),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
        ) {
            if (sincronizando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VitairosTealPrimary)
            } else {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizar Cola Ahora")
            }
        }
    }
}

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, style = MaterialTheme.typography.headlineMedium, color = color)
            Text(text = titulo, style = MaterialTheme.typography.bodySmall, color = VitairosTextSecondary)
        }
    }
}

@Composable
fun VistaCola(
    reportes: List<ReporteEntity>,
    onEliminar: (ReporteEntity) -> Unit
) {
    if (reportes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay reportes pendientes en la cola.", color = VitairosTextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(reportes, key = { it.id }) { reporte ->
                TarjetaReporteLocal(
                    reporte = reporte,
                    permiteEliminar = true,
                    onEliminar = { onEliminar(reporte) }
                )
            }
        }
    }
}

@Composable
fun VistaHistorial(reportes: List<ReporteEntity>) {
    if (reportes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay reportes sincronizados en el historial.", color = VitairosTextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(reportes, key = { it.id }) { reporte ->
                TarjetaReporteLocal(
                    reporte = reporte,
                    permiteEliminar = false,
                    onEliminar = {}
                )
            }
        }
    }
}

@Composable
fun TarjetaReporteLocal(
    reporte: ReporteEntity,
    permiteEliminar: Boolean,
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

                if (permiteEliminar && reporte.estado != "Sincronizado" && reporte.estado != "Enviando") {
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