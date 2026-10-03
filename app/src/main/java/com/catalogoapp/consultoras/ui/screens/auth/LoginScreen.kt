package com.catalogoapp.consultoras.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.catalogoapp.consultoras.viewmodel.AuthUiState
import com.catalogoapp.consultoras.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado) {
        if (estado is AuthUiState.Exito) {
            onLoginExitoso()
            viewModel.resetEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CatalogoApp", style = MaterialTheme.typography.headlineMedium)
        Text("Ingreso de consultoras", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electronico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasena") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.iniciarSesion(email.trim(), password) },
            enabled = estado !is AuthUiState.Cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado is AuthUiState.Cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Iniciar sesion")
            }
        }

        TextButton(onClick = onIrARecuperar) {
            Text("¿Olvidaste tu contrasena?")
        }

        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Registrate")
        }

        if (estado is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (estado as AuthUiState.Error).mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
