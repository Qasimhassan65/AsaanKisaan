package com.example.asaankisaan

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.asaankisaan.ui.homescreen.HomeScreen
import com.example.asaankisaan.ui.diseasedetection.DiseaseDetectionScreen
import com.example.asaankisaan.ui.cropprices.CropPricesScreen
import com.example.asaankisaan.ui.cropprices.CropAnalyticsScreen
import com.example.asaankisaan.ui.theme.AsaanKisaanTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val PREFS_NAME = "AsaanKisaanPrefs"
    private val LANGUAGE_KEY = "app_language"

    // Save language preference
    private fun saveLanguagePreference(context: Context, langCode: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, langCode).apply()
    }

    // Get language preference (default = English)
    private fun getLanguagePreference(context: Context): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    // Set app locale
    private fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config: Configuration = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Update activity context
        val activityContext = (context as? ComponentActivity)?.baseContext ?: context
        val newConfig = Configuration(activityContext.resources.configuration)
        newConfig.setLocale(locale)
        activityContext.resources.updateConfiguration(
            newConfig,
            activityContext.resources.displayMetrics
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply saved language preference BEFORE setContent
        val savedLanguage = getLanguagePreference(this)
        setAppLocale(this, savedLanguage)

        setContent {
            val currentContext = LocalContext.current
            LaunchedEffect(savedLanguage) {
                setAppLocale(currentContext, savedLanguage)
            }

            AsaanKisaanTheme {
                val context = LocalContext.current
                var allPermissionsGranted by remember { mutableStateOf(false) }

                val permissionsToRequest = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
                ).apply {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    } else {
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }.toTypedArray()

                val requestPermissionLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions: Map<String, Boolean> ->
                        allPermissionsGranted = permissions.entries.all { it.value }
                    }

                LaunchedEffect(Unit) {
                    val currentPermissions = permissionsToRequest.all {
                        ContextCompat.checkSelfPermission(context, it) ==
                                PackageManager.PERMISSION_GRANTED
                    }

                    if (currentPermissions) {
                        allPermissionsGranted = true
                    } else {
                        requestPermissionLauncher.launch(permissionsToRequest)
                    }
                }

                val onLanguageSelected: (String) -> Unit = { langCode ->
                    saveLanguagePreference(context, langCode)
                    setAppLocale(context, langCode)
                    recreate()
                }

                if (allPermissionsGranted) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "homeScreen"
                    ) {
                        composable("homeScreen") {
                            HomeScreen(
                                onNavigateToDiseaseDetection = {
                                    navController.navigate("diseaseDetectionScreen")
                                },
                                onLanguageSelected = onLanguageSelected,
                                currentLanguage = getLanguagePreference(context),
                                onNavigateToCropPrices = {
                                    navController.navigate("cropPricesScreen")
                                }
                            )
                        }
                        composable("diseaseDetectionScreen") {
                            DiseaseDetectionScreen(
                                onBackClicked = { navController.popBackStack() }
                            )
                        }
                        composable("cropPricesScreen") {
                            CropPricesScreen(
                                onBackClicked = { navController.popBackStack() },
                                onNavigateToCropAnalytics = { cropName ->
                                    navController.navigate("cropAnalyticsScreen/$cropName")
                                }
                            )
                        }
                        composable(
                            "cropAnalyticsScreen/{cropName}",
                            arguments = listOf(
                                navArgument("cropName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val cropName =
                                backStackEntry.arguments?.getString("cropName") ?: "Unknown Crop"
                            CropAnalyticsScreen(
                                cropName = cropName,
                                onBackClicked = { navController.popBackStack() }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Requesting Permissions...")
                    }
                }
            }
        }
    }
}
