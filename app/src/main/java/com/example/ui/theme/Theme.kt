package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Classic M3 (Purple / Amethyst)
private val ClassicLight = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val ClassicDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5)
)

// 2. Emerald Audit (Teal / Sage / Forest - Professional Tax/Audit theme)
private val EmeraldLight = lightColorScheme(
    primary = Color(0xFF006B54),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF99F5D5),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4A635A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE8DD),
    onSecondaryContainer = Color(0xFF062018),
    tertiary = Color(0xFF3F6375),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF4FBF7),
    onBackground = Color(0xFF191D1B),
    surface = Color(0xFFF4FBF7),
    onSurface = Color(0xFF191D1B)
)

private val EmeraldDark = darkColorScheme(
    primary = Color(0xFF7DF8CE),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513E),
    onPrimaryContainer = Color(0xFF99F5D5),
    secondary = Color(0xFFB1CCBE),
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF334B43),
    onSecondaryContainer = Color(0xFFCDE8DD),
    tertiary = Color(0xFFA7CCE1),
    onTertiary = Color(0xFF0B3445),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF161E1A),
    onSurface = Color(0xFFE1E3E0)
)

// 3. Sapphire Sky (Ocean blue / Aqua / Slate)
private val SapphireLight = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D40),
    secondary = Color(0xFF42A5F5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = Color(0xFF00838F),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F8FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF5F8FC),
    onSurface = Color(0xFF1A1C1E)
)

private val SapphireDark = darkColorScheme(
    primary = Color(0xFF9CCAFF),
    onPrimary = Color(0xFF00325B),
    primaryContainer = Color(0xFF00477F),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF82CFFF),
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C70),
    onSecondaryContainer = Color(0xFFE1F5FE),
    tertiary = Color(0xFF80DEEA),
    onTertiary = Color(0xFF00363C),
    background = Color(0xFF0A1118),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF101923),
    onSurface = Color(0xFFE2E2E6)
)

// 4. Sunset Crimson (Rust / Coral / Sand)
private val SunsetLight = lightColorScheme(
    primary = Color(0xFFB22A00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD2),
    onPrimaryContainer = Color(0xFF3C0700),
    secondary = Color(0xFF77574E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD2),
    onSecondaryContainer = Color(0xFF2C1510),
    tertiary = Color(0xFF6C5D2F),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231A18),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231A18)
)

private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB59F),
    onPrimary = Color(0xFF5F1500),
    primaryContainer = Color(0xFF871200),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFE7BDB2),
    onSecondary = Color(0xFF442A22),
    secondaryContainer = Color(0xFF5D4038),
    onSecondaryContainer = Color(0xFFFFDAD2),
    tertiary = Color(0xFFD9C58D),
    onTertiary = Color(0xFF3B2F05),
    background = Color(0xFF1B110F),
    onBackground = Color(0xFFF1DFDA),
    surface = Color(0xFF241815),
    onSurface = Color(0xFFF1DFDA)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean,
  themeName: String = "Classic M3",
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeName) {
    "Emerald Audit" -> if (darkTheme) EmeraldDark else EmeraldLight
    "Sapphire Sky" -> if (darkTheme) SapphireDark else SapphireLight
    "Sunset Crimson" -> if (darkTheme) SunsetDark else SunsetLight
    else -> if (darkTheme) ClassicDark else ClassicLight
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
