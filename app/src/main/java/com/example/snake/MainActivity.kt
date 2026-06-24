package com.example.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snake.ui.screens.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainNavigation(this)
            }
        }
    }
}


@Composable
fun MainNavigation(context: android.content.Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(context, navController) }
        composable("register") { RegisterScreen(context, navController) }
        composable("game/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Player"
            GameScreen(context, username, navController)
        }
        composable("leaderboard") { LeaderboardScreen(context, navController) }
    }
}