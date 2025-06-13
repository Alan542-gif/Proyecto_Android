package com.example.medireminder.activities

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp


@Composable
fun AddMedicineScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedDays = remember { mutableStateListOf<String>() }
    var showDayDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var doseMenuExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val amPm = if (selectedHour < 12) "a.m." else "p.m."
            val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            time = String.format("%d:%02d %s", hour12, selectedMinute, amPm)
        },
        hour,
        minute,
        false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Nuevo\nmedicamento",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Nombre del medicamento") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dosis con DropdownMenu
        Box {
            SelectorField(
                label = "Dosis",
                value = if (dose.isEmpty()) "Selecciona la dosis" else dose,
                onClick = { doseMenuExpanded = true }
            )

            DropdownMenu(
                expanded = doseMenuExpanded,
                onDismissRequest = { doseMenuExpanded = false }
            ) {
                (1..10).forEach { num ->
                    DropdownMenuItem(
                        text = { Text("$num") },
                        onClick = {
                            dose = num.toString()
                            doseMenuExpanded = false
                        }
                    )
                }
            }
        }

        SelectorField(label = "Hora", value = if (time.isEmpty()) "Selecciona la hora" else time) {
            timePickerDialog.show()
        }

        SelectorField(
            label = "Días",
            value = if (selectedDays.isEmpty()) "Selecciona los días" else selectedDays.joinToString(", "),
            onClick = { showDayDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Notas", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "Por favor ingresa el nombre del medicamento."
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val db = Firebase.firestore
                        val newMed = hashMapOf(
                            "name" to name.trim(),
                            "dose" to dose.trim(),
                            "time" to time.trim(),
                            "days" to selectedDays,
                            "notes" to notes.trim(),
                            "status" to "Pendiente",
                            "active" to true
                        )
                        db.collection("medicamentos").add(newMed).await()

                        // Navegar a home limpiando pila
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        errorMessage = e.localizedMessage ?: "Error al guardar"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                text = if (isLoading) "Guardando..." else "Guardar",
                color = Color.White
            )
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDayDialog) {
        DaysDialog(
            initialSelected = selectedDays.toList(),
            onDismiss = { showDayDialog = false },
            onConfirm = {
                selectedDays.clear()
                selectedDays.addAll(it)
                showDayDialog = false
            }
        )
    }
}

@Composable
fun SelectorField(label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun DaysDialog(
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
            TextButton(onClick = { onConfirm(selected) }) {
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
                                if (selected.contains(day)) {
                                    selected.remove(day)
                                } else {
                                    selected.add(day)
                                }
                            }
                    ) {
                        Checkbox(
                            checked = selected.contains(day),
                            onCheckedChange = null
                        )
                        Text(text = day)
                    }
                }
            }
        }
    )
}
