package com.catalogoapp.consultoras.ui.screens.pedido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catalogoapp.consultoras.data.model.Producto
import com.catalogoapp.consultoras.viewmodel.PedidoViewModel

@Composable
fun NuevoPedidoScreen(
    viewModel: PedidoViewModel,
    onVolver: () -> Unit,
    onPedidoCreado: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje != null) {
            viewModel.limpiarMensaje()
            onPedidoCreado()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nuevo pedido", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = estado.nombreCliente,
            onValueChange = viewModel::actualizarNombreCliente,
            label = { Text("Nombre del cliente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = estado.telefonoCliente,
            onValueChange = viewModel::actualizarTelefonoCliente,
            label = { Text("Telefono del cliente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = estado.direccion,
            onValueChange = viewModel::actualizarDireccion,
            label = { Text("Direccion de entrega") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estado.productosDisponibles) { producto ->
                FilaProductoSeleccionable(
                    producto = producto,
                    cantidad = viewModel.cantidadEnCarritoDe(producto.id),
                    onCambiarCantidad = { cantidad -> viewModel.cambiarCantidadEnCarrito(producto, cantidad) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Total: S/ ${"%.2f".format(viewModel.totalCarrito())}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        estado.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.crearPedido() },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Registrar pedido")
            }
        }
    }
}

@Composable
private fun FilaProductoSeleccionable(
    producto: Producto,
    cantidad: Int,
    onCambiarCantidad: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text("S/ ${"%.2f".format(producto.precio)}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(enabled = cantidad > 0, onClick = { onCambiarCantidad(cantidad - 1) }) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text("$cantidad", modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onCambiarCantidad(cantidad + 1) }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
