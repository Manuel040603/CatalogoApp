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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    val estado by viewModel.uiState.collectAsState()

    val contrasenasNoCoinciden = password.isNotEmpty() && password != confirmarPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro de consultora", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

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
            label = { Text("Contrasena (minimo 6 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contrasena") },
            singleLine = true,
            isError = contrasenasNoCoinciden,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        if (contrasenasNoCoinciden) {
            Text("Las contrasenas no coinciden", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.registrar(email.trim(), password) },
            enabled = estado !is AuthUiState.Cargando &&
                email.isNotBlank() && password.length >= 6 && !contrasenasNoCoinciden,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado is AuthUiState.Cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Crear cuenta")
            }
        }

        TextButton(onClick = onIrALogin) {
            Text("¿Ya tienes cuenta? Inicia sesion")
        }

        when (val actual = estado) {
            is AuthUiState.Exito -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(actual.mensaje, color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(Unit) {
                    onRegistroExitoso()
                    viewModel.resetEstado()
                }
            }
            is AuthUiState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}
