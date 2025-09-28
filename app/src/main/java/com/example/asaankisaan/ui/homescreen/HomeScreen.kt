package com.example.asaankisaan.ui.homescreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.* 
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector 
import androidx.core.content.ContextCompat
import com.example.asaankisaan.R
import com.example.asaankisaan.data.WeatherResponse
import com.example.asaankisaan.data.WeatherService
import com.example.asaankisaan.data.mapWeatherIconToImageVector
import com.example.asaankisaan.ui.theme.DarkGreenPrimary
import com.example.asaankisaan.ui.theme.LightGreen
import com.example.asaankisaan.ui.theme.SkyBlue
import com.example.asaankisaan.ui.common.TranslucentWhite
import com.example.asaankisaan.ui.common.glassmorphism
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import android.util.Log // Import for logging
import androidx.compose.ui.res.stringResource // Import for string resources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiseaseDetection: () -> Unit,
    onLanguageSelected: (String) -> Unit, // New language selection callback
    currentLanguage: String // Current language code (e.g., "en", "ur")
) {
    val gradientColors = listOf(
        LightGreen, // Light green
        SkyBlue  // Sky blue
    )

    val context = LocalContext.current
    val weatherService = remember { WeatherService() }
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationCallback = remember { mutableStateOf<LocationCallback?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        Log.d("WeatherAppDebug", "Permissions granted: $locationPermissionGranted")
        if (locationPermissionGranted) {
            // Permissions granted, try to get location
            // Request location updates if permissions are granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Log.d("WeatherAppDebug", "Requesting location updates...")
                val newLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            Log.d("WeatherAppDebug", "Location received: Lat=${location.latitude}, Lon=${location.longitude}")
                            coroutineScope.launch {
                                weatherData = weatherService.getCurrentWeather(
                                    location.latitude,
                                    location.longitude,
                                    "d6615659812e59e87f7d010163015822"
                                )
                                Log.d("WeatherAppDebug", "Weather data fetched: ${weatherData?.name}")
                            }
                            fusedLocationClient.removeLocationUpdates(this) // Stop updates after first location
                            Log.d("WeatherAppDebug", "Location updates removed.")
                        } ?: Log.e("WeatherAppDebug", "LocationResult had no lastLocation.")
                    }
                }
                locationCallback.value = newLocationCallback

                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000
                )
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000)
                    .setMaxUpdateDelayMillis(10000)
                    .build()

                fusedLocationClient.requestLocationUpdates(locationRequest, newLocationCallback, Looper.getMainLooper())
            } else {
                Log.e("WeatherAppDebug", "Permissions not actually granted despite result callback saying so.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationPermission || coarseLocationPermission) {
            locationPermissionGranted = true
            Log.d("WeatherAppDebug", "Initial check: Permissions already granted.")
            // Manually trigger location request if permissions were already granted
            // This block is effectively re-running the logic inside the launcher callback
            // if permissions are already there, ensuring weather data is fetched.
            val newLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        Log.d("WeatherAppDebug", "Location received (initial): Lat=${location.latitude}, Lon=${location.longitude}")
                        coroutineScope.launch {
                            weatherData = weatherService.getCurrentWeather(
                                location.latitude,
                                location.longitude,
                                "d6615659812e59e87f7d010163015822"
                            )
                            Log.d("WeatherAppDebug", "Weather data fetched (initial): ${weatherData?.name}")
                        }
                        fusedLocationClient.removeLocationUpdates(this) // Stop updates after first location
                        Log.d("WeatherAppDebug", "Location updates removed (initial).")
                    } ?: Log.e("WeatherAppDebug", "LocationResult had no lastLocation (initial).")
                }
            }
            locationCallback.value = newLocationCallback

            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, newLocationCallback, Looper.getMainLooper())


        } else {
            Log.d("WeatherAppDebug", "Initial check: Requesting permissions.")
            // Request permissions if not granted
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    DisposableEffect(fusedLocationClient) {
        onDispose {
            Log.d("WeatherAppDebug", "HomeScreen disposed, removing location updates.")
            locationCallback.value?.let { 
                fusedLocationClient.removeLocationUpdates(it)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row with App Info and Language Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Info Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Plant Icon
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = stringResource(R.string.plant_icon_description),
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Weather Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val temperature = weatherData?.main?.temp?.toInt()
                            Text(
                                text = if (temperature != null) "$temperature°C" else stringResource(R.string.default_temperature),
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = weatherData?.weather?.firstOrNull()?.icon?.let { mapWeatherIconToImageVector(it) } ?: Icons.Default.CloudOff,
                                contentDescription = stringResource(R.string.weather_icon_description),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = weatherData?.name ?: stringResource(R.string.loading_text),
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Language Selector (Replaced with ExposedDropdownMenuBox)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val languageOptions = listOf("en", "ur")
                    val displayLanguage = when (currentLanguage) {
                        "en" -> stringResource(R.string.language_english)
                        "ur" -> stringResource(R.string.language_urdu)
                        else -> stringResource(R.string.language_english) // Default to English display
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        },
                        modifier = Modifier.width(IntrinsicSize.Max) // Make dropdown content width fit
                    ) {
                        Card(
                            modifier = Modifier
                                .menuAnchor()
                                .width(IntrinsicSize.Max),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = displayLanguage,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.dropdown_icon_description),
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languageOptions.forEach { langCode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = when (langCode) {
                                            "en" -> stringResource(R.string.language_english)
                                            "ur" -> stringResource(R.string.language_urdu)
                                            else -> ""
                                        })
                                    },
                                    onClick = {
                                        onLanguageSelected(langCode)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Favorite Crops Prices Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.favorite_crops_prices_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Wheat Price
                    CropPriceRow(
                        cropName = stringResource(R.string.crop_wheat),
                        price = stringResource(R.string.price_wheat),
                        showProgress = false
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Cotton Price with Progress
                    CropPriceRow(
                        cropName = stringResource(R.string.crop_cotton),
                        price = stringResource(R.string.price_cotton),
                        showProgress = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Rice Price
                    CropPriceRow(
                        cropName = stringResource(R.string.crop_rice),
                        price = stringResource(R.string.price_rice),
                        showProgress = false
                    )
                }
            }
            
            // Feature Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Disease Prediction Card
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.disease_prediction_title),
                    subtitle = stringResource(R.string.disease_prediction_subtitle),
                    onClick = onNavigateToDiseaseDetection // Call the lambda here
                )
                
                // Helpline Card
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Phone,
                    title = stringResource(R.string.helpline_title),
                    subtitle = stringResource(R.string.helpline_subtitle)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Navigation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(25.dp) 
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.home_icon_description),
                        tint = DarkGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = stringResource(R.string.chat_icon_description),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_icon_description),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Decorative Diamond Shape
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun CropPriceRow(
    cropName: String,
    price: String,
    showProgress: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = cropName,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = price,
                color = Color.Black,
                fontSize = 16.sp
            )
            
            if (showProgress) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkGreenPrimary)
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null // Added optional onClick lambda
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = if (title == "Helpline") DarkGreenPrimary else Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        // Providing placeholder lambdas and a default language for preview
        HomeScreen(
            onNavigateToDiseaseDetection = {},
            onLanguageSelected = {},
            currentLanguage = "en"
        )
    }
}