package github.boxiaolanya2008.kc_tool.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ToolDarkColors = darkColorScheme(
    primary = ToolPrimaryDark,
    onPrimary = ToolOnPrimaryDark,
    primaryContainer = ToolPrimaryContainerDark,
    onPrimaryContainer = ToolOnPrimaryContainerDark,
    secondary = ToolSecondaryDark,
    onSecondary = ToolOnSecondaryDark,
    secondaryContainer = ToolSecondaryContainerDark,
    onSecondaryContainer = ToolOnSecondaryContainerDark,
    tertiary = ToolTertiaryDark,
    onTertiary = ToolOnTertiaryDark,
    tertiaryContainer = ToolTertiaryContainerDark,
    onTertiaryContainer = ToolOnTertiaryContainerDark,
    error = ToolErrorDark,
    onError = ToolOnErrorDark,
    errorContainer = ToolErrorContainerDark,
    onErrorContainer = ToolOnErrorContainerDark,
    background = ToolBackgroundDark,
    onBackground = ToolOnBackgroundDark,
    surface = ToolSurfaceDark,
    onSurface = ToolOnSurfaceDark,
    surfaceVariant = ToolSurfaceVariantDark,
    onSurfaceVariant = ToolOnSurfaceVariantDark,
    outline = ToolOutlineDark,
    outlineVariant = ToolOutlineVariantDark,
    scrim = ToolScrimDark,
    inverseSurface = ToolInverseSurfaceDark,
    inverseOnSurface = ToolInverseOnSurfaceDark,
    inversePrimary = ToolInversePrimaryDark,
    surfaceTint = ToolSurfaceTintDark,
    surfaceBright = ToolSurfaceBrightDark,
    surfaceDim = ToolSurfaceDimDark,
    surfaceContainerLowest = ToolSurfaceContainerLowestDark,
    surfaceContainerLow = ToolSurfaceContainerLowDark,
    surfaceContainer = ToolSurfaceContainerDark,
    surfaceContainerHigh = ToolSurfaceContainerHighDark,
    surfaceContainerHighest = ToolSurfaceContainerHighestDark
)

private val ToolLightColors = lightColorScheme(
    primary = ToolPrimaryLight,
    onPrimary = ToolOnPrimaryLight,
    primaryContainer = ToolPrimaryContainerLight,
    onPrimaryContainer = ToolOnPrimaryContainerLight,
    secondary = ToolSecondaryLight,
    onSecondary = ToolOnSecondaryLight,
    secondaryContainer = ToolSecondaryContainerLight,
    onSecondaryContainer = ToolOnSecondaryContainerLight,
    tertiary = ToolTertiaryLight,
    onTertiary = ToolOnTertiaryLight,
    tertiaryContainer = ToolTertiaryContainerLight,
    onTertiaryContainer = ToolOnTertiaryContainerLight,
    error = ToolErrorLight,
    onError = ToolOnErrorLight,
    errorContainer = ToolErrorContainerLight,
    onErrorContainer = ToolOnErrorContainerLight,
    background = ToolBackgroundLight,
    onBackground = ToolOnBackgroundLight,
    surface = ToolSurfaceLight,
    onSurface = ToolOnSurfaceLight,
    surfaceVariant = ToolSurfaceVariantLight,
    onSurfaceVariant = ToolOnSurfaceVariantLight,
    outline = ToolOutlineLight,
    outlineVariant = ToolOutlineVariantLight,
    scrim = ToolScrimLight,
    inverseSurface = ToolInverseSurfaceLight,
    inverseOnSurface = ToolInverseOnSurfaceLight,
    inversePrimary = ToolInversePrimaryLight,
    surfaceTint = ToolSurfaceTintLight,
    surfaceBright = ToolSurfaceBrightLight,
    surfaceDim = ToolSurfaceDimLight,
    surfaceContainerLowest = ToolSurfaceContainerLowestLight,
    surfaceContainerLow = ToolSurfaceContainerLowLight,
    surfaceContainer = ToolSurfaceContainerLight,
    surfaceContainerHigh = ToolSurfaceContainerHighLight,
    surfaceContainerHighest = ToolSurfaceContainerHighestLight
)

@Composable
fun KctoolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ToolDarkColors
        else -> ToolLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
