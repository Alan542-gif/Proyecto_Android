package com.example.medireminder.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Medicamento(
    val id: String = "",
    val name: String = "",
    val dose: String = "",
    val time: String = "",
    val notas: String = ""
)

@Composable
fun MedicineListScreen() {
    var medicamentos by remember { mutableStateOf(listOf<Medicamento>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val db = Firebase.firestore
                val snapshot = db.collection("medicamentos").get().await()
                medicamentos = snapshot.documents.map { doc ->
                    Medicamento(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        dose = doc.getString("dose") ?: "",
                        time = doc.getString("time") ?: "",
                        notas = doc.getString("notes") ?: ""
                    )
                }
                isLoading = false
                errorMessage = null
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Error al cargar medicamentos"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Historial de Medicamentos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null -> {
                Text(errorMessage ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
            }
            medicamentos.isEmpty() -> {
                Text("No hay medicamentos registrados.")
            }
            else -> {
                LazyColumn {
                    items(medicamentos) { med ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = med.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Dosis: ${med.dose}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Hora: ${med.time}", style = MaterialTheme.typography.bodyMedium)
                                if (med.notas.isNotBlank()) {
                                    Text(text = "Notas: ${med.notas}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
