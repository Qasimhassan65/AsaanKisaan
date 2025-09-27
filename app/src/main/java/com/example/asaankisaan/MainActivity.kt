package com.example.asaankisaan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.asaankisaan.ui.homescreen.HomeScreen
import com.example.asaankisaan.ui.theme.AsaanKisaanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsaanKisaanTheme {
                HomeScreen()
            }
        }
    }
}