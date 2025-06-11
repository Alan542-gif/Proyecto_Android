package com.example.medireminder.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddMedicineScreen(onAddSuccess: (() -> Unit)? = null) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pendiente") }
    var active by remember { mutableStateOf(true) }
    var interval by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Añadir Medicamento", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del medicamento") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            label = { Text("Dosis") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora (ej: 08:30 AM)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas (opcional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Estado (status)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if (active) "Activo" else "Inactivo",
            onValueChange = {
                active = it.equals("activo", ignoreCase = true)
            },
            label = { Text("Activo (Activo/Inactivo)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = interval,
            onValueChange = { interval = it },
            label = { Text("Intervalo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = days,
            onValueChange = { days = it },
            label = { Text("Días") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank() || dose.isBlank() || time.isBlank()) {
                    errorMessage = "Por favor llena todos los campos obligatorios"
                    return@Button
                }
                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    try {
                        val db = Firebase.firestore
                        val newMed = hashMapOf(
                            "name" to name.trim(),
                            "dose" to dose.trim(),
                            "time" to time.trim(),
                            "notes" to notes.trim(),
                            "status" to status.trim(),
                            "active" to active,
                            "interval" to interval.trim(),
                            "days" to days.trim()
                        )
                        db.collection("medicamentos").add(newMed).await()

                        // Reset campos
                        name = ""
                        dose = ""
                        time = ""
                        notes = ""
                        status = "Pendiente"
                        active = true
                        interval = ""
                        days = ""

                        isLoading = false
                        errorMessage = null
                        onAddSuccess?.invoke()
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = e.localizedMessage ?: "Error al agregar medicamento"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Agregando..." else "Agregar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
