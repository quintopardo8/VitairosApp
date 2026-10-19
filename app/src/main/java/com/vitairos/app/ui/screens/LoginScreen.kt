package com.vitairos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.ui.theme.*
import com.vitairos.app.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ReportViewModel,
    onLoginExitoso: () -> Unit
) {
    val usuarioActivo by viewModel.usuarioActivo.collectAsStateWithLifecycle()
    var expandirUsuarios by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitairosDarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = VitairosSurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = VitairosTealPrimary,
                    modifier = Modifier.size(60.dp)
                )

                Text(
                    text = "VITAIROS Operaciones",
                    style = MaterialTheme.typography.titleLarge,
                    color = VitairosTextPrimary
                )

                Text(
                    text = "Seleccione el perfil de operador en faena:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VitairosTextSecondary
                )

                ExposedDropdownMenuBox(
                    expanded = expandirUsuarios,
                    onExpandedChange = { expandirUsuarios = !expandirUsuarios }
                ) {
                    OutlinedTextField(
                        value = usuarioActivo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operador de Turno") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirUsuarios) },
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
                        expanded = expandirUsuarios,
                        onDismissRequest = { expandirUsuarios = false },
                        modifier = Modifier.background(VitairosSurfaceDark)
                    ) {
                        viewModel.usuariosDisponibles.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user, color = VitairosTextPrimary) },
                                onClick = {
                                    viewModel.seleccionarUsuario(user)
                                    expandirUsuarios = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onLoginExitoso,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitairosTealPrimary,
                        contentColor = VitairosDarkBackground
                    )
                ) {
                    Text("Ingresar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}