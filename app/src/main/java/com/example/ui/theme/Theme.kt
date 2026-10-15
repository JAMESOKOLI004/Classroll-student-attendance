package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ClassRollShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp), // Requested 12px rounded cards
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val ClassRollLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueSubtle,
    onPrimaryContainer = PrimaryBlueDark,

    secondary = PrimaryBlueLight,
    onSecondary = Color.White,
    secondaryContainer = SurfaceSubtle,
    onSecondaryContainer = TextPrimary,

    tertiary = StatusPresentGreen,
    onTertiary = Color.White,
    tertiaryContainer = StatusPresentGreenSubtle,
    onTertiaryContainer = StatusPresentGreenDark,

    background = BackgroundSlate,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,

    outline = BorderSubtle,
    outlineVariant = DividerColor,

    error = StatusAbsentRed,
    onError = Color.White,
    errorContainer = StatusAbsentRedSubtle,
    onErrorContainer = StatusAbsentRedDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Keep branded #2563EB ClassRoll palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClassRollLightColorScheme,
        typography = Typography,
        shapes = ClassRollShapes,
        content = content
    )
}
