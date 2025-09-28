package com.example.asaankisaan.ui.cropprices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asaankisaan.data.model.CropPriceData
import com.example.asaankisaan.data.model.AVAILABLE_CROPS
import com.example.asaankisaan.data.repository.CropPriceRepository
import com.example.asaankisaan.ui.theme.FarmlandGreen
import com.example.asaankisaan.ui.theme.NeonGreen
import com.example.asaankisaan.ui.theme.SkyBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPricesScreen(
    onBackClicked: () -> Unit,
    onNavigateToCropAnalytics: (String) -> Unit, // New callback for navigation
    userLatitude: Double = 31.5204, // Default to Lahore
    userLongitude: Double = 74.3587
) {
    val context = LocalContext.current
    val repository = remember { CropPriceRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var cropData by remember { mutableStateOf<List<CropPriceData>>(emptyList()) }
    var currentLocation by remember { mutableStateOf("Lahore") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            repository.loadCropData()
            currentLocation = repository.getClosestLocation(userLatitude, userLongitude)
            isLoading = false
        }
    }
    
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            SkyBlue, // #87CEEB
            FarmlandGreen // #98FB98
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Fixed height as per v0 code
                .clip(RoundedCornerShape(30.dp)), // Rounded edges as per v0 code
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0x80 / 255f)), // #80FFFFFF
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crop Prices",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📍 $currentLocation",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                
                IconButton(onClick = { /* TODO: Handle menu */ }) {
                    Icon(
                        imageVector = Icons.Default.Menu, // Assuming 'Menu' icon, as it was in the reference code, the image shows a different icon though
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            // Dynamic Crop Cards from CSV data
            val currentPrices = repository.getCurrentPricesForLocation(currentLocation)
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(AVAILABLE_CROPS) { crop ->
                    val cropPrice = currentPrices[crop.name]
                    if (cropPrice != null) {
                        val trend = repository.getPriceTrend(crop.name, currentLocation)
                        val trendColor = when (trend) {
                            "↑" -> NeonGreen
                            "↓" -> Color.Red
                            else -> Color.White
                        }
                        
                        CropPriceCard(
                            cropName = crop.displayName,
                            price = "PKR ${String.format("%.0f", cropPrice.predictedPricePkrPer40kg)} per 40kg",
                            trendColor = trendColor,
                            trendIcon = trend,
                            icon = crop.icon,
                            onClick = { onNavigateToCropAnalytics(crop.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CropPriceCard(
    cropName: String,
    price: String,
    trendColor: Color,
    trendIcon: String,
    icon: String = "🌾",
    onClick: () -> Unit // Added onClick lambda
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                width = 2.dp,
                color = NeonGreen, // Using NeonGreen for card border
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick), // Make the card clickable
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crop Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Crop Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cropName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = price,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            
            // Trend Indicator
            Text(
                text = trendIcon,
                color = trendColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropPricesScreenPreview() {
    MaterialTheme {
        CropPricesScreen(onBackClicked = {}, onNavigateToCropAnalytics = {}) // Provide placeholder lambda
    }
}