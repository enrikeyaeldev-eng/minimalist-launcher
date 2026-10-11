package dev.minimalist.launcher

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** "Phosphor" palette from the approved mockup. */
object Phosphor {
    val background = Color(0xFF050605)
    val text = Color(0xFFB9C7B9)
    val dim = Color(0xFF4C5A4C)
    val accent = Color(0xFF39FF14)

    val mono = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )
}
