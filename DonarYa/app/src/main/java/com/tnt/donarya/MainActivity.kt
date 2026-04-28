package com.tnt.donarya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.tnt.donarya.navigation.NavGraph
import com.tnt.donarya.ui.theme.DonarYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DonarYaTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}