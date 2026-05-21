package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.myapplication.presentation.screens.FormularioScreen
import com.example.myapplication.presentation.screens.ResumenScreen
import com.example.myapplication.presentation.state.EcoTripUiEvent
import com.example.myapplication.presentation.viewmodel.EcoTripViewModel

@Composable
fun EcoTripNavHost(
    navController: NavHostController,
    viewModel: EcoTripViewModel
) {
    NavHost(
        navController = navController,
        startDestination = FormularioRoute
    ) {
        composable<FormularioRoute> {
            FormularioScreen(
                viewModel = viewModel,
                onNavigateToResumen = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable<ResumenRoute> { backStackEntry ->
            val resumen: ResumenRoute = backStackEntry.toRoute()
            ResumenScreen(
                resumen = resumen,
                onNavigateBack = {
                    viewModel.onEvent(EcoTripUiEvent.ResetearEstadoNavegacion)
                    
                    navController.navigate(FormularioRoute) {
                        popUpTo<FormularioRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
