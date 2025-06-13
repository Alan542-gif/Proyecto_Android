package com.example.medireminder.activities

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.medireminder.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PerfilScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    val user = auth.currentUser
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                .background(Color.White.copy(alpha = 0.85f))
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.Start,
                   modifier = Modifier.fillMaxWidth()
                ) {
                Text(
                    text = "Ajustes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(vertical = 8.dp)
                ) {
                    Text("Notificaciones", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Botón cerrar sesión
                Button(
                    onClick = {
                        auth.signOut()
                        context.startActivity(
                            Intent(context, SignInActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón eliminar cuenta (elimina datos en Firestore)
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar datos", color = Color.White)
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar datos
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar datos?") },
            text = { Text("Esto eliminará tus datos de esta app, pero no tu cuenta de Google. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    firestore.collection("medicamentos")
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val batch = firestore.batch()
                            for (document in querySnapshot.documents) {
                                batch.delete(document.reference)
                            }
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "Todos los datos fueron eliminados", Toast.LENGTH_SHORT).show()
                                showDeleteDialog = false
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar los datos", Toast.LENGTH_LONG).show()
                                showDeleteDialog = false
                            }
                        }
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            }

        )
    }
}
