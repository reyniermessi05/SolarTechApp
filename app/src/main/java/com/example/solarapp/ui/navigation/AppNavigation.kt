package com.example.solarapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.solarapp.ui.screens.FaultDetailScreen
import com.example.solarapp.ui.screens.FaultFinderScreen
import com.example.solarapp.ui.screens.HomeScreen
import com.example.solarapp.ui.screens.PlaceholderScreen
import com.example.solarapp.ui.screens.TroubleshootingScreen

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Solar Field Tool")
    object Troubleshooting : Screen("troubleshooting", "Troubleshooting")
    object Loto : Screen("loto", "LOTO")
    object Jha : Screen("jha", "JHA")
    object Plants : Screen("plants", "Plants")
    object Report : Screen("report", "Report")
    object FaultFinder : Screen("faultFinder/{model}", "Fault Finder") {
        fun createRoute(model: String) = "faultFinder/${Uri.encode(model)}"
    }
    object FaultDetail : Screen("faultDetail/{faultId}/{faultCode}", "Fault Detail") {
        fun createRoute(faultId: Int, faultCode: String) = "faultDetail/$faultId/${Uri.encode(faultCode)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Determine title dynamically
    val currentTitle = when {
        currentRoute == Screen.Home.route -> androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.title_home)
        currentRoute == Screen.Troubleshooting.route -> Screen.Troubleshooting.title
        currentRoute == Screen.Loto.route -> Screen.Loto.title
        currentRoute == Screen.Jha.route -> Screen.Jha.title
        currentRoute == Screen.Plants.route -> Screen.Plants.title
        currentRoute == Screen.Report.route -> Screen.Report.title
        currentRoute?.startsWith("faultFinder/") == true -> {
            val model = Uri.decode(navBackStackEntry?.arguments?.getString("model") ?: "")
            "Fault Finder - $model"
        }
        currentRoute?.startsWith("faultDetail/") == true -> {
            val faultCode = Uri.decode(navBackStackEntry?.arguments?.getString("faultCode") ?: "")
            faultCode
        }
        else -> androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.app_name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    if (currentRoute != Screen.Home.route) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screen.Home.route) {
                        IconButton(onClick = {
                            val currentLocales = AppCompatDelegate.getApplicationLocales()
                            val newLocale = if (currentLocales.isEmpty || currentLocales.get(0)?.language == "en") {
                                "es"
                            } else {
                                "en"
                            }
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                        }) {
                            Text(
                                text = "EN/ES",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTroubleshooting = { navController.navigate(Screen.Troubleshooting.route) },
                    onNavigateToLoto = { navController.navigate(Screen.Loto.route) },
                    onNavigateToJha = { navController.navigate(Screen.Jha.route) },
                    onNavigateToPlants = { navController.navigate(Screen.Plants.route) },
                    onNavigateToReport = { navController.navigate(Screen.Report.route) }
                )
            }
            composable(Screen.Troubleshooting.route) {
                TroubleshootingScreen(
                    onModelSelected = { model ->
                        navController.navigate(Screen.FaultFinder.createRoute(model))
                    }
                )
            }
            composable(Screen.Loto.route) {
                PlaceholderScreen(title = "LOTO Procedures")
            }
            composable(Screen.Jha.route) {
                PlaceholderScreen(title = "Job Hazard Analysis")
            }
            composable(Screen.Plants.route) {
                PlaceholderScreen(title = "Plant Details")
            }
            composable(Screen.Report.route) {
                PlaceholderScreen(title = "Reporting Tool")
            }
            composable(
                route = Screen.FaultFinder.route,
                arguments = listOf(navArgument("model") { type = NavType.StringType })
            ) { backStackEntry ->
                val model = Uri.decode(backStackEntry.arguments?.getString("model") ?: "")
                FaultFinderScreen(
                    model = model,
                    onNavigateToDetail = { faultId, faultCode ->
                        navController.navigate(Screen.FaultDetail.createRoute(faultId, faultCode))
                    }
                )
            }
            composable(
                route = Screen.FaultDetail.route,
                arguments = listOf(
                    navArgument("faultId") { type = NavType.IntType },
                    navArgument("faultCode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val faultId = backStackEntry.arguments?.getInt("faultId") ?: 0
                FaultDetailScreen(faultId = faultId)
            }
        }
    }
}
