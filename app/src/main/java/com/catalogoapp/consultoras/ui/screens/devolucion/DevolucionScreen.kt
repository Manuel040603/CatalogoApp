package com.catalogoapp.consultoras.ui.screens.devolucion

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.catalogoapp.consultoras.viewmodel.DevolucionViewModel

@Composable
fun DevolucionScreen(
    viewModel: DevolucionViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> bitmap?.let { viewModel.registrarFoto(it) } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registrar devolucion", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (estado.productoSeleccionado == null) {
            Text("Selecciona el producto devuelto", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estado.productos) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.seleccionarProducto(producto) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(producto.categoria, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            return@Column
        }

        Text("Producto: ${estado.productoSeleccionado?.nombre}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        estado.foto?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto del producto devuelto",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = { lanzadorCamara.launch(null) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (estado.foto == null) "Tomar foto del producto devuelto" else "Tomar otra foto")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (estado.foto != null && estado.resultadoApto == null) {
            Button(
                onClick = { viewModel.clasificarProducto() },
                enabled = !estado.clasificando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.clasificando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Clasificar con IA")
                }
            }
        }

        estado.resultadoApto?.let { apto ->
            Spacer(modifier = Modifier.height(20.dp))
            val colorResultado = if (apto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Text(
                text = if (apto) "APTO" else "NO APTO",
                style = MaterialTheme.typography.headlineMedium,
                color = colorResultado
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Etiqueta detectada: ${estado.etiquetaDetectada}")
            Text("Confianza: ${(((estado.confianza ?: 0f) * 100).toInt())}%")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.confirmarActualizacionInventario() },
                enabled = !estado.actualizando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.actualizando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Confirmar y actualizar inventario")
                }
            }
        }

        estado.mensaje?.let { mensaje ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { viewModel.reiniciar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar otra devolucion")
            }
        }

        estado.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

