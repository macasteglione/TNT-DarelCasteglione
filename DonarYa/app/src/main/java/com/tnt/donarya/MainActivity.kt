package com.tnt.donarya

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.places.api.Places
import com.tnt.donarya.data.GlobalNotificationObserver
import com.tnt.donarya.data.NotificationHelper
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.navigation.NavGraph
import com.tnt.donarya.navigation.Screen
import com.tnt.donarya.ui.theme.DonarYaTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    private var pendingNeedId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = "#1B4332".toColorInt()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        checkGooglePlayServices()
        initPlaces()
        askNotificationPermission()

        val user = UserRepositoryImpl.restoreSession()

        if (user != null) {
            GlobalNotificationObserver.start(this)
        }

        pendingNeedId = intent?.getStringExtra(NotificationHelper.EXTRA_NEED_ID)

        val startDestination = when (user?.rol) {
            UserRole.MERENDERO -> Screen.MerenderoHome.route
            UserRole.DONANTE -> Screen.MerenderoList.route
            else -> Screen.Onboarding.route
        }

        setContent {
            DonarYaTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    pendingNeedId = pendingNeedId,
                    onPendingNeedNavigated = { pendingNeedId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingNeedId = intent.getStringExtra(NotificationHelper.EXTRA_NEED_ID)
    }

    private fun checkGooglePlayServices() {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (result != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Log.w("PlayServices", "Google Play Services error: $result")
        } else {
            Log.i("PlayServices", "Google Play Services available")
        }
    }

    private fun initPlaces() {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, "AIzaSyAbW_ksLEUwypPpBQljcmLtew_QnoNuaXs")
                Log.i("PlacesAPI", "Places initialized")
            }
        } catch (e: Exception) {
            Log.e("PlacesAPI", "Failed to initialize Places", e)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
