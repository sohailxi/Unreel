package app.unreel.android.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E266D),
    onPrimaryContainer = Color(0xFFD6D0FF),
    secondary = Color(0xFF00CEC9),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D4B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFF7675),
    onTertiary = Color.White,
    background = Color(0xFF121214),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF1E1E24),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF2B2B36),
    onSurfaceVariant = Color(0xFFCBCBD4),
    error = Color(0xFFFF5252),
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A4AD1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECE9FF),
    onPrimaryContainer = Color(0xFF1D1263),
    secondary = Color(0xFF00A896),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FBF7),
    onSecondaryContainer = Color(0xFF003832),
    tertiary = Color(0xFFE84118),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEBEBF0),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun UnreelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
