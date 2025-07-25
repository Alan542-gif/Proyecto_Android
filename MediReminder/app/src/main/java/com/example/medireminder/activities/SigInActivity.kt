package com.example.medireminder.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.medireminder.R
import com.example.medireminder.ui.theme.MediReminderTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.material3.OutlinedTextFieldDefaults

class SignInActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            MediReminderTheme {
                var showRegister by remember { mutableStateOf(false) }

                if (showRegister) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onBackToSignIn = { showRegister = false }
                    )
                } else {
                    LoginScreen(
                        onSignInSuccess = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onShowRegister = { showRegister = true },
                        googleSignInClient = googleSignInClient
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onShowRegister: () -> Unit,
    googleSignInClient: GoogleSignInClient
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    onSignInSuccess()
                } else {
                    errorMessage = signInTask.exception?.localizedMessage ?: "Error con Google Sign-In"
                }
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Google Sign-In cancelado o fallido"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1976D2))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("MediReminder", textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), color = Color.Black)

        Image(painter = painterResource(id = R.drawable.fondo2), contentDescription = "Logo",
            modifier = Modifier.size(180.dp).align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(20.dp))

        Text("Lleva un control de tus medicamentos y horarios.", textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = Color.Black)

        Text("Inicia sesión para comenzar a organizar tu salud.", textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodySmall, color = Color.Black)

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = {
                Text("Correo electrónico", color = Color.White)
            }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White,
                cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
            )
        )

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = {
                Text("Contraseña", color = Color.White)
            }, singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White,
                cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
            )
        )


        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true; errorMessage = null
                    Firebase.auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) onSignInSuccess()
                        else errorMessage = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                    }
                } else errorMessage = "Rellena todos los campos"
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text(if (isLoading) "Iniciando..." else "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "──────────── o ────────────",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        )


        OutlinedButton(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continuar con Google")
        }


        Spacer(modifier = Modifier.height(16.dp))
        Text("¿No tienes cuenta? Regístrate", modifier = Modifier.fillMaxWidth().clickable { onShowRegister() },
            textAlign = TextAlign.Center, color = Color.Black,
            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline))

        Spacer(modifier = Modifier.height(8.dp))
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Al iniciar sesión, aceptas los términos y condiciones", textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = Color.Black)
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToSignIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1976D2)).padding(16.dp),
        verticalArrangement = Arrangement.Center) {

        Text("Registrar nuevo usuario", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("Correo electrónico", color = Color.White) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White,
                cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White))

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Contraseña", color = Color.White) }, visualTransformation = PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White,
                cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White))

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña", color = Color.White) }, visualTransformation = PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White,
                cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White))

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorMessage = "Todos los campos son obligatorios"
                return@Button
            }
            if (password != confirmPassword) {
                errorMessage = "Las contraseñas no coinciden"
                return@Button
            }
            isLoading = true; errorMessage = null
            Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) onRegisterSuccess()
                else errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
            Text(if (isLoading) "Registrando..." else "Registrar")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Volver a iniciar sesión", modifier = Modifier.clickable { onBackToSignIn() }.fillMaxWidth(),
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline))

        Spacer(modifier = Modifier.height(8.dp))
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
