package com.catalogoapp.consultoras.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catalogoapp.consultoras.data.model.Producto
import com.catalogoapp.consultoras.viewmodel.CatalogoViewModel

@Composable
fun CatalogoScreen(
    viewModel: CatalogoViewModel,
    onVolver: () -> Unit,
    onRegistrarDevolucion: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catalogo de productos", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onRegistrarDevolucion, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar devolucion")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (estado.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (estado.productos.isEmpty() && estado.categoriaSeleccionada == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aun no hay productos cargados")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.cargarProductosDeEjemplo() }) {
                    Text("Cargar productos de ejemplo")
                }
            }
            return@Column
        }

        val categorias = viewModel.categoriasDisponibles()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = estado.categoriaSeleccionada == null,
                    onClick = { viewModel.filtrarPorCategoria(null) },
                    label = { Text("Todas") }
                )
            }
            items(categorias) { categoria ->
                FilterChip(
                    selected = estado.categoriaSeleccionada == categoria,
                    onClick = { viewModel.filtrarPorCategoria(categoria) },
                    label = { Text(categoria) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(estado.productos) { producto -> TarjetaProducto(producto) }
        }
    }
}

@Composable
private fun TarjetaProducto(producto: Producto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(producto.categoria, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("S/ ${producto.precio}", fontWeight = FontWeight.Bold)
                Text("Stock: ${producto.stock}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}