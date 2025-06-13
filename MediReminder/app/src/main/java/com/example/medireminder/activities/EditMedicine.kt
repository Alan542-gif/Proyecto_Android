package com.example.medireminder.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(
    id: String,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("Selecciona la hora") }
    var days by remember { mutableStateOf(listOf<String>()) }
    var notes by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        try {
            val doc = db.collection("medicamentos").document(id).get().await()
            name = doc.getString("name") ?: ""
            dose = doc.getString("dose") ?: ""
            time = doc.getString("time") ?: "Selecciona la hora"
            days = doc.get("days") as? List<String> ?: emptyList()
            notes = doc.getString("notes") ?: ""
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Editar", style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ))
                            Text("medicamento", style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del medicamento") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("Dosis") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectorField(
                    label = "Hora",
                    value = time,
                    onClick = { showTimeDialog = true }
                )

                SelectorField(
                    label = "Días",
                    value = if (days.isEmpty()) "Selecciona los días" else days.joinToString(", "),
                    onClick = { showDaysDialog = true }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (Opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isSaving = true
                        errorMessage = null
                        db.collection("medicamentos").document(id)
                            .update(
                                mapOf(
                                    "name" to name,
                                    "dose" to dose,
                                    "time" to time,
                                    "days" to days,
                                    "notes" to notes
                                )
                            )
                            .addOnSuccessListener {
                                isSaving = false
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                errorMessage = e.localizedMessage ?: "Error al guardar cambios"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving && !isDeleting
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(modifier = Modifier.padding(horizontal = 32.dp))
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isDeleting = true
                        errorMessage = null
                        db.collection("medicamentos").document(id)
                            .update("active", false)
                            .addOnSuccessListener {
                                isDeleting = false
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                isDeleting = false
                                errorMessage = e.localizedMessage ?: "Error al actualizar"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDaysDialog) {
        DaysDialog(
            initialSelected = days,
            onDismiss = { showDaysDialog = false },
            onConfirm = {
                days = it
                showDaysDialog = false
            }
        )
    }

    if (showTimeDialog) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimeDialog = false },
            onConfirm = {
                time = it
                showTimeDialog = false
            }
        )
    }
}

@Composable
fun SelectoField(label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun DayDialog(
    initialSelected: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val options = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )
    val selected = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecciona los días") },
        text = {
            Column {
                options.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (selected.contains(day)) selected.remove(day) else selected.add(day)
                            }
                    ) {
                        Checkbox(
                            checked = selected.contains(day),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = day)
                    }
                }
            }
        }
    )
}

// Simple TimePicker Dialog placeholder (text input). Puedes reemplazar por uno nativo o librería.

@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var timeInput by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timeInput) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Selecciona la hora") },
        text = {
            OutlinedTextField(
                value = timeInput,
                onValueChange = { timeInput = it },
                label = { Text("Hora (ej: 08:30 AM)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
