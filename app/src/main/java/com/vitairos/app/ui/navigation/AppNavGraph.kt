package com.vitairos.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitairos.app.ui.screens.DashboardScreen
import com.vitairos.app.ui.screens.LoginScreen
import com.vitairos.app.ui.screens.NewReportScreen
import com.vitairos.app.ui.screens.ReportDetailScreen
import com.vitairos.app.viewmodel.ReportViewModel

object Destinos {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val NUEVO_REPORTE = "nuevo_reporte"
    const val EDITAR_REPORTE = "editar_reporte/{id}"
    const val DETALLE = "detalle/{id}"

    fun editar(id: String) = "editar_reporte/$id"
    fun detalle(id: String) = "detalle/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: ReportViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Destinos.LOGIN,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable(Destinos.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoginExitoso = {
                    navController.navigate(Destinos.DASHBOARD) {
                        popUpTo(Destinos.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToNewReport = { navController.navigate(Destinos.NUEVO_REPORTE) },
                onOpenDetail = { id -> navController.navigate(Destinos.detalle(id)) },
                onLogout = {
                    navController.navigate(Destinos.LOGIN) {
                        popUpTo(Destinos.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.NUEVO_REPORTE) {
            NewReportScreen(
                viewModel = viewModel,
                reporteId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.EDITAR_REPORTE,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entrada ->
            NewReportScreen(
                viewModel = viewModel,
                reporteId = entrada.arguments?.getString("id"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString("id") ?: ""
            ReportDetailScreen(
                viewModel = viewModel,
                reporteId = id,
                onNavigateBack = { navController.popBackStack() },
                onEditar = { reporteId -> navController.navigate(Destinos.editar(reporteId)) }
            )
        }
    }
}
