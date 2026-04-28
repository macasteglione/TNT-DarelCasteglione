package com.tnt.donarya.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tnt.donarya.ui.screens.AlertsScreen
import com.tnt.donarya.ui.screens.MerenderoDetailScreen
import com.tnt.donarya.ui.screens.MerenderoHomeScreen
import com.tnt.donarya.ui.screens.MerenderoListScreen
import com.tnt.donarya.ui.screens.OnboardingScreen
import com.tnt.donarya.ui.screens.ProfileScreen
import com.tnt.donarya.ui.screens.PublishNeedScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onMerenderoSelected = { navController.navigate(Screen.MerenderoHome.route) },
                onDonanteSelected = { navController.navigate(Screen.MerenderoList.route) },
                onLogin = { navController.navigate(Screen.MerenderoList.route) }
            )
        }

        composable(Screen.MerenderoList.route) {
            MerenderoListScreen(
                onMerenderoClick = { id ->
                    navController.navigate(
                        Screen.MerenderoDetail.createRoute(
                            id
                        )
                    )
                },
                onAlertas = { navController.navigate(Screen.Alerts.route) },
                onPerfil = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(
            route = Screen.MerenderoDetail.route,
            arguments = listOf(navArgument("merenderoId") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("merenderoId") ?: ""
            MerenderoDetailScreen(
                merenderoId = id,
                onBack = { navController.popBackStack() },
                onVoyParaAllá = { navController.popBackStack() }
            )
        }

        composable(Screen.PublishNeed.route) {
            PublishNeedScreen(
                onBack = { navController.popBackStack() },
                onPublish = { navController.popBackStack() }
            )
        }

        composable(Screen.Alerts.route) {
            AlertsScreen(
                onDonar = { navController.navigate(Screen.MerenderoList.route) },
                onPerfil = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.MerenderoHome.route) {
            MerenderoHomeScreen(
                onAlertas = { navController.navigate(Screen.Alerts.route) },
                onPublishNeed = { navController.navigate(Screen.PublishNeed.route) },
                onPerfil = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onAlertas = { navController.navigate(Screen.Alerts.route) },
                onDonar = { navController.navigate(Screen.MerenderoList.route) }
            )
        }
    }
}
