package com.example.asaankisaan.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val name: String
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class Main(
    val temp: Float,
    @SerialName("feels_like")
    val feelsLike: Float,
    @SerialName("temp_min")
    val tempMin: Float,
    @SerialName("temp_max")
    val tempMax: Float,
    val pressure: Int,
    val humidity: Int
)

class WeatherService {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): WeatherResponse? {
        return try {
            httpClient.get("https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&appid=${apiKey}&units=metric").body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun mapWeatherIconToImageVector(iconCode: String): ImageVector {
    return when (iconCode) {
        "01d" -> Icons.Default.WbSunny // clear sky day
        "01n" -> Icons.Default.ModeNight // clear sky night
        "02d" -> Icons.Default.CloudQueue // few clouds day
        "02n" -> Icons.Default.BrokenImage // few clouds night
        "03d", "03n" -> Icons.Default.Cloud // scattered clouds
        "04d", "04n" -> Icons.Default.CloudCircle // broken clouds
        "09d", "09n" -> Icons.Default.Grain // shower rain
        "10d" -> Icons.Default.WbCloudy // rain day
        "10n" -> Icons.Default.Shower // rain night
        "11d", "11n" -> Icons.Default.FlashOn // thunderstorm
        "13d", "13n" -> Icons.Default.AcUnit // snow
        "50d", "50n" -> Icons.Default.BlurOn // mist
        else -> Icons.Default.CloudOff // default or unknown
    }
}
