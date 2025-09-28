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
}
