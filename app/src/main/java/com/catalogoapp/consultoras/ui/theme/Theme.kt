package com.catalogoapp.consultoras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFD81B60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E4),
    onPrimaryContainer = Color(0xFF3A001A),
    secondary = Color(0xFF880E4F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBD4E4),
    onSecondaryContainer = Color(0xFF330018),
    tertiary = Color(0xFF6D4C41),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEFDDD7),
    onTertiaryContainer = Color(0xFF2B1510),
    background = Color(0xFFFFF8FA),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFF8FA),
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF2DDE2),
    onSurfaceVariant = Color(0xFF514347),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF847377)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7D2252),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFFE8B4CF),
    onSecondary = Color(0xFF4C0028),
    secondaryContainer = Color(0xFF6A1040),
    onSecondaryContainer = Color(0xFFFBD4E4),
    tertiary = Color(0xFFDBC0B4),
    onTertiary = Color(0xFF3E2B21),
    tertiaryContainer = Color(0xFF553C32),
    onTertiaryContainer = Color(0xFFEFDDD7),
    background = Color(0xFF201A1B),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF201A1B),
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFD5C2C6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9E8D90)
)

@Composable
fun CatalogoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}