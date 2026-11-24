package com.vitairos.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitairos.app.R
import com.vitairos.app.ui.theme.VitairosDarkBackground
import com.vitairos.app.ui.theme.VitairosSurfaceDark
import com.vitairos.app.ui.theme.VitairosSurfaceVariant
import com.vitairos.app.ui.theme.VitairosTealPrimary
import com.vitairos.app.ui.theme.VitairosTextPrimary
import com.vitairos.app.ui.theme.VitairosTextSecondary
import com.vitairos.app.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ReportViewModel,
    onLoginExitoso: () -> Unit
) {
    val usuarioActivo by viewModel.usuarioActivo.collectAsStateWithLifecycle()
    val usuariosDisponibles by viewModel.usuariosDisponibles.collectAsStateWithLifecycle()
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
                Image(
                    painter = painterResource(id = R.drawable.vitairos),
                    contentDescription = "Logo VITAIROS",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
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
                        usuariosDisponibles.forEach { user ->
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ingresar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}