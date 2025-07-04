package com.example.helloworld.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.helloworld.ui.screen.MainScreen
import com.example.helloworld.ui.screen.TitleScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "title") {
        composable("title") {
            TitleScreen(onClick = {
                navController.navigate("main") {
                    popUpTo("title") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen()
        }
    }
}
