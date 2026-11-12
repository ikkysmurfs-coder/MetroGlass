/**
 * MetroGlass (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score

val DefaultThemeColor = MetroOrange

val DarkGlassColorScheme = darkColorScheme(
    primary = MetroOrange,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF331000),
    onPrimaryContainer = PureWhite,
    secondary = MetroOrange,
    onSecondary = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkGlassSurface,
    onSurfaceVariant = PureWhite,
    outline = DarkGlassBorder,
)

val LightGlassColorScheme = lightColorScheme(
    primary = MetroOrange,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = MetroOrange,
    secondary = MetroOrange,
    onSecondary = PureWhite,
    background = OffWhite,
    onBackground = PureBlack,
    surface = OffWhite,
    onSurface = PureBlack,
    surfaceVariant = LightGlassSurface,
    onSurfaceVariant = PureBlack,
    outline = LightGlassBorder,
)

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = true,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(darkTheme, pureBlack, themeColor) {
        if (darkTheme) {
            DarkGlassColorScheme
        } else {
            LightGlassColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

fun Bitmap.extractThemeColor(): Color = DefaultThemeColor

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> = Score.score(
    swatches.associate { it.rgb to it.population },
    desiredColorCount,
    fallbackColor,
    true,
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
