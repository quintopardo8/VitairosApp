package com.vitairos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.vitairos.app.data.local.AppDatabase
import com.vitairos.app.repository.ReporteRepository
import com.vitairos.app.ui.navigation.AppNavGraph
import com.vitairos.app.ui.theme.VitairosAppTheme
import com.vitairos.app.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //instanciar persistencia local
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ReporteRepository(database.reporteDao())

        //instanciar viewModel con repositorio
        val viewModel = ReportViewModel(repository)

        //renderizado tema corporativo y grafo de pantallas
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