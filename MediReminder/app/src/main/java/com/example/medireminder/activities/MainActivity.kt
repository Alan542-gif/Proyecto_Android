package com.example.medireminder.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.medireminder.ui.theme.MediReminderTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
        composable(BottomNavScreen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable("edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            EditMedicineScreen(id = id, navController = navController)
        }
        composable(BottomNavScreen.Add.route) {
            AddMedicineScreen(onAddSuccess = {
                navController.navigate(BottomNavScreen.Historial.route) {
                    popUpTo(BottomNavScreen.Historial.route) { inclusive = true }
                }
            })
        }
        composable(BottomNavScreen.Historial.route) {
            MedicineListScreen()
        }
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
