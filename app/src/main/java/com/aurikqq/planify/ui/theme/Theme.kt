package com.aurikqq.planify.ui.theme

import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PlanifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = remember(darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    val colors = animateColorScheme(colorScheme)

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}


@Composable
fun animateColorScheme(
    targetScheme: ColorScheme,
    durationMillis: Int = 400
): ColorScheme {
    val transition = updateTransition(targetState = targetScheme, label = "colorSchemeTransition")

    @Composable
    fun animate(target: (ColorScheme) -> Color, label: String) =
        transition.animateColor(
            transitionSpec = { tween(durationMillis) },
            label = label
        ) { target(it) }.value

    return ColorScheme(
        primary = animate({ it.primary }, "primary"),
        onPrimary = animate({ it.onPrimary }, "onPrimary"),
        primaryContainer = animate({ it.primaryContainer }, "primaryContainer"),
        onPrimaryContainer = animate({ it.onPrimaryContainer }, "onPrimaryContainer"),

        inversePrimary = animate({ it.inversePrimary }, "inversePrimary"),

        secondary = animate({ it.secondary }, "secondary"),
        onSecondary = animate({ it.onSecondary }, "onSecondary"),
        secondaryContainer = animate({ it.secondaryContainer }, "secondaryContainer"),
        onSecondaryContainer = animate({ it.onSecondaryContainer }, "onSecondaryContainer"),

        tertiary = animate({ it.tertiary }, "tertiary"),
        onTertiary = animate({ it.onTertiary }, "onTertiary"),
        tertiaryContainer = animate({ it.tertiaryContainer }, "tertiaryContainer"),
        onTertiaryContainer = animate({ it.onTertiaryContainer }, "onTertiaryContainer"),

        background = animate({ it.background }, "background"),
        onBackground = animate({ it.onBackground }, "onBackground"),

        surface = animate({ it.surface }, "surface"),
        onSurface = animate({ it.onSurface }, "onSurface"),
        surfaceVariant = animate({ it.surfaceVariant }, "surfaceVariant"),
        onSurfaceVariant = animate({ it.onSurfaceVariant }, "onSurfaceVariant"),

        surfaceTint = animate({ it.surfaceTint }, "surfaceTint"),

        inverseSurface = animate({ it.inverseSurface }, "inverseSurface"),
        inverseOnSurface = animate({ it.inverseOnSurface }, "inverseOnSurface"),

        outline = animate({ it.outline }, "outline"),
        outlineVariant = animate({ it.outlineVariant }, "outlineVariant"),

        error = animate({ it.error }, "error"),
        onError = animate({ it.onError }, "onError"),
        errorContainer = animate({ it.errorContainer }, "errorContainer"),
        onErrorContainer = animate({ it.onErrorContainer }, "onErrorContainer"),

        scrim = animate({ it.scrim }, "scrim"),

        surfaceBright = animate({ it.surfaceBright }, "surfaceBright"),
        surfaceDim = animate({ it.surfaceDim }, "surfaceDim"),

        surfaceContainer = animate({ it.surfaceContainer }, "surfaceContainer"),
        surfaceContainerLow = animate({ it.surfaceContainerLow }, "surfaceContainerLow"),
        surfaceContainerLowest = animate({ it.surfaceContainerLowest }, "surfaceContainerLowest"),
        surfaceContainerHigh = animate({ it.surfaceContainerHigh }, "surfaceContainerHigh"),
        surfaceContainerHighest = animate({ it.surfaceContainerHighest }, "surfaceContainerHighest"),

        primaryFixed = animate({ it.primaryFixed }, "primaryFixed"),
        primaryFixedDim = animate({ it.primaryFixedDim }, "primaryFixedDim"),
        onPrimaryFixed = animate({ it.onPrimaryFixed }, "onPrimaryFixed"),
        onPrimaryFixedVariant = animate({ it.onPrimaryFixedVariant }, "onPrimaryFixedVariant"),

        secondaryFixed = animate({ it.secondaryFixed }, "secondaryFixed"),
        secondaryFixedDim = animate({ it.secondaryFixedDim }, "secondaryFixedDim"),
        onSecondaryFixed = animate({ it.onSecondaryFixed }, "onSecondaryFixed"),
        onSecondaryFixedVariant = animate({ it.onSecondaryFixedVariant }, "onSecondaryFixedVariant"),

        tertiaryFixed = animate({ it.tertiaryFixed }, "tertiaryFixed"),
        tertiaryFixedDim = animate({ it.tertiaryFixedDim }, "tertiaryFixedDim"),
        onTertiaryFixed = animate({ it.onTertiaryFixed }, "onTertiaryFixed"),
        onTertiaryFixedVariant = animate({ it.onTertiaryFixedVariant }, "onTertiaryFixedVariant"),
    )
}
