package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.VioletAccent
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    height: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val gradient = remember {
        Brush.linearGradient(
            colors = listOf(CyanAccent, VioletAccent, IndigoLight)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barWidth = (totalWidth / (barCount * 1.6f)).coerceAtLeast(3f)
            val barSpacing = (totalWidth - (barWidth * barCount)) / (barCount + 1)

            val centerY = totalHeight / 2f

            for (i in 0 until barCount) {
                val x = barSpacing + i * (barWidth + barSpacing)

                val calculatedHeightRatio: Float = when {
                    isListening -> {
                        // Dynamically scale with RMS volume
                        val wave = (sin(phase + i * 0.4) * 0.5f + 0.5f).toFloat()
                        val dynamicBoost = rmsLevel * 0.8f
                        (0.15f + wave * 0.35f + dynamicBoost).coerceIn(0.1f, 1.0f)
                    }
                    isSpeaking -> {
                        // Pulsing rhythmic speech rhythm
                        val wave1 = sin(phase * 1.5 + i * 0.5).toFloat()
                        val wave2 = sin(phase * 0.8 - i * 0.3).toFloat()
                        val combined = ((wave1 + wave2) / 2f * 0.4f + 0.5f)
                        combined.coerceIn(0.15f, 0.95f)
                    }
                    else -> {
                        // Gentle resting idle breathing wave
                        val idleWave = (sin(phase * 0.5 + i * 0.2) * 0.1f + 0.12f).toFloat()
                        idleWave
                    }
                }

                val barH = (totalHeight * calculatedHeightRatio).coerceAtLeast(6f)
                val top = centerY - (barH / 2f)

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
