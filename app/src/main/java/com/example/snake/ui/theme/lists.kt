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
import kotlinx.coroutines.flow.first
import kotlin.random.Random

@Composable
fun LeaderboardScreen(context: Context, navController: NavController) {
    val db = remember { AppDatabase.getDatabase(context) }
    val localScores by db.scoreDao().getAllScoresDescending().collectAsState(initial = emptyList())
    var networkPlayersWithScores by remember { mutableStateOf<List<Pair<ReqResUser, Int>>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getGlobalPlayers()
            val playersWithScores = response.data.map { user ->
                val mockScore = Random(user.id.toLong()).nextInt(20, 150)*10
                user to mockScore
            }.sortedByDescending { it.second }

            networkPlayersWithScores = playersWithScores

            val currentLocal = db.scoreDao().getAllScoresDescending().first()
            if (currentLocal.isEmpty()) {
                playersWithScores.take(3).forEach { (user, score) ->
                    db.scoreDao().insertOrUpdateHigherScore(LocalScore(
                        username = "${user.firstName} ${user.lastName}",
                        score = score
                    ))
                }
            }
        } catch (e: Exception) {
            networkPlayersWithScores = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboards", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

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

        Text("Global Users (Mockup Scores)", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(networkPlayersWithScores) { (user, score) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge)
                            Text(user.email, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("$score pts", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Button(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Return to Menu")
        }
    }
}
