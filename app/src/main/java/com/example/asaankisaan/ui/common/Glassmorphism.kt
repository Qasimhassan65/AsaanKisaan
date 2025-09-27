package com.example.asaankisaan.ui.common

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

val TranslucentWhite = Color(0x99FFFFFF)

fun Modifier.glassmorphism() = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val blurEffect = RenderEffect.createBlurEffect(16f, 16f, Shader.TileMode.MIRROR)
        this
            .graphicsLayer {
                renderEffect = blurEffect.asComposeRenderEffect()
            }
            .background(
                color = TranslucentWhite,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    } else {
        this
            .background(
                color = TranslucentWhite,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    }
}