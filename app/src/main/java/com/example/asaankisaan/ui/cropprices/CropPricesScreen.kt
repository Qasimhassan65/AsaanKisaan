package com.example.asaankisaan.ui.cropprices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asaankisaan.ui.theme.FarmlandGreen
import com.example.asaankisaan.ui.theme.NeonGreen
import com.example.asaankisaan.ui.theme.SkyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPricesScreen(
    onBackClicked: () -> Unit,
    onNavigateToCropAnalytics: (String) -> Unit // New callback for navigation
) {
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
                
                Text(
                    text = "Crop Prices",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
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
        
        // Crop Cards
        CropPriceCard(
            cropName = "Wheat",
            price = "PKR 3,200 per 40kg",
            trendColor = NeonGreen, // Using NeonGreen for trend color
            trendIcon = "↑",
            onClick = { onNavigateToCropAnalytics("Wheat") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CropPriceCard(
            cropName = "Rice",
            price = "PKR 2,800 per 40kg",
            trendColor = Color.Red, // Using Red for downward trend
            trendIcon = "↓",
            onClick = { onNavigateToCropAnalytics("Rice") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CropPriceCard(
            cropName = "Corn",
            price = "PKR 2,500 per 40kg",
            trendColor = Color.White, // Using White for no change
            trendIcon = "=",
            onClick = { onNavigateToCropAnalytics("Corn") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CropPriceCard(
            cropName = "Cotton",
            price = "PKR 2,500 per 40kg",
            trendColor = Color.White,
            trendIcon = "=",
            onClick = { onNavigateToCropAnalytics("Cotton") }
        )
    }
}

@Composable
fun CropPriceCard(
    cropName: String,
    price: String,
    trendColor: Color,
    trendIcon: String,
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
                    text = when(cropName) {
                        "Wheat" -> "🌾"
                        "Rice" -> "🍚" // Changed to reflect rice icon in image
                        "Corn" -> "🌽"
                        "Cotton" -> "\uD83C\uDF32" // Using unicode for cotton plant icon
                        else -> "🌾"
                    },
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