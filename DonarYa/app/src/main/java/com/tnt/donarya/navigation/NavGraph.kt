package com.tnt.donarya.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.presentation.ui.AlertsScreen
import com.tnt.donarya.presentation.ui.LoginScreen
import com.tnt.donarya.presentation.ui.MerenderoDetailScreen
import com.tnt.donarya.presentation.ui.MerenderoHomeScreen
import com.tnt.donarya.presentation.ui.MerenderoListScreen
import com.tnt.donarya.presentation.ui.OnboardingScreen
import com.tnt.donarya.presentation.ui.ProfileScreen
import com.tnt.donarya.presentation.ui.PublishNeedScreen
import com.tnt.donarya.presentation.ui.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController,startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(

                onMerenderoSelected = {navController.navigate(Screen.Register.route)},

                onDonanteSelected = {navController.navigate(Screen.Register.route)},

                onLogin = {navController.navigate(Screen.Login.route)},

            )
        }

        composable(Screen.MerenderoList.route) {
            MerenderoListScreen(
                onMerenderoClick = { id ->
                    navController.navigate(Screen.MerenderoDetail.createRoute(id))
                },
                onAlertas = {
                    navController.navigate(Screen.Alerts.route) {
                        popUpTo(Screen.MerenderoList.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onPerfil = {
                    navController.navigate(Screen.Profile.createRoute("donante")) {
                        popUpTo(Screen.MerenderoList.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
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
                onPerfil = { navController.navigate(Screen.Profile.createRoute("donante")) }
            )
        }

        composable(Screen.MerenderoHome.route) {
            MerenderoHomeScreen(
                onAlertas = {
                    navController.navigate(Screen.Alerts.route) {
                        popUpTo(Screen.MerenderoHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onPublishNeed = { navController.navigate(Screen.PublishNeed.route) },
                onPerfil = {
                    navController.navigate(Screen.Profile.createRoute("merendero")) {
                        popUpTo(Screen.MerenderoHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStack ->
            val role = backStack.arguments?.getString("role") ?: "donante"
            ProfileScreen(
                role = role,
                onAlertas = {
                    navController.navigate(Screen.Alerts.route)
                },
                onDonar = {
                    if (role == "merendero") {
                        navController.navigate(Screen.MerenderoHome.route)
                    } else {
                        navController.navigate(Screen.MerenderoList.route)
                    }
                },
                onLogout = {

                    navController.navigate(Screen.Onboarding.route) {

                        popUpTo(0) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
        // LOGIN Y REGISTER

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol ->
                    val destino = if (rol == UserRole.MERENDERO)
                        Screen.MerenderoHome.route
                    else
                        Screen.MerenderoList.route
                    navController.navigate(destino) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onRegistrate = { navController.navigate(Screen.Register.route) }
            )
        }

// Registro
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { rol ->
                    val destino = if (rol == UserRole.MERENDERO)
                        Screen.MerenderoHome.route
                    else
                        Screen.MerenderoList.route
                    navController.navigate(destino) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }




    }
}
