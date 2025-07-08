package com.example.helloworld.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.helloworld.ui.component.BottomNavigationBar

@Composable
fun MainScreen(
    navController: NavHostController,
    routines: List<Routine>,
    onRoutinesChanged: (List<Routine>) -> Unit
) {
    val tabNavController = rememberNavController()
    Text("MainScreen", color = Color.Red)
    Scaffold(
        bottomBar = { BottomNavigationBar(tabNavController) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavHost(
                navController = tabNavController,
                startDestination = "camera"
            ) {
                composable("routine") {
                    RoutineScreen(
                        routines = routines,
                        onRoutinesChanged = onRoutinesChanged,
                        onCreateRoutine = { navController.navigate("create_routine") }
                    )
                }
                composable("camera") { CameraScreen() }
                composable("diary") { DiaryScreen() }

            }
        }
    }
}
