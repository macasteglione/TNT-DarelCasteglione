package com.tnt.donarya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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

        UserRepositoryImpl.init(this)
        val user = UserRepositoryImpl.restoreSession()

        val startDestination = when (user?.rol) {
            UserRole.MERENDERO -> Screen.MerenderoHome.route
            UserRole.DONANTE -> Screen.MerenderoList.route
            else -> Screen.Onboarding.route
        }

        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as ComponentActivity).window
                    window.statusBarColor = Color(0xFF1B4332).toArgb()
                    WindowCompat.getInsetsController(window, view)
                        .isAppearanceLightStatusBars = false
                }
            }

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