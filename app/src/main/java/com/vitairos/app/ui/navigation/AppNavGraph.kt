package com.vitairos.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vitairos.app.ui.screens.NewReportScreen
import com.vitairos.app.ui.screens.QueueScreen
import com.vitairos.app.viewmodel.ReportViewModel

object Destinos {
    const val COLA_REPORTES = "cola_reportes"
    const val NUEVO_REPORTE = "nuevo_reporte"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: ReportViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Destinos.COLA_REPORTES
    ) {
        // Pantalla 1
        composable(Destinos.COLA_REPORTES) {
            QueueScreen(
                viewModel = viewModel,
                onNavigateToNewReport = {
                    navController.navigate(Destinos.NUEVO_REPORTE)
                },
                onNavigateBack = {
                }
            )
        }

        // Pantalla 2
        composable(Destinos.NUEVO_REPORTE) {
            NewReportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}