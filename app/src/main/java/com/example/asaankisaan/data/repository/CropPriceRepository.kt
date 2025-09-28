package com.example.asaankisaan.data.repository

import android.content.Context
import com.example.asaankisaan.data.model.CropPriceData
import com.example.asaankisaan.data.model.PricePoint
import com.example.asaankisaan.data.model.AVAILABLE_LOCATIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

class CropPriceRepository(private val context: Context) {
    
    private var cropPriceData: List<CropPriceData> = emptyList()
    
    suspend fun loadCropData() = withContext(Dispatchers.IO) {
        try {
            val csvContent = context.assets.open("predicted_crop_prices.csv").bufferedReader().use { it.readText() }
            cropPriceData = parseCsvData(csvContent)
            cropPriceData
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun parseCsvData(csvContent: String): List<CropPriceData> {
        val lines = csvContent.trim().split("\n")
        val data = mutableListOf<CropPriceData>()
        
        // Skip header line
        for (i in 1 until lines.size) {
            val columns = lines[i].split(",")
            if (columns.size >= 8) {
                try {
                    data.add(
                        CropPriceData(
                            year = columns[0].trim().toInt(),
                            weekNumber = columns[1].trim().toInt(),
                            date = columns[2].trim(),
                            marketLocation = columns[3].trim(),
                            commodity = columns[4].trim(),
                            basePricePkrPer40kg = columns[5].trim().toDouble(),
                            inflationRatePercent = columns[6].trim().toDouble(),
                            predictedPricePkrPer40kg = columns[7].trim().toDouble()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return data
    }
    
    fun getClosestLocation(userLat: Double, userLon: Double): String {
        var closestLocation = AVAILABLE_LOCATIONS[0]
        var minDistance = Double.MAX_VALUE
        
        for (location in AVAILABLE_LOCATIONS) {
            val distance = calculateDistance(userLat, userLon, location.latitude, location.longitude)
            if (distance < minDistance) {
                minDistance = distance
                closestLocation = location
            }
        }
        
        return closestLocation.name
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    
    fun getCurrentPricesForLocation(location: String): Map<String, CropPriceData> {
        val currentPrices = mutableMapOf<String, CropPriceData>()
        
        // Get latest date for the location
        val latestDate = cropPriceData
            .filter { it.marketLocation == location }
            .maxByOrNull { it.date }?.date
        
        if (latestDate != null) {
            cropPriceData
                .filter { it.marketLocation == location && it.date == latestDate }
                .forEach { data ->
                    currentPrices[data.commodity] = data
                }
        }
        
        return currentPrices
    }
    
    fun getPriceHistoryForCropAndLocation(crop: String, location: String): List<PricePoint> {
        return cropPriceData
            .filter { it.commodity == crop && it.marketLocation == location }
            .sortedBy { it.date }
            .map { data ->
                PricePoint(
                    date = data.date,
                    price = data.predictedPricePkrPer40kg,
                    basePrice = data.basePricePkrPer40kg,
                    inflationRate = data.inflationRatePercent
                )
            }
    }
    
    fun getAllCrops(): List<String> {
        return cropPriceData.map { it.commodity }.distinct().sorted()
    }
    
    fun getAllLocations(): List<String> {
        return cropPriceData.map { it.marketLocation }.distinct().sorted()
    }
    
    fun getPriceTrend(crop: String, location: String): String {
        val history = getPriceHistoryForCropAndLocation(crop, location)
        if (history.size < 2) return "="
        
        val recent = history.takeLast(2)
        val currentPrice = recent[1].price
        val previousPrice = recent[0].price
        
        return when {
            currentPrice > previousPrice -> "↑"
            currentPrice < previousPrice -> "↓"
            else -> "="
        }
    }
    
    fun getWeeklyData(crop: String, location: String, currentWeek: Int, currentYear: Int): List<PricePoint> {
        val allData = getPriceHistoryForCropAndLocation(crop, location)
        
        // Get data for previous week, current week, and next week
        val weeklyData = mutableListOf<PricePoint>()
        
        // Previous week
        val prevWeek = if (currentWeek > 1) currentWeek - 1 else 52
        val prevYear = if (currentWeek > 1) currentYear else currentYear - 1
        
        // Current week
        val currWeek = currentWeek
        val currYear = currentYear
        
        // Next week
        val nextWeek = if (currentWeek < 52) currentWeek + 1 else 1
        val nextYear = if (currentWeek < 52) currentYear else currentYear + 1
        
        // Find data for each week
        listOf(
            Triple(prevWeek, prevYear, "Previous Week"),
            Triple(currWeek, currYear, "Current Week"),
            Triple(nextWeek, nextYear, "Next Week")
        ).forEach { (week, year, label) ->
            val weekData = allData.find { 
                val dataWeek = it.date.split("-")[1].toIntOrNull() ?: 0
                val dataYear = it.date.split("-")[0].toIntOrNull() ?: 0
                dataWeek == week && dataYear == year
            }
            
            if (weekData != null) {
                weeklyData.add(PricePoint(
                    date = "$label (Week $week)",
                    price = weekData.price,
                    basePrice = weekData.basePrice,
                    inflationRate = weekData.inflationRate
                ))
            }
        }
        
        return weeklyData
    }
    
    fun getCurrentWeekAndYear(): Pair<Int, Int> {
        // Get current week and year from the latest data
        val latestDate = cropPriceData.maxByOrNull { it.date }?.date
        if (latestDate != null) {
            val parts = latestDate.split("-")
            if (parts.size >= 2) {
                val year = parts[0].toIntOrNull() ?: 2024
                val week = parts[1].toIntOrNull() ?: 1
                return Pair(week, year)
            }
        }
        return Pair(1, 2024) // Default fallback
    }
    
    fun getCurrentYearData(crop: String, location: String, year: Int): List<PricePoint> {
        // Get all data for the current year, using predictions if actual data isn't available
        val yearData = cropPriceData
            .filter { 
                it.commodity == crop && 
                it.marketLocation == location && 
                it.year == year 
            }
            .sortedBy { it.weekNumber }
            .map { data ->
                PricePoint(
                    date = "Week ${data.weekNumber}",
                    price = data.predictedPricePkrPer40kg, // Use predicted price
                    basePrice = data.basePricePkrPer40kg,
                    inflationRate = data.inflationRatePercent
                )
            }
        
        return yearData
    }
    
    fun getNormalizedYearData(crop: String, location: String, year: Int): List<PricePoint> {
        val yearData = getCurrentYearData(crop, location, year)
        
        if (yearData.isEmpty()) return emptyList()
        
        // Calculate statistical measures for better curve visualization
        val prices = yearData.map { it.price }
        val mean = prices.average()
        val standardDeviation = kotlin.math.sqrt(prices.map { (it - mean) * (it - mean) }.average())
        
        // Apply logarithmic transformation to amplify variations
        val logPrices = prices.map { kotlin.math.ln(it) }
        val logMin = logPrices.minOrNull() ?: 0.0
        val logMax = logPrices.maxOrNull() ?: 1.0
        val logRange = logMax - logMin
        
        // Apply polynomial transformation for more dramatic curves
        return yearData.mapIndexed { index, point ->
            val normalizedIndex = index.toDouble() / (yearData.size - 1)
            
            // Apply logarithmic transformation
            val logPrice = kotlin.math.ln(point.price)
            val normalizedLogPrice = if (logRange > 0) {
                (logPrice - logMin) / logRange
            } else {
                0.5
            }
            
            // Apply polynomial curve transformation (creates S-curve effect)
            val polynomialFactor = 3 * normalizedLogPrice * normalizedLogPrice - 2 * normalizedLogPrice * normalizedLogPrice * normalizedLogPrice
            
            // Add seasonal variation based on week number
            val weekNumber = point.date.replace("Week ", "").toIntOrNull() ?: 1
            val seasonalVariation = kotlin.math.sin(2 * kotlin.math.PI * weekNumber / 52.0) * 0.1
            
            // Combine transformations for rich curve visualization
            val transformedPrice = polynomialFactor + seasonalVariation + normalizedIndex * 0.3
            
            // Scale to 0-100 range for consistent visualization
            val finalPrice = (transformedPrice * 80 + 10).coerceIn(0.0, 100.0)
            
            PricePoint(
                date = point.date,
                price = finalPrice,
                basePrice = point.basePrice,
                inflationRate = point.inflationRate
            )
        }
    }
    
    fun getCurveEnhancedData(crop: String, location: String, year: Int): List<PricePoint> {
        val yearData = getCurrentYearData(crop, location, year)
        
        if (yearData.isEmpty()) return emptyList()
        
        // Method 1: Z-score normalization with amplification
        val prices = yearData.map { it.price }
        val mean = prices.average()
        val stdDev = kotlin.math.sqrt(prices.map { (it - mean) * (it - mean) }.average())
        
        return yearData.mapIndexed { index, point ->
            // Z-score normalization
            val zScore = if (stdDev > 0) (point.price - mean) / stdDev else 0.0
            
            // Amplify variations using sigmoid function
            val sigmoidValue = 1.0 / (1.0 + kotlin.math.exp(-zScore * 2))
            
            // Add time-based curve component
            val timeComponent = kotlin.math.sin(2 * kotlin.math.PI * index / yearData.size)
            
            // Combine components for rich curve visualization
            val curveValue = (sigmoidValue * 60 + timeComponent * 20 + 20).coerceIn(0.0, 100.0)
            
            PricePoint(
                date = point.date,
                price = curveValue,
                basePrice = point.basePrice,
                inflationRate = point.inflationRate
            )
        }
    }
}
