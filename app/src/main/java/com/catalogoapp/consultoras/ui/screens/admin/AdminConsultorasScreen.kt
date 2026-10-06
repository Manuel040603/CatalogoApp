package com.catalogoapp.consultoras.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catalogoapp.consultoras.data.model.ConsultoraProfile
import com.catalogoapp.consultoras.viewmodel.AdminConsultorasViewModel

@Composable
fun AdminConsultorasScreen(
    viewModel: AdminConsultorasViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Administrar consultoras", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (estado.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (estado.consultoras.isEmpty()) {
            Text("Aun no hay consultoras registradas")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(estado.consultoras) { consultora ->
                TarjetaConsultora(
                    consultora = consultora,
                    actualizando = estado.actualizando,
                    onCambiarEstado = { activo -> viewModel.cambiarEstado(consultora.uid, activo) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaConsultora(
    consultora: ConsultoraProfile,
    actualizando: Boolean,
    onCambiarEstado: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    consultora.nombre.ifBlank { "Sin nombre registrado" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(consultora.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    if (consultora.activo) "Activa" else "Inactiva",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (consultora.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Switch(
                checked = consultora.activo,
                onCheckedChange = onCambiarEstado,
                enabled = !actualizando
            )
        }
    }
}
