package com.gk.habitstreak

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.gk.habitstreak.ui.HabitScreen

// Modern, energetic color palette
private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),        // Indigo 600
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF), // Indigo 50
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0EA5E9),      // Sky 500
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),     // Slate 50
    onBackground = Color(0xFF0F172A),   // Slate 900
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),  // Slate 100
    onSurfaceVariant = Color(0xFF64748B), // Slate 500
    outline = Color(0xFFCBD5E1),        // Slate 300
    outlineVariant = Color(0xFFE2E8F0)   // Slate 200
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),        // Indigo 400
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    background = Color(0xFF0F172A),     // Slate 900
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),        // Slate 800
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),  // Slate 700
    onSurfaceVariant = Color(0xFF94A3B8), // Slate 400
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
@Preview
fun App(
    onTriggerReminder: ((title: String, message: String) -> Unit)? = null
) {
    val darkTheme = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors
    ) {
        HabitScreen(onTriggerReminder = onTriggerReminder)
    }
}