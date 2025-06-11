package com.example.medireminder.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Alignment
import androidx.navigation.NavController

@Composable
fun EditMedicineScreen(
    id: String,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        try {
            val doc = db.collection("medicamentos").document(id).get().await()
            name = doc.getString("name") ?: ""
            dose = doc.getString("dose") ?: ""
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Error al cargar medicamento"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Editar Medicamento", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                label = { Text("Dosis") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para guardar cambios
            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    db.collection("medicamentos").document(id)
                        .update("name", name, "dose", dose)
                        .addOnSuccessListener {
                            isSaving = false
                            navController.popBackStack()  // Regresa al Home
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            errorMessage = e.localizedMessage ?: "Error al guardar cambios"
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && !isDeleting
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar cambios")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para "eliminar" (marcar como inactive)
            Button(
                onClick = {
                    isDeleting = true
                    errorMessage = null
                    db.collection("medicamentos").document(id)
                        .update("active", false)
                        .addOnSuccessListener {
                            isDeleting = false
                            navController.popBackStack()  // Regresa al Home
                        }
                        .addOnFailureListener { e ->
                            isDeleting = false
                            errorMessage = e.localizedMessage ?: "Error al actualizar"
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !isSaving && !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Eliminar medicamento", color = MaterialTheme.colorScheme.onError)
            }

            Spacer(modifier = Modifier.height(12.dp))

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
