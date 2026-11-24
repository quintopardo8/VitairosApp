package com.vitairos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitairos.app.data.remote.TrabajadorDto
import com.vitairos.app.viewmodel.TrabajadorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajadoresScreen(
    viewModel: TrabajadorViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var trabajadorEditando by remember { mutableStateOf<TrabajadorDto?>(null) }

    LaunchedEffect(state.mensaje, state.error) {
        val texto = state.mensaje ?: state.error
        if (texto != null) {
            snackbarHostState.showSnackbar(texto)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trabajadores") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarTrabajadores() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    trabajadorEditando = null
                    mostrarDialogo = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo trabajador")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.cargando && state.trabajadores.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.trabajadores.isEmpty()) {
                Text(
                    text = "No hay trabajadores registrados",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.trabajadores,
                        key = { it.id ?: it.hashCode() }
                    ) { trabajador ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = trabajador.nombre.orEmpty(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = trabajador.cargo.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!trabajador.areaAsignada.isNullOrBlank()) {
                                        Text(
                                            text = "Área: ${trabajador.areaAsignada}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            trabajadorEditando = trabajador
                                            mostrarDialogo = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    trabajador.id?.let { id ->
                                        IconButton(onClick = { viewModel.eliminarTrabajador(id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoTrabajador(
            inicial = trabajadorEditando,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nombre, cargo, area ->
                val actual = trabajadorEditando
                if (actual?.id != null) {
                    viewModel.actualizarTrabajador(actual.id, nombre, cargo, area)
                } else {
                    viewModel.crearTrabajador(nombre, cargo, area)
                }
                mostrarDialogo = false
            }
        )
    }
}

@Composable
private fun DialogoTrabajador(
    inicial: TrabajadorDto?,
    onDismiss: () -> Unit,
    onGuardar: (nombre: String, cargo: String, area: String) -> Unit
) {
    var nombre by remember(inicial) { mutableStateOf(inicial?.nombre.orEmpty()) }
    var cargo by remember(inicial) { mutableStateOf(inicial?.cargo.orEmpty()) }
    var area by remember(inicial) { mutableStateOf(inicial?.areaAsignada.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (inicial == null) "Nuevo trabajador" else "Editar trabajador")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = { Text("Cargo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Área asignada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onGuardar(nombre, cargo, area) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}