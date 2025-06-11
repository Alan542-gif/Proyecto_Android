package com.example.medireminder.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PerfilScreen(onSignOut: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Pantalla Perfil")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSignOut) {
            Text("Cerrar sesión")
        }
    }
}
