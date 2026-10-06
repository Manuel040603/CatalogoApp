package com.catalogoapp.consultoras.ui.screens.profile

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.catalogoapp.consultoras.util.bitmapABase64
import com.catalogoapp.consultoras.util.base64ABitmap
import com.catalogoapp.consultoras.viewmodel.ProfileUiState
import com.catalogoapp.consultoras.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    usuario: FirebaseUser,
    viewModel: ProfileViewModel,
    onCerrarSesion: () -> Unit,
    onVerCatalogo: () -> Unit = {},
    onIrACapacitaciones: () -> Unit = {},
    onIrAAdminConsultoras: () -> Unit = {},
    onIrAPedidos: () -> Unit = {},
    onIrAReparto: () -> Unit = {},
    onIrAAgregarProducto: () -> Unit = {}
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var preferencias by remember { mutableStateOf("") }
    var fotoNueva by remember { mutableStateOf<Bitmap?>(null) }
    val estado by viewModel.uiState.collectAsState()
    val perfilCargado by viewModel.perfil.collectAsState()

    LaunchedEffect(usuario.uid) {
        viewModel.cargarPerfil(usuario.uid)
        viewModel.registrarTokenFcm()
        viewModel.programarRecordatorioPedidos(context)
    }

    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(perfilCargado) {
        perfilCargado?.let { perfil ->
            nombre = perfil.nombre
            preferencias = perfil.preferencias
        }
    }

    val fotoGuardada = remember(perfilCargado) {
        perfilCargado?.fotoBase64?.takeIf { it.isNotBlank() }?.let { base64ABitmap(it) }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> fotoNueva = bitmap }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> if (concedido) lanzadorCamara.launch(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            val permisoOk = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (permisoOk) {
                                lanzadorCamara.launch(null)
                            } else {
                                lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val fotoAMostrar = fotoNueva ?: fotoGuardada
                    if (fotoAMostrar != null) {
                        Image(
                            bitmap = fotoAMostrar.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (nombre.isBlank()) "Consultora" else nombre,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = usuario.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Nivel Oro ⭐",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Ventas",
                    value = "S/ 1,250",
                    icon = Icons.Default.AttachMoney
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pedidos",
                    value = "42",
                    icon = Icons.Default.List
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Rango",
                    value = "Top 10",
                    icon = Icons.Default.Star
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Información Personal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = preferencias,
                        onValueChange = { preferencias = it },
                        label = { Text("Preferencias") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val fotoParaGuardar = fotoNueva?.let { bitmapABase64(it) } ?: perfilCargado?.fotoBase64 ?: ""
                            viewModel.guardarPerfil(
                                context = context,
                                uid = usuario.uid,
                                email = usuario.email ?: "",
                                nombre = nombre,
                                preferencias = preferencias,
                                fotoBase64 = fotoParaGuardar
                            )
                        },
                        enabled = estado !is ProfileUiState.Guardando && nombre.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        if (estado is ProfileUiState.Guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Actualizar Perfil", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Gestión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            ProfileActionItem("Ver Catálogo", Icons.Default.ShoppingCart, onVerCatalogo)
            ProfileActionItem("Mis Pedidos", Icons.Default.List, onIrAPedidos)
            ProfileActionItem("Reparto", Icons.Default.LocalShipping, onIrAReparto)
            ProfileActionItem("Capacitaciones", Icons.Default.School, onIrACapacitaciones)
            ProfileActionItem("Agregar Producto", Icons.Default.AddShoppingCart, onIrAAgregarProducto)
            ProfileActionItem("Admin Panel", Icons.Default.Settings, onIrAAdminConsultoras)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            when (val actual = estado) {
                is ProfileUiState.Guardado -> {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                is ProfileUiState.Error -> {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ProfileActionItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
    }
}
