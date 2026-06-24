package com.example.snake.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snake.data.local.AppDatabase
import com.example.snake.data.local.PreferenceManager
import com.example.snake.data.local.User
import com.example.snake.data.network.AuthRequest
import com.example.snake.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(context: Context, navController: NavController) {
    var email by remember { mutableStateOf("eve.holt@reqres.in") } 
    var password by remember { mutableStateOf("cityslicka") }
    val scope = rememberCoroutineScope()
    val prefManager = remember { PreferenceManager(context) }
    val db = remember { AppDatabase.getDatabase(context) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Snake Clone Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = try {
                            RetrofitClient.api.login(AuthRequest(email, password))
                        } catch (e: Exception) {
                            null
                        }

                        if (response?.token != null) {
                            prefManager.saveAuthToken(response.token)
                            val username = email.substringBefore("@")
                            navController.navigate("game/$username") { popUpTo("login") { inclusive = true } }
                        } else {
                            val localUser = db.userDao().getUserByEmail(email)
                            if (localUser != null && localUser.password == password) {
                                prefManager.saveAuthToken("local-token-${localUser.email}")
                                val username = email.substringBefore("@")
                                navController.navigate("game/$username") { popUpTo("login") { inclusive = true } }
                            } else {
                                Toast.makeText(context, "Auth Error: Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun RegisterScreen(context: Context, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                scope.launch {
                    try {
                        try {
                            RetrofitClient.api.register(AuthRequest(email, password))
                        } catch (_: Exception) {
                        }

                        db.userDao().insertUser(User(email, password))
                        Toast.makeText(context, "Registration Success (Saved Locally)!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login")

                    } catch (e: Exception) {
                        Toast.makeText(context, "Registration Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}