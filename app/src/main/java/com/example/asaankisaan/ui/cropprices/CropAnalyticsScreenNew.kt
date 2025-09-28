package com.example.asaankisaan.ui.cropprices

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asaankisaan.data.model.PricePoint
import com.example.asaankisaan.data.model.AVAILABLE_CROPS
import com.example.asaankisaan.data.repository.CropPriceRepository
import com.example.asaankisaan.ui.theme.FarmlandGreen
import com.example.asaankisaan.ui.theme.NeonGreen
import com.example.asaankisaan.ui.theme.SkyBlue
import kotlinx.coroutines.launch

data class HistoryItem(val date: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAnalyticsScreen(
    cropName: String,
    onBackClicked: () -> Unit,
    userLatitude: Double = 31.5204, // Default to Lahore
    userLongitude: Double = 74.3587
) {
    val context = LocalContext.current
    val repository = remember { CropPriceRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var chartData by remember { mutableStateOf<List<PricePoint>>(emptyList()) }
    var historyData by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var currentPrice by remember { mutableStateOf("Loading...") }
    var currentPricePer40kg by remember { mutableStateOf("Loading...") }
    var trendIcon by remember { mutableStateOf("=") }
    var trendColor by remember { mutableStateOf(Color.White) }
    var currentLocation by remember { mutableStateOf("Lahore") }
    var isLoading by remember { mutableStateOf(true) }
    
    // Get crop info
    val cropInfo = AVAILABLE_CROPS.find { it.name == cropName } ?: AVAILABLE_CROPS[0]
    val cropIcon = cropInfo.icon
    
    LaunchedEffect(cropName, userLatitude, userLongitude) {
        coroutineScope.launch {
            isLoading = true
            repository.loadCropData()
            currentLocation = repository.getClosestLocation(userLatitude, userLongitude)
            
            // Get price history for the crop and location
            val priceHistory = repository.getPriceHistoryForCropAndLocation(cropName, currentLocation)
            
            // Convert to chart data (limit to last 50 points for performance)
            chartData = priceHistory.takeLast(50).map { 
                PricePoint(it.date, it.price) 
            }
            
            // Get recent history for display
            historyData = priceHistory.takeLast(10).map { 
                HistoryItem(it.date, "PKR ${String.format("%.0f", it.price)}") 
            }.reversed()
            
            // Get current price and trend
            val latestPrice = priceHistory.lastOrNull()
            if (latestPrice != null) {
                currentPrice = "PKR ${String.format("%.0f", latestPrice.price)}"
                currentPricePer40kg = "PKR ${String.format("%.0f", latestPrice.price)} per 40kg"
            }
            
            trendIcon = repository.getPriceTrend(cropName, currentLocation)
            trendColor = when (trendIcon) {
                "↑" -> NeonGreen
                "↓" -> Color.Red
                else -> Color.White
            }
            
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
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0x80 / 255f)),
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
                        text = cropInfo.displayName,
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
                
                IconButton(onClick = { /* TODO: Handle info */ }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
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
            // Current Price Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Crop Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cropIcon,
                            fontSize = 32.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Price Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentPrice,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentPricePer40kg,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                    
                    // Trend Indicator
                    Text(
                        text = trendIcon,
                        color = trendColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Price Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Price Trend (Last 50 Weeks)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (chartData.isNotEmpty()) {
                        PriceChart(
                            data = chartData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No data available",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent History
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Recent History",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyData) { item ->
                            HistoryItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryItem) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.price,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.date,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PriceChart(
    data: List<PricePoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40.dp.toPx()
        
        val minPrice = data.minOf { it.price }
        val maxPrice = data.maxOf { it.price }
        val priceRange = maxPrice - minPrice
        
        val stepX = (canvasWidth - 2 * padding) / (data.size - 1)
        
        // Create path for the line
        val path = Path()
        val fillPath = Path()
        
        data.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val y = canvasHeight - padding - ((point.price - minPrice) / priceRange) * (canvasHeight - 2 * padding)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, canvasHeight - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        
        fillPath.lineTo(canvasWidth - padding, canvasHeight - padding)
        fillPath.close()
        
        // Draw filled area
        drawPath(
            path = fillPath,
            color = Color.White.copy(alpha = 0.2f)
        )
        
        // Draw line
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        
        // Draw data points
        data.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val y = canvasHeight - padding - ((point.price - minPrice) / priceRange) * (canvasHeight - 2 * padding)
            
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Draw price labels
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            
            // Min price
            canvas.nativeCanvas.drawText(
                String.format("%.0f", minPrice),
                padding,
                canvasHeight - padding + 30.dp.toPx(),
                paint
            )
            
            // Max price
            canvas.nativeCanvas.drawText(
                String.format("%.0f", maxPrice),
                padding,
                padding + 20.dp.toPx(),
                paint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropAnalyticsScreenPreview() {
    MaterialTheme {
        CropAnalyticsScreen(
            cropName = "Wheat",
            onBackClicked = {}
        )
    }
}
