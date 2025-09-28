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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke // Import Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asaankisaan.ui.theme.FarmlandGreen
import com.example.asaankisaan.ui.theme.NeonGreen
import com.example.asaankisaan.ui.theme.SkyBlue

data class PricePoint(val date: String, val price: Int)
data class HistoryItem(val date: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAnalyticsScreen(
    cropName: String,
    onBackClicked: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            SkyBlue, // #87CEEB
            FarmlandGreen // #98FB98
        )
    )

    val chartData: List<PricePoint>
    val historyData: List<HistoryItem>
    val currentPrice: String
    val currentPricePer40kg: String
    val trendIcon: String
    val trendColor: Color
    val cropIcon: String

    when (cropName) {
        "Wheat" -> {
            chartData = listOf(
                PricePoint("May 1", 3400),
                PricePoint("May 8", 3450),
                PricePoint("May 15", 3500),
                PricePoint("May 22", 3550),
                PricePoint("May 29", 3600)
            )
            historyData = listOf(
                HistoryItem("May 29", "PKR 3600"),
                HistoryItem("May 28", "PKR 3550"),
                HistoryItem("May 27", "PKR 3500")
            )
            currentPrice = "PKR 3,600"
            currentPricePer40kg = "PKR 3,600 per 40kg"
            trendIcon = "↑"
            trendColor = NeonGreen
            cropIcon = "🌾"
        }
        "Rice" -> {
            chartData = listOf(
                PricePoint("May 1", 2800),
                PricePoint("May 8", 2750),
                PricePoint("May 15", 2700),
                PricePoint("May 22", 2650),
                PricePoint("May 29", 2600)
            )
            historyData = listOf(
                HistoryItem("May 29", "PKR 2600"),
                HistoryItem("May 28", "PKR 2650"),
                HistoryItem("May 27", "PKR 2700")
            )
            currentPrice = "PKR 2,600"
            currentPricePer40kg = "PKR 2,600 per 40kg"
            trendIcon = "↓"
            trendColor = Color.Red
            cropIcon = "🍚"
        }
        "Corn" -> {
            chartData = listOf(
                PricePoint("May 1", 2500),
                PricePoint("May 8", 2500),
                PricePoint("May 15", 2500),
                PricePoint("May 22", 2500),
                PricePoint("May 29", 2500)
            )
            historyData = listOf(
                HistoryItem("May 29", "PKR 2500"),
                HistoryItem("May 28", "PKR 2500"),
                HistoryItem("May 27", "PKR 2500")
            )
            currentPrice = "PKR 2,500"
            currentPricePer40kg = "PKR 2,500 per 40kg"
            trendIcon = "="
            trendColor = Color.White
            cropIcon = "🌽"
        }
        "Cotton" -> {
            chartData = listOf(
                PricePoint("May 1", 2900),
                PricePoint("May 8", 2950),
                PricePoint("May 15", 3000),
                PricePoint("May 22", 2980),
                PricePoint("May 29", 3000)
            )
            historyData = listOf(
                HistoryItem("May 29", "PKR 3000"),
                HistoryItem("May 28", "PKR 2980"),
                HistoryItem("May 27", "PKR 3000")
            )
            currentPrice = "PKR 3,000"
            currentPricePer40kg = "PKR 3,000 per 40kg"
            trendIcon = "↑"
            trendColor = NeonGreen
            cropIcon = "\uD83C\uDF32"
        }
        else -> { // Default data
            chartData = emptyList()
            historyData = emptyList()
            currentPrice = "N/A"
            currentPricePer40kg = "N/A"
            trendIcon = ""
            trendColor = Color.White
            cropIcon = ""
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "$cropName Prices",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(onClick = { /* Handle info */ }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Price Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 2.dp,
                        color = NeonGreen,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Crop Icon
                    Text(
                        text = cropIcon,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 20.dp)
                    )

                    // Price Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentPrice,
                            color = NeonGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentPricePer40kg,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }

                    // Trend Arrow
                    Text(
                        text = trendIcon,
                        color = trendColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Price Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 2.dp,
                        color = NeonGreen,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                if (chartData.isNotEmpty()) {
                    PriceChart(
                        data = chartData,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No chart data available", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price History Section
            Text(
                text = "Price History",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyData) { item ->
                    PriceHistoryCard(item)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compare Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 2.dp,
                        color = NeonGreen,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { /* TODO: Handle comparison */ },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Compare with Other Crops",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Decorative Diamond
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
fun PriceChart(
    data: List<PricePoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val horizontalPadding = 30.dp.toPx()
        val verticalPadding = 20.dp.toPx()
        val labelTextSize = 12.sp.toPx()

        val chartWidth = size.width - (2 * horizontalPadding)
        val chartHeight = size.height - (2 * verticalPadding)

        val prices = data.map { it.price }
        val minPrice = prices.minOrNull() ?: 0
        val maxPrice = prices.maxOrNull() ?: 1

        val priceRange = (maxPrice - minPrice).toFloat()
        if (priceRange == 0f) return@Canvas // Avoid division by zero if all prices are same

        // Draw Y-axis labels and horizontal grid lines
        val numYLabels = 4
        for (i in 0 until numYLabels) {
            val y = verticalPadding + (chartHeight / (numYLabels - 1)) * i
            val price = maxPrice - (priceRange / (numYLabels - 1)) * i
            val label = price.toInt().toString()

            drawContext.canvas.nativeCanvas.drawText(
                label,
                5.dp.toPx(),
                y + labelTextSize / 3, // Adjust for vertical centering
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = labelTextSize
                    alpha = 180
                }
            )

            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(horizontalPadding, y),
                end = Offset(size.width - horizontalPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw X-axis labels
        val xStep = chartWidth / (data.size - 1).toFloat()
        data.forEachIndexed { index, point ->
            val x = horizontalPadding + index * xStep
            drawContext.canvas.nativeCanvas.drawText(
                point.date,
                x - (labelTextSize * 2) / 2, // Approximate center alignment
                size.height - 5.dp.toPx(),
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = labelTextSize
                    alpha = 180
                }
            )
        }

        // Draw chart line and points
        val points = data.mapIndexed { index, point ->
            val x = horizontalPadding + index * xStep
            val normalizedPrice = (point.price - minPrice) / priceRange
            val y = verticalPadding + chartHeight - (normalizedPrice * chartHeight)
            Offset(x, y)
        }

        // Draw line segments
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = path,
            color = NeonGreen,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = NeonGreen,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun PriceHistoryCard(item: HistoryItem) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(60.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.date,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Text(
                text = item.price,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropAnalyticsScreenPreview() {
    MaterialTheme {
        CropAnalyticsScreen(cropName = "Wheat", onBackClicked = {})
    }
}
