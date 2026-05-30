package com.tnt.donarya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.navigation.NavGraph
import com.tnt.donarya.navigation.Screen
import com.tnt.donarya.ui.theme.DonarYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar storage
        UserRepositoryImpl.init(this)
        // Restaurar sesión
        val user = UserRepositoryImpl.restoreSession()


        val startDestination = when (user?.rol) {

            UserRole.MERENDERO ->
                Screen.MerenderoHome.route

            UserRole.DONANTE ->
                Screen.MerenderoList.route

            else ->
                Screen.Onboarding.route
        }

        setContent {

            DonarYaTheme {

                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}