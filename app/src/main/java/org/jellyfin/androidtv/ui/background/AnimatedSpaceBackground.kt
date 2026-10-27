package org.jellyfin.androidtv.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Orion Media animated space background.
 * Renders a deep-space scene with twinkling stars, a glowing sun,
 * and orbiting planets. Used as the fallback background when no
 * dynamic server background is available.
 */
@Composable
fun AnimatedSpaceBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "space")

    // Master rotation for orbits
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    // Twinkle phase
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    // Slow drift for stars
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    // Planet data (relative orbit radius, size, color, speed multiplier)
    val planets = remember {
        listOf(
            Planet(0.18f, 0.012f, Color(0xFFFFB347), 1.0f),   // Mercury-like
            Planet(0.26f, 0.018f, Color(0xFF4FC3F7), 0.7f),   // Earth-like
            Planet(0.36f, 0.025f, Color(0xFFEF5350), 0.5f),   // Mars-like
            Planet(0.50f, 0.045f, Color(0xFFFFA726), 0.3f),   // Jupiter-like
            Planet(0.62f, 0.038f, Color(0xFFAB47BC), 0.22f),  // Saturn-like
        )
    }

    // Star field (deterministic seed for stability)
    val stars = remember {
        val rng = Random(42)
        List(180) {
            Star(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                size = rng.nextFloat() * 2.5f + 0.5f,
                brightness = rng.nextFloat() * 0.6f + 0.4f,
                twinkleSpeed = rng.nextFloat() * 2f + 0.5f,
                twinkleOffset = rng.nextFloat() * (2 * Math.PI).toFloat()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val maxOrbit = minOf(w, h) * 0.42f

        // Deep space gradient background
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0D1B3E),
                    Color(0xFF060D1F),
                    Color(0xFF020510),
                    Color(0xFF000000)
                ),
                center = Offset(cx, cy),
                radius = maxOf(w, h) * 0.8f
            )
        )

        // Nebula wisps
        drawNebula(w, h, cx, cy, drift)

        // Stars
        for (star in stars) {
            val twinkleVal = (sin(twinkle * star.twinkleSpeed + star.twinkleOffset) + 1f) / 2f
            val alpha = star.brightness * (0.5f + 0.5f * twinkleVal)
            val sx = star.x * w
            val sy = star.y * h
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(sx, sy)
            )
        }

        // Orbit rings
        for (planet in planets) {
            val orbitR = planet.orbitRadius * maxOrbit
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = orbitR,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }

        // Sun
        val sunRadius = maxOrbit * 0.06f
        // Sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF8E1).copy(alpha = 0.9f),
                    Color(0xFFFFD54F).copy(alpha = 0.5f),
                    Color(0xFFFF8F00).copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = sunRadius * 4f
            ),
            radius = sunRadius * 4f,
            center = Offset(cx, cy)
        )
        // Sun core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDE7),
                    Color(0xFFFFD54F),
                    Color(0xFFFF8F00)
                ),
                center = Offset(cx, cy),
                radius = sunRadius
            ),
            radius = sunRadius,
            center = Offset(cx, cy)
        )

        // Planets
        for (planet in planets) {
            val orbitR = planet.orbitRadius * maxOrbit
            val angle = orbitAngle * planet.speed + planet.orbitOffset
            val px = cx + orbitR * cos(angle)
            val py = cy + orbitR * sin(angle)
            val pr = planet.size * maxOrbit

            // Planet glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        planet.color.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(px, py),
                    radius = pr * 2.5f
                ),
                radius = pr * 2.5f,
                center = Offset(px, py)
            )
            // Planet body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        planet.color.copy(alpha = 0.9f),
                        planet.color.copy(alpha = 0.6f),
                        planet.color.copy(alpha = 0.4f)
                    ),
                    center = Offset(px - pr * 0.3f, py - pr * 0.3f),
                    radius = pr
                ),
                radius = pr,
                center = Offset(px, py)
            )
            // Saturn-like ring for the last planet
            if (planet === planets.last()) {
                drawCircle(
                    color = Color(0xFFCE93D8).copy(alpha = 0.4f),
                    radius = pr * 2.2f,
                    center = Offset(px, py),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = pr * 0.3f)
                )
            }
        }

        // Shooting star (occasional)
        val shootPhase = (drift * 7f) % 1f
        if (shootPhase < 0.15f) {
            val t = shootPhase / 0.15f
            val startX = w * 0.8f
            val startY = h * 0.15f
            val endX = startX - w * 0.3f * t
            val endY = startY + h * 0.25f * t
            drawLine(
                color = Color.White.copy(alpha = (1f - t) * 0.8f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}

private data class Planet(
    val orbitRadius: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val orbitOffset: Float = 0f
) {
    init {
        // Randomize starting angle per planet
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val twinkleSpeed: Float,
    val twinkleOffset: Float
)

private fun DrawScope.drawNebula(w: Float, h: Float, cx: Float, cy: Float, drift: Float) {
    val nebulaColors = listOf(
        Color(0xFF4A148C).copy(alpha = 0.08f),
        Color(0xFF1A237E).copy(alpha = 0.06f),
        Color(0xFF004D40).copy(alpha = 0.05f),
    )
    for ((i, color) in nebulaColors.withIndex()) {
        val offsetX = cx + (w * 0.15f * sin(drift * (i + 1) * 2f))
        val offsetY = cy + (h * 0.1f * cos(drift * (i + 2) * 1.5f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = Offset(offsetX, offsetY),
                radius = maxOf(w, h) * 0.4f
            ),
            radius = maxOf(w, h) * 0.4f,
            center = Offset(offsetX, offsetY)
        )
    }
}
