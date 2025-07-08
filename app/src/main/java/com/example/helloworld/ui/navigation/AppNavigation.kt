package com.example.helloworld.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.helloworld.ui.screen.MainScreen
import com.example.helloworld.ui.screen.TitleScreen
import com.example.helloworld.ui.screen.CreateRoutineScreen
import com.example.helloworld.sampledata.RoutineRepository
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var routines by remember {
        mutableStateOf(RoutineRepository.loadRoutinesFromInternal(context))
    }

    NavHost(navController = navController, startDestination = "title") {
        composable("title") {
            // 1) 터치 시 즉시 이동
            TitleScreen(onClick = {
                navController.navigate("main") {
                    popUpTo("title") { inclusive = true }
                }
            })

            // 2) 일정 시간(3초) 후 자동 이동
            LaunchedEffect(Unit) {
                delay(2200) // 3000ms = 3초
                navController.navigate("main") {
                    popUpTo("title") { inclusive = true }
                }
            }
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
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                existingRoutines = routines
            )
        }
    }
}
