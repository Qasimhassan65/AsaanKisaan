package com.example.asaankisaan.data.model

data class CropPriceData(
    val year: Int,
    val weekNumber: Int,
    val date: String,
    val marketLocation: String,
    val commodity: String,
    val basePricePkrPer40kg: Double,
    val inflationRatePercent: Double,
    val predictedPricePkrPer40kg: Double
)

data class CropInfo(
    val name: String,
    val displayName: String,
    val icon: String = ""
)

data class LocationInfo(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class PricePoint(
    val date: String, 
    val price: Double,
    val basePrice: Double,
    val inflationRate: Double
)

data class HistoryItem(
    val date: String, 
    val price: String
)

// Available crops from CSV
val AVAILABLE_CROPS = listOf(
    CropInfo("Wheat", "Wheat", "🌾"),
    CropInfo("Rice", "Rice", "🍚"),
    CropInfo("Maize", "Maize", "🌽"),
    CropInfo("Cotton", "Cotton", "🌿"),
    CropInfo("Sugarcane", "Sugarcane", "🎋"),
    CropInfo("Vegetables", "Vegetables", "🥬")
)

// Available locations from CSV
val AVAILABLE_LOCATIONS = listOf(
    LocationInfo("Faisalabad", 31.4504, 73.1350),
    LocationInfo("Karachi", 24.8607, 67.0011),
    LocationInfo("Lahore", 31.5204, 74.3587),
    LocationInfo("Multan", 30.1575, 71.5249),
    LocationInfo("Peshawar", 34.0151, 71.5249),
    LocationInfo("Quetta", 30.1798, 66.9750)
)
