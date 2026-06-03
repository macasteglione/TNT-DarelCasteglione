package com.tnt.donarya.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.NeedItem
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
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onMerenderoSelected = {
                    navController.navigate(Screen.Register.createRoute(UserRole.MERENDERO.name))
                },
                onDonanteSelected = {
                    navController.navigate(Screen.Register.createRoute(UserRole.DONANTE.name))
                },
                onLogin = { navController.navigate(Screen.Login.route) },
            )
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("rolInicial") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStack ->
            val rolStr = backStack.arguments?.getString("rolInicial") ?: ""
            val rolInicial = if (rolStr.isNotBlank()) UserRole.valueOf(rolStr) else null
            RegisterScreen(
                rolInicial = rolInicial,
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

        composable(Screen.MerenderoList.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            val role = user?.rol ?: UserRole.DONANTE
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
                    navController.navigate(Screen.Profile.createRoute(role.name.lowercase())) {
                        popUpTo(Screen.MerenderoList.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                role = role
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
            val currentUser = UserRepositoryImpl.getCurrentUser()
            val merenderoId = currentUser?.merenderoId

            PublishNeedScreen(
                onBack = { navController.popBackStack() },
                onPublish = { type, urgency, description, items, whatsapp ->
                    if (merenderoId != null) {
                        val need = NeedItem(
                            id = System.currentTimeMillis().toString(),
                            merenderoId = merenderoId,
                            title = items.firstOrNull() ?: "Necesidad",
                            description = description,
                            type = type,
                            urgency = urgency,
                            items = items,
                            publishedMinutesAgo = 0,
                            donorsOnWay = 0,
                            isCovered = false
                        )
                        NeedRepositoryImpl.add(merenderoId, need)

                        val merendero = MerenderoRepositoryImpl.getById(merenderoId)
                        if (merendero != null) {
                            val updated = merendero.copy(activeNeeds = merendero.activeNeeds + 1)
                            MerenderoRepositoryImpl.add(updated)
                        }
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Alerts.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            val role = user?.rol ?: UserRole.DONANTE
            AlertsScreen(
                onHome = { navController.navigate(Screen.MerenderoHome.route) },
                onDonar = { navController.navigate(Screen.MerenderoList.route) },
                onPerfil = {
                    navController.navigate(Screen.Profile.createRoute(role.name.lowercase()))
                },
                role = role
            )
        }

        composable(Screen.MerenderoHome.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            val role = user?.rol ?: UserRole.MERENDERO
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
                    navController.navigate(Screen.Profile.createRoute(role.name.lowercase())) {
                        popUpTo(Screen.MerenderoHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                role = role
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStack ->
            val roleStr = backStack.arguments?.getString("role") ?: "donante"
            val role = UserRepositoryImpl.getCurrentUser()?.rol
                ?: if (roleStr == "merendero") UserRole.MERENDERO else UserRole.DONANTE
            ProfileScreen(
                role = roleStr,
                onAlertas = {
                    navController.navigate(Screen.Alerts.route)
                },
                onHome = {
                    navController.navigate(Screen.MerenderoHome.route)
                },
                onDonar = {
                    navController.navigate(Screen.MerenderoList.route)
                },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                roleEnum = role
            )
        }

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
                onRegistrate = { navController.navigate(Screen.Register.createRoute("")) }
            )
        }
    }
}
