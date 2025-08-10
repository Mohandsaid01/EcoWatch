package com.example.ecowatch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ecowatch.screens.SpeciesFormScreen
import com.example.ecowatch.screens.SpeciesListScreen

object Routes {
    const val LIST = "list"
    const val FORM = "form"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            SpeciesListScreen(
                onAddClick = { navController.navigate(Routes.FORM) },
                onOpenDetails = { /* plus tard */ }
            )
        }

        composable(Routes.FORM) {
            SpeciesFormScreen(
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
