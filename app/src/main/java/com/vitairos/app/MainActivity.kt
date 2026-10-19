package com.vitairos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.vitairos.app.data.local.AppDatabase
import com.vitairos.app.data.network.ConexionObserver
import com.vitairos.app.repository.ReporteRepository
import com.vitairos.app.ui.navigation.AppNavGraph
import com.vitairos.app.ui.theme.VitairosAppTheme
import com.vitairos.app.viewmodel.ReportViewModel
import com.vitairos.app.viewmodel.ReportViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ReportViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        ReportViewModelFactory(
            repository = ReporteRepository(database.reporteDao()),
            conexion = ConexionObserver(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VitairosAppTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
