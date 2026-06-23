package com.tnt.donarya.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole

import com.tnt.donarya.presentation.ui.LoginScreen
import com.tnt.donarya.presentation.ui.MerenderoDetailScreen
import com.tnt.donarya.presentation.ui.MerenderoHomeScreen
import com.tnt.donarya.presentation.ui.MerenderoListScreen
import com.tnt.donarya.presentation.ui.OnboardingScreen
import com.tnt.donarya.presentation.ui.ProfileScreen
import com.tnt.donarya.presentation.ui.PublishNeedScreen
import com.tnt.donarya.presentation.ui.RegisterScreen
import com.tnt.donarya.presentation.ui.NotificationsScreen
import com.tnt.donarya.presentation.ui.MapPickerScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.presentation.viewmodel.RegisterViewModel

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    val registerViewModel: RegisterViewModel = viewModel()
    var updateAddressCallback: ((String) -> Unit)? = null

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
            
            // Si entramos sin rol (desde el login o similar), limpiar estado previo
            LaunchedEffect(rolStr) {
                if (rolStr.isBlank()) {
                    registerViewModel.resetState()
                }
            }

            RegisterScreen(
                rolInicial = rolInicial,
                viewModel = registerViewModel,
                onRegisterSuccess = { rol ->
                    val destino = if (rol == UserRole.MERENDERO)
                        Screen.MerenderoHome.route
                    else
                        Screen.MerenderoList.route
                    navController.navigate(destino) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onOpenMap = {
                    navController.navigate(Screen.MapPicker.route)
                },
                onBack = { navController.popBackStack() },
                onAddressSelected = { updateAddressCallback = it }
            )
        }

        composable(Screen.MapPicker.route) {
            MapPickerScreen(
                viewModel = registerViewModel,
                onAddressSelected = { updateAddressCallback?.invoke(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MerenderoList.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            val role = user?.rol ?: UserRole.DONANTE
            MerenderoListScreen(
                onNeedClick = { needId ->
                    navController.navigate(
                        Screen.MerenderoDetail.createRoute(needId)
                    )
                },
                onPerfil = {
                    navController.navigate(Screen.Profile.createRoute(role.name.lowercase())) {
                        popUpTo(Screen.MerenderoList.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                role = role,
                onNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(
            route = Screen.MerenderoDetail.route,
            arguments = listOf(navArgument("merenderoId") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("merenderoId") ?: ""
            MerenderoDetailScreen(
                needId = id,
                onBack = { navController.popBackStack() },
                onVoyParaAllá = { navController.popBackStack() }
            )
        }

        composable(Screen.PublishNeed.route) {
            val currentUser = UserRepositoryImpl.getCurrentUser()
            val merenderoId = currentUser?.merenderoId ?: ""

            PublishNeedScreen(
                onBack = { navController.popBackStack() },
                onPublish = { type, urgency, description, items, _ ->
                    navController.popBackStack()
                },
                initialWhatsapp = ""
            )
        }


        composable(Screen.MerenderoHome.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            val role = user?.rol ?: UserRole.MERENDERO
            MerenderoHomeScreen(
                onPublishNeed = { navController.navigate(Screen.PublishNeed.route) },
                onEditNeed = { needId ->
                    navController.navigate(Screen.EditNeed.createRoute(needId))
                },
                onPerfil = {
                    navController.navigate(Screen.Profile.createRoute(role.name.lowercase())) {
                        popUpTo(Screen.MerenderoHome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                role = role,
                onNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
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
                onEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                roleEnum = role,
                onNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            val user = UserRepositoryImpl.getCurrentUser()
            RegisterScreen(
                isEditMode = true,
                rolInicial = user?.rol,
                viewModel = registerViewModel,
                initialNombre = user?.nombre ?: "",
                initialEmail = user?.email ?: "",
                initialNombreComedor = user?.nombreComedor ?: "",
                initialWhatsapp = user?.whatsapp ?: "",
                initialDireccion = user?.direccion ?: "",
                onRegisterSuccess = { navController.popBackStack() },
                onOpenMap = {
                    navController.navigate(Screen.MapPicker.route)
                },
                onBack = { navController.popBackStack() }
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

        composable(
            route = Screen.EditNeed.route,
            arguments = listOf(
                navArgument("needId") { type = NavType.StringType }
            )
        ) { backStack ->
            val needId = backStack.arguments?.getString("needId") ?: ""
            PublishNeedScreen(
                needId = needId,
                onBack = { navController.popBackStack() },
                onPublish = { _, _, _, _, _ -> navController.popBackStack() },  // ← ignorás los parámetros
                initialWhatsapp = ""
            )
        }
    }

}
