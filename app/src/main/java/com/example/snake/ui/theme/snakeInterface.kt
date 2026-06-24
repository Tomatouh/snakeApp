package com.example.snake.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snake.data.local.AppDatabase
import com.example.snake.data.local.LocalScore
import com.example.snake.game.Direction
import com.example.snake.game.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(context: Context, username: String, navController: NavController) {
    var gameState by remember { mutableStateOf(GameState()) }
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    LaunchedEffect(gameState.isGameOver) {
        while (!gameState.isGameOver) {
            delay(150)
            gameState = gameState.move()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Player: $username", style = MaterialTheme.typography.titleMedium)
            Text("Score: ${gameState.currentScore}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF388E3C))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .border(2.dp, Color.DarkGray)
                .background(Color(0xFFE8F5E9))
        ) {
            val cellSize = 300.dp / GameState.BOARD_SIZE

            Box(
                modifier = Modifier
                    .offset(x = cellSize * gameState.food.x, y = cellSize * gameState.food.y)
                    .size(cellSize)
                    .background(Color.Red)
            )

            gameState.snake.forEach { segment ->
                Box(
                    modifier = Modifier
                        .offset(x = cellSize * segment.x, y = cellSize * segment.y)
                        .size(cellSize)
                        .background(Color(0xFF2E7D32))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { if(gameState.direction != Direction.DOWN) gameState = gameState.copy(direction = Direction.UP) }) { Text("▲") }
            Row {
                Button(onClick = { if(gameState.direction != Direction.RIGHT) gameState = gameState.copy(direction = Direction.LEFT) }) { Text("◀") }
                Spacer(modifier = Modifier.width(40.dp))
                Button(onClick = { if(gameState.direction != Direction.LEFT) gameState = gameState.copy(direction = Direction.RIGHT) }) { Text("▶") }
            }
            Button(onClick = { if(gameState.direction != Direction.UP) gameState = gameState.copy(direction = Direction.DOWN) }) { Text("▼") }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (gameState.isGameOver) {
            Text("GAME OVER", style = MaterialTheme.typography.headlineSmall, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                onClick = {
                    scope.launch {
                        db.scoreDao().insertScore(LocalScore(username = username, score = gameState.currentScore))
                        navController.navigate("leaderboard")
                    }
                }
            ) {
                Text("Save Score & View Standings")
            }
        }
    }
}