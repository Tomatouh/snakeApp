package com.example.snake.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.snake.data.local.AppDatabase
import com.example.snake.data.local.LocalScore
import com.example.snake.data.network.ReqResUser
import com.example.snake.data.network.RetrofitClient
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LeaderboardScreen(context: Context, navController: NavController) {
    val db = remember { AppDatabase.getDatabase(context) }
    val localScores by db.scoreDao().getAllScoresDescending().collectAsState(initial = emptyList())
    var networkPlayers by remember { mutableStateOf<List<ReqResUser>>(emptyList()) }

    // Crash Prevention: Enclose HTTP Calls safely inside Try/Catch within side-effects
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getGlobalPlayers()
            networkPlayers = response.data
        } catch (e: Exception) {
            // Logs safely or serves gracefully without forcing an app termination
            networkPlayers = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboards", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row or Side by Side Layout for Local vs Global
        Text("Your Local Top Scores (Room)", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(localScores) { record ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(record.username)
                        Text("${record.score} pts", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Global Developers (ReqRes Mock API)", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(networkPlayers) { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("${user.firstName} ${user.lastName} (${user.email})")
                    }
                }
            }
        }

        Button(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Logout / Return to Menu")
        }
    }
}