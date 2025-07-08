package com.example.helloworld.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.helloworld.ui.screen.MainScreen
import com.example.helloworld.ui.screen.TitleScreen
import com.example.helloworld.ui.screen.CreateRoutineScreen
import com.example.helloworld.sampledata.RoutineRepository


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var routines by remember { mutableStateOf(RoutineRepository.loadRoutinesFromInternal(context)) }

    NavHost(navController = navController, startDestination = "title") {
        composable("title") {
            TitleScreen(onClick = {
                navController.navigate("main") {
                    popUpTo("title") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(
                navController = navController,
                routines = routines,
                onRoutinesChanged = { newList ->
                    routines = newList
                    RoutineRepository.saveRoutinesToInternal(context, newList)
                }
            )
        }
        composable("create_routine") {
            CreateRoutineScreen(
                onRoutineCreated = { newRoutine ->
                    val updated = routines + newRoutine
                    routines = updated
                    RoutineRepository.saveRoutinesToInternal(context, updated)
                    navController.popBackStack() // 이전 화면(MainScreen)으로 복귀
                },
                onBack = { navController.popBackStack() },
                existingRoutines = routines
            )
        }
    }
}
