package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import com.example.ui.viewmodel.AppTheme

// CLASSIC INDIGO (Light / M3 styles)
private val ClassicLightColors = lightColorScheme(
    primary = Color(0xFF1E3A8A), // deep indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1D4ED8),
    tertiary = Color(0xFFF59E0B), // cherry amber gold
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8)
)

// SAKURA PINK (Cherry Blossom / Soft Light)
private val SakuraLightColors = lightColorScheme(
    primary = Color(0xFFD81B60), // dark rose pink
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD1DC), // soft blush
    onPrimaryContainer = Color(0xFF4C0519),
    secondary = Color(0xFFEC407A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1F2),
    onSecondaryContainer = Color(0xFF9F1239),
    tertiary = Color(0xFF880E4F),
    onTertiary = Color.White,
    background = Color(0xFFFFF5F5),
    onBackground = Color(0xFF450A0A),
    surface = Color.White,
    onSurface = Color(0xFF450A0A),
    surfaceVariant = Color(0xFFFFE4E6),
    onSurfaceVariant = Color(0xFF9F1239),
    outline = Color(0xFFFDA4AF)
)

// ZEN MATCHA (Green Tea / Soothing Light)
private val ZenMatchaColors = lightColorScheme(
    primary = Color(0xFF2E7D32), // rich matcha green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDC8),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F8E9),
    onSecondaryContainer = Color(0xFF2E7D32),
    tertiary = Color(0xFFE6EE9C),
    onTertiary = Color(0xFF33691E),
    background = Color(0xFFF9FBE7),
    onBackground = Color(0xFF1B5E20),
    surface = Color.White,
    onSurface = Color(0xFF1B5E20),
    surfaceVariant = Color(0xFFF0F4C3),
    onSurfaceVariant = Color(0xFF33691E),
    outline = Color(0xFFC5E1A5)
)

// SAMURAI DARK (Armor charcoal / High-Contrast Dark)
private val SamuraiDarkColors = darkColorScheme(
    primary = Color(0xFFB39DDB), // neon violet
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF311B92),
    onPrimaryContainer = Color(0xFFEDE7F6),
    secondary = Color(0xFF80DEEA), // neon cyan
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF006064),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF121212),
    background = Color(0xFF121212),
    onBackground = Color(0xFFECEFF1),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFECEFF1),
    outline = Color(0xFF78909C)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.SAMURAI_DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.CLASSIC_INDIGO -> ClassicLightColors
        AppTheme.SAKURA_PINK -> SakuraLightColors
        AppTheme.ZEN_MATCHA -> ZenMatchaColors
        AppTheme.SAMURAI_DARK -> SamuraiDarkColors
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
