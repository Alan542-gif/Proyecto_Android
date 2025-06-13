package com.example.medireminder.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.sp


data class Medicamento(
    val id: String = "",
    val name: String = "",
    val dose: String = "",
    val time: String = "",
    val notas: String = "",
    val fechaRegistro: Date = Date()
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
                        notas = doc.getString("notes") ?: "",
                        fechaRegistro = doc.getDate("fechaRegistro") ?: Date ()
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
        Text("Actividad", style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        ))
        Spacer(modifier = Modifier.height(32.dp))

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
                        val etiquetaDia = med.fechaRegistro?.let { getDiaTexto(it) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (etiquetaDia != null) {
                                    Text(
                                        text = etiquetaDia,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
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

fun getDiaTexto(fecha: Date): String {
    val hoy = Calendar.getInstance()
    val fechaCal = Calendar.getInstance().apply { time = fecha }

    return when {
        esMismoDia(hoy, fechaCal) -> "Hoy"
        esAyer(hoy, fechaCal) -> "Ayer"
        else -> {
            val formato = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX"))
            formato.format(fecha)
        }
    }
}

fun esMismoDia(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

fun esAyer(hoy: Calendar, fecha: Calendar): Boolean {
    hoy.add(Calendar.DAY_OF_YEAR, -1)
    return esMismoDia(hoy, fecha)
}