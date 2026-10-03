package com.catalogoapp.consultoras.ui.screens.profile

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    onIrAReparto: () -> Unit = {}
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var preferencias by remember { mutableStateOf("") }
    var foto by remember { mutableStateOf<Bitmap?>(null) }
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

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> foto = bitmap }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> if (concedido) lanzadorCamara.launch(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineSmall)
        Text(usuario.email ?: "", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (foto != null) {
                Image(
                    bitmap = foto!!.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            val permisoOk = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (permisoOk) lanzadorCamara.launch(null) else lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }) {
            Text("Tomar foto con la camara")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = preferencias,
            onValueChange = { preferencias = it },
            label = { Text("Preferencias (ej: maquillaje, fragancias)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.guardarPerfil(
                    context = context,
                    uid = usuario.uid,
                    email = usuario.email ?: "",
                    nombre = nombre,
                    preferencias = preferencias,
                    tienePhoto = foto != null
                )
            },
            enabled = estado !is ProfileUiState.Guardando && nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado is ProfileUiState.Guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Guardar perfil")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
            Text("Ver catalogo")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onIrAPedidos, modifier = Modifier.fillMaxWidth()) {
            Text("Pedidos")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onIrAReparto, modifier = Modifier.fillMaxWidth()) {
            Text("Reparto de pedidos")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onIrACapacitaciones, modifier = Modifier.fillMaxWidth()) {
            Text("Capacitaciones")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onIrAAdminConsultoras, modifier = Modifier.fillMaxWidth()) {
            Text("Administrar consultoras")
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesion")
        }

        when (val actual = estado) {
            is ProfileUiState.Guardado -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(actual.mensaje, color = MaterialTheme.colorScheme.primary)
            }
            is ProfileUiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}
