package com.example.medireminder.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.medireminder.ui.theme.MediReminderTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Filled.Home)
    object Add : BottomNavScreen("add", "Añadir", Icons.Filled.Add)
    object Historial : BottomNavScreen("historial", "Historial", Icons.Filled.List)
    object Perfil : BottomNavScreen("perfil", "Perfil", Icons.Filled.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContent {
            MediReminderTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavScreen.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavScreen.Home.route) { HomeScreen() }
        composable(BottomNavScreen.Add.route) {
            AddMedicineScreen(onAddSuccess = {
                navController.navigate(BottomNavScreen.Historial.route) {
                    popUpTo(BottomNavScreen.Historial.route) { inclusive = true }
                }
            })
        }
        composable(BottomNavScreen.Historial.route) { MedicineListScreen() }
        composable(BottomNavScreen.Perfil.route) {
            PerfilScreen(
                onSignOut = {
                    Firebase.auth.signOut()
                    navController.context.startActivity(
                        Intent(navController.context, SignInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Add,
        BottomNavScreen.Historial,
        BottomNavScreen.Perfil
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla Home")
    }
}

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

// Modelo de medicamento
data class Medicamento(
    val id: String = "",
    val nombre: String = "",
    val dosis: String = "",
    val hora: String = "",
    val notas: String = ""
)


// Pantalla para añadir medicamento
@Composable
fun AddMedicineScreen(onAddSuccess: (() -> Unit)? = null) {
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
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
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del medicamento") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dosis,
            onValueChange = { dosis = it },
            label = { Text("Dosis (ej: 500 mg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = hora,
            onValueChange = { hora = it },
            label = { Text("Hora (ej: 08:30 AM)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),  // Puedes usar Number o Text según prefieras
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Notas (opcional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombre.isBlank() || dosis.isBlank() || hora.isBlank()) {
                    errorMessage = "Por favor llena todos los campos obligatorios"
                    return@Button
                }
                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    try {
                        val db = Firebase.firestore
                        val newMed = hashMapOf(
                            "nombre" to nombre.trim(),
                            "dosis" to dosis.trim(),
                            "hora" to hora.trim(),
                            "notas" to notas.trim()
                        )
                        db.collection("medicamentos").add(newMed).await()

                        nombre = ""
                        dosis = ""
                        hora = ""
                        notas = ""
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


// Pantalla para listar medicamentos
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
                        nombre = doc.getString("nombre") ?: "",
                        dosis = doc.getString("dosis") ?: "",
                        hora = doc.getString("hora") ?: "",
                        notas = doc.getString("notas") ?: ""
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

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(errorMessage ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
        } else if (medicamentos.isEmpty()) {
            Text("No hay medicamentos registrados.")
        } else {
            LazyColumn {
                items(medicamentos) { med ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = med.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Dosis: ${med.dosis}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Hora: ${med.hora}", style = MaterialTheme.typography.bodyMedium)
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

