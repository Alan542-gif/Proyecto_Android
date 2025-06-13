package com.example.medireminder.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.example.medireminder.R
import androidx.compose.ui.unit.sp


data class Medicine(
    val id: String = "",
    val name: String = "",
    val dose: String = "",
    val interval: String = "",
    val days: List<String> = emptyList(),
    val time: String = "",
    val status: String = "",
    val active: Boolean = true,
    val notes: String = ""
)

@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("medicamentos")
                .whereEqualTo("active", true)
                .get()
                .await()

            medicines = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Medicine::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {

        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo3),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFB3E5FC).copy(alpha = 0.85f))
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (medicines.isEmpty()) {
                Text(
                    text = "No tienes medicamentos activos.",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column {
                    Text("Hola", fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Tus medicamentos", fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(medicines) { med ->
                            MedicineCard(med) {
                                navController.navigate("edit/${med.id}")
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicineCard(medicine: Medicine, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = medicine.name, fontSize = 18.sp, color = Color.Black)
                Text(
                    text = if (medicine.status == "Tomado") "✓ Tomado" else "⏺ Próximo",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Dosis: ${medicine.dose}")
            if (medicine.interval.isNotEmpty()) Text(medicine.interval)
            if (medicine.days.isNotEmpty()) Text(medicine.days.joinToString(", "))
            if (medicine.time.isNotEmpty()) Text(medicine.time)
            if (medicine.notes.isNotEmpty()) Text("Notas: ${medicine.notes}")
        }
    }
}
