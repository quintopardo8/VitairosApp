package com.vitairos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.data.model.Estados
import com.vitairos.app.data.model.ReporteEntity
import com.vitairos.app.ui.components.RecogerMensajes
import com.vitairos.app.ui.components.TarjetaReporte
import com.vitairos.app.ui.theme.StatusError
import com.vitairos.app.ui.theme.StatusQueued
import com.vitairos.app.ui.theme.StatusSynced
import com.vitairos.app.ui.theme.VitairosDarkBackground
import com.vitairos.app.ui.theme.VitairosSurfaceDark
import com.vitairos.app.ui.theme.VitairosTealPrimary
import com.vitairos.app.ui.theme.VitairosTextPrimary
import com.vitairos.app.ui.theme.VitairosTextSecondary
import com.vitairos.app.viewmodel.ReportViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.vitairos.app.R

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
    onOpenDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    var pestañaActual by remember { mutableStateOf(DashboardTab.RESUMEN) }

    val listaReportes by viewModel.listaReportes.collectAsStateWithLifecycle(initialValue = emptyList())
    val sincronizando by viewModel.sincronizando.collectAsStateWithLifecycle()
    val operadorActual by viewModel.usuarioActivo.collectAsStateWithLifecycle()
    val hayConexion by viewModel.hayConexion.collectAsStateWithLifecycle()
    val sinConexionSimulada by viewModel.sinConexionSimulada.collectAsStateWithLifecycle()

    val snackbarHost = remember { SnackbarHostState() }
    RecogerMensajes(viewModel, snackbarHost)

    val reportesEnCola = listaReportes.filter {
        it.estado == Estados.EN_COLA || it.estado == Estados.ENVIANDO
    }
    val reportesSincronizados = listaReportes.filter { it.estado == Estados.SINCRONIZADO }
    val reportesConError = listaReportes.filter { it.estado == Estados.ERROR }
    val reportesPendientes = listaReportes.filter { it.estado != Estados.SINCRONIZADO }
    val hayParaSincronizar = listaReportes.any {
        it.estado == Estados.EN_COLA || it.estado == Estados.ERROR
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                    }
                        Image(
                            painter = painterResource(id = R.drawable.vitairos),
                            contentDescription = "Logo VITAIROS",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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
                    if (!hayConexion) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Sin conexión",
                            tint = StatusQueued,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.sincronizarCola() },
                        enabled = !sincronizando && hayParaSincronizar
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
                                tint = if (hayParaSincronizar) VitairosTealPrimary else VitairosTextSecondary
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VitairosSurfaceDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = VitairosSurfaceDark) {
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
                        )
                    )
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
        snackbarHost = { SnackbarHost(snackbarHost) },
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
                    hayConexion = hayConexion,
                    sinConexionSimulada = sinConexionSimulada,
                    sincronizando = sincronizando,
                    hayParaSincronizar = hayParaSincronizar,
                    onCrearReporte = onNavigateToNewReport,
                    onSincronizar = { viewModel.sincronizarCola() },
                    onAlternarConexion = { viewModel.alternarConexionSimulada() }
                )

                DashboardTab.COLA -> VistaListaReportes(
                    reportes = reportesPendientes,
                    mensajeVacio = "No hay reportes pendientes en la cola.",
                    permiteEliminar = true,
                    onOpenDetail = onOpenDetail,
                    onEliminar = { viewModel.eliminarReporte(it) }
                )

                DashboardTab.HISTORIAL -> VistaListaReportes(
                    reportes = listaReportes,
                    mensajeVacio = "Aún no hay reportes creados.",
                    permiteEliminar = false,
                    onOpenDetail = onOpenDetail,
                    onEliminar = {}
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
    hayConexion: Boolean,
    sinConexionSimulada: Boolean,
    sincronizando: Boolean,
    hayParaSincronizar: Boolean,
    onCrearReporte: () -> Unit,
    onSincronizar: () -> Unit,
    onAlternarConexion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            TarjetaMetrica("En Cola", enColaCount.toString(), StatusQueued, Modifier.weight(1f))
            TarjetaMetrica("Sincronizados", sincronizadosCount.toString(), StatusSynced, Modifier.weight(1f))
            TarjetaMetrica("Errores", errorCount.toString(), StatusError, Modifier.weight(1f))
        }


        Card(
            colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simular sin conexión",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitairosTextPrimary
                    )
                    Text(
                        text = if (hayConexion)
                            "Conectado: los envíos llegan a la API mock."
                        else
                            "Sin conexión: los reportes quedan en cola y se reintenta (5, 15 y 30 s).",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hayConexion) VitairosTextSecondary else StatusQueued
                    )
                }
                Switch(
                    checked = sinConexionSimulada,
                    onCheckedChange = { onAlternarConexion() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VitairosDarkBackground,
                        checkedTrackColor = StatusQueued
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

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
            enabled = !sincronizando && hayParaSincronizar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VitairosTealPrimary)
        ) {
            if (sincronizando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = VitairosTealPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizar / Reintentar Ahora")
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
fun VistaListaReportes(
    reportes: List<ReporteEntity>,
    mensajeVacio: String,
    permiteEliminar: Boolean,
    onOpenDetail: (String) -> Unit,
    onEliminar: (ReporteEntity) -> Unit
) {
    if (reportes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(mensajeVacio, color = VitairosTextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            items(reportes, key = { it.id }) { reporte ->
                TarjetaReporte(
                    reporte = reporte,
                    permiteEliminar = permiteEliminar,
                    onClick = { onOpenDetail(reporte.id) },
                    onEliminar = { onEliminar(reporte) }
                )
            }
        }
    }
}
