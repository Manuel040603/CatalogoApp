package com.catalogoapp.consultoras.ui.screens.reparto

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.catalogoapp.consultoras.data.model.ParadaRepartoDto
import com.catalogoapp.consultoras.util.PolylineDecoder
import com.catalogoapp.consultoras.viewmodel.RepartoViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun RepartoDetalleScreen(
    idReparto: Int,
    viewModel: RepartoViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(idReparto) {
        val configuracion = Configuration.getInstance()
        configuracion.userAgentValue = context.packageName
        configuracion.osmdroidBasePath = context.cacheDir
        configuracion.osmdroidTileCache = context.cacheDir
        viewModel.cargarReparto(idReparto)
    }

    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            obtenerUbicacionYActualizar(context, idReparto, viewModel)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reparto #$idReparto", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (estado.cargandoReparto && estado.repartoActual == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val reparto = estado.repartoActual
        if (reparto == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(estado.error ?: "No se pudo cargar el reparto")
            }
            return@Column
        }

        Text("${reparto.zona} - ${reparto.chofer} - ${reparto.vehiculo}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        MapaReparto(
            paradas = reparto.paradas,
            geometriaRuta = reparto.geometriaRuta,
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val concedido = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (concedido) {
                    obtenerUbicacionYActualizar(context, idReparto, viewModel)
                } else {
                    lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar mi ubicacion")
        }
        Spacer(modifier = Modifier.height(8.dp))

        estado.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text("Paradas en orden de entrega", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reparto.paradas.sortedBy { it.orden }) { parada ->
                FilaParada(
                    parada = parada,
                    actualizando = estado.actualizandoParadaId == parada.id,
                    onMarcarEntregada = { viewModel.marcarParadaEntregada(idReparto, parada.id) }
                )
            }
        }
    }
}

@Composable
private fun MapaReparto(
    paradas: List<ParadaRepartoDto>,
    geometriaRuta: String?,
    modifier: Modifier = Modifier
) {
    val formaMapa = RoundedCornerShape(16.dp)
    val modificadorContenedor = modifier
        .clip(formaMapa)
        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), formaMapa)

    if (paradas.isEmpty()) {
        Box(modifier = modificadorContenedor, contentAlignment = Alignment.Center) {
            Text("Sin paradas para mostrar")
        }
        return
    }

    val mapViewReferencia = remember { mutableStateOf<MapView?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            mapViewReferencia.value?.onDetach()
        }
    }

    AndroidView(
        modifier = modificadorContenedor,
        factory = { contexto ->
            MapView(contexto).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                mapViewReferencia.value = this
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val paradasOrdenadas = paradas.sortedBy { it.orden }
            val puntosParadas = paradasOrdenadas.map { GeoPoint(it.latitud, it.longitud) }
            paradasOrdenadas.forEachIndexed { indice, parada ->
                val punto = GeoPoint(parada.latitud, parada.longitud)
                val marcador = Marker(mapView)
                marcador.position = punto
                marcador.title = "${indice + 1}. ${parada.nombreCliente}"
                marcador.snippet = if (parada.entregada) "Entregado" else "Pendiente"
                mapView.overlays.add(marcador)
            }

            val puntosRutaDecodificados: List<GeoPoint> = if (!geometriaRuta.isNullOrBlank()) {
                try {
                    val puntosRuta = PolylineDecoder.decodificar(geometriaRuta)
                    if (puntosRuta.isNotEmpty()) {
                        val linea = Polyline()
                        linea.setPoints(puntosRuta)
                        mapView.overlays.add(linea)
                    }
                    puntosRuta
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            mapView.invalidate()

            val puntosParaEncuadre = puntosParadas + puntosRutaDecodificados
            mapView.post {
                val caja = BoundingBox.fromGeoPoints(puntosParaEncuadre)
                mapView.zoomToBoundingBox(caja, false, 120, 17.0, 0L)
            }
        }
    )
}

@Composable
private fun FilaParada(
    parada: ParadaRepartoDto,
    actualizando: Boolean,
    onMarcarEntregada: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${parada.orden + 1}. ${parada.nombreCliente}", fontWeight = FontWeight.Bold)
                Text(parada.direccion, style = MaterialTheme.typography.bodySmall)
            }
            if (parada.entregada) {
                Text("Entregado", color = MaterialTheme.colorScheme.primary)
            } else {
                Button(onClick = onMarcarEntregada, enabled = !actualizando) {
                    if (actualizando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Marcar entregada")
                    }
                }
            }
        }
    }
}

private fun obtenerUbicacionYActualizar(
    context: android.content.Context,
    idReparto: Int,
    viewModel: RepartoViewModel
) {
    val concedido = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!concedido) return

    val gestorUbicacion = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (proveedor in proveedores) {
        try {
            if (gestorUbicacion.isProviderEnabled(proveedor)) {
                val ultimaUbicacion = gestorUbicacion.getLastKnownLocation(proveedor)
                if (ultimaUbicacion != null) {
                    viewModel.actualizarMiUbicacion(idReparto, ultimaUbicacion.latitude, ultimaUbicacion.longitude)
                    return
                }
            }
        } catch (e: SecurityException) {
        }
    }
}
