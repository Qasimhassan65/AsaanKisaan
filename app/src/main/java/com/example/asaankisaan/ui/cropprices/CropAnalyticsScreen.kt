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
    var weeklyData by remember { mutableStateOf<List<PricePoint>>(emptyList()) }
    var useCurveEnhanced by remember { mutableStateOf(true) }
    
    // Get crop info
    val cropInfo = AVAILABLE_CROPS.find { it.name == cropName } ?: AVAILABLE_CROPS[0]
    val cropIcon = cropInfo.icon
    
    LaunchedEffect(cropName, userLatitude, userLongitude, useCurveEnhanced) {
        coroutineScope.launch {
            isLoading = true
            repository.loadCropData()
            currentLocation = repository.getClosestLocation(userLatitude, userLongitude)
            
            // Get current year data
            val (currentWeek, currentYear) = repository.getCurrentWeekAndYear()
            
            // Get curve-enhanced data for the current year
            chartData = if (useCurveEnhanced) {
                repository.getCurveEnhancedData(cropName, currentLocation, currentYear)
            } else {
                repository.getNormalizedYearData(cropName, currentLocation, currentYear)
            }
            
            // Get weekly data for previous, current, and next week (for the price display)
            val rawWeeklyData = repository.getWeeklyData(cropName, currentLocation, currentWeek, currentYear)
            weeklyData = rawWeeklyData
            
            // Get recent history for display (show actual prices, not normalized)
            val priceHistory = repository.getPriceHistoryForCropAndLocation(cropName, currentLocation)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (useCurveEnhanced) {
                                "Current Year Price Trend (Curve Enhanced)"
                            } else {
                                "Current Year Price Trend (Normalized)"
                            },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Toggle button for curve visualization
                        Card(
                            modifier = Modifier.clickable { useCurveEnhanced = !useCurveEnhanced },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (useCurveEnhanced) "Curves" else "Lines",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                if (chartData.isNotEmpty()) {
                    PriceChart(
                        data = chartData,
                        modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show actual prices for current year
                        Text(
                            text = "Current Year Prices (Predicted):",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display actual prices for current year (sample weeks)
                        val (_, currentYear) = repository.getCurrentWeekAndYear()
                        val actualYearData = repository.getCurrentYearData(cropName, currentLocation, currentYear)
                        
                        // Show every 4th week for readability
                        actualYearData.filterIndexed { index, _ -> index % 4 == 0 || index == actualYearData.size - 1 }
                            .take(8) // Limit to 8 entries
                            .forEach { weekData ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = weekData.date,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "PKR ${String.format("%.0f", weekData.price)}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
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
        
        val stepX = if (data.size > 1) (canvasWidth - 2 * padding) / (data.size - 1) else 0f
        
        // Calculate points
        val points = data.mapIndexed { index, point ->
            val x = padding + index * stepX
            val y = if (priceRange > 0) {
                canvasHeight - padding - ((point.price - minPrice) / priceRange) * (canvasHeight - 2 * padding)
            } else {
                canvasHeight / 2
            }
            Offset(x.toFloat(), y.toFloat())
        }
        
        // Create smooth curve using cubic bezier
        val curvePath = Path()
        val fillPath = Path()
        
        if (points.isNotEmpty()) {
            curvePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, canvasHeight - padding)
            fillPath.lineTo(points[0].x, points[0].y)
            
            for (i in 1 until points.size) {
                val prevPoint = points[i - 1]
                val currentPoint = points[i]
                
                // Calculate control points for smooth curves with enhanced curvature
                val midX = (prevPoint.x + currentPoint.x) / 2
                val midY = (prevPoint.y + currentPoint.y) / 2
                
                // Enhanced control points for more dramatic curves
                val curvatureFactor = 0.4f
                val controlPoint1X = prevPoint.x + (currentPoint.x - prevPoint.x) * curvatureFactor
                val controlPoint1Y = prevPoint.y + (currentPoint.y - prevPoint.y) * curvatureFactor + 
                    kotlin.math.sin(i * 0.5f) * 20f // Add wave component
                val controlPoint2X = currentPoint.x - (currentPoint.x - prevPoint.x) * curvatureFactor
                val controlPoint2Y = currentPoint.y - (currentPoint.y - prevPoint.y) * curvatureFactor + 
                    kotlin.math.cos(i * 0.5f) * 15f // Add wave component
                
                curvePath.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    currentPoint.x, currentPoint.y
                )
                
                fillPath.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    currentPoint.x, currentPoint.y
                )
            }
            
            fillPath.lineTo(points.last().x, canvasHeight - padding)
            fillPath.close()
        }
        
        // Draw filled area with gradient
        drawPath(
            path = fillPath,
            color = Color.White.copy(alpha = 0.15f)
        )
        
        // Draw main curve
        drawPath(
            path = curvePath,
            color = Color.White,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        
        // Draw data points with different sizes
        points.forEachIndexed { index, point ->
            val radius = if (index == 0 || index == points.size - 1) 6.dp.toPx() else 4.dp.toPx()
            val alpha = if (index == 0 || index == points.size - 1) 1f else 0.8f
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = point
            )
            
            // Add inner circle for emphasis
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = point
            )
        }
        
        // Draw labels
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            
            // Y-axis labels
            canvas.nativeCanvas.drawText(
                "Low",
                padding.toFloat(),
                canvasHeight - padding + 25.dp.toPx(),
                paint
            )
            
            canvas.nativeCanvas.drawText(
                "High",
                padding.toFloat(),
                padding + 15.dp.toPx(),
                paint
            )
            
            // X-axis labels (show every few weeks for readability)
            val weekLabelPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 10.sp.toPx()
                textAlign = Paint.Align.CENTER
            }
            
            val labelInterval = maxOf(1, data.size / 8) // Show max 8 labels
            
            data.forEachIndexed { index, point ->
                if (index % labelInterval == 0 || index == data.size - 1) {
                    val x = padding + index * stepX
                    val weekNumber = point.date.replace("Week ", "").toIntOrNull() ?: (index + 1)
                    
                    canvas.nativeCanvas.drawText(
                        "W$weekNumber",
                        x,
                        canvasHeight - padding + 35.dp.toPx(),
                        weekLabelPaint
                    )
                }
            }
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
