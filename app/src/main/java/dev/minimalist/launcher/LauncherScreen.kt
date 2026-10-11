package dev.minimalist.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RowPadding = PaddingValues(horizontal = 28.dp, vertical = 13.dp)

@Composable
fun LauncherScreen(
    apps: List<AppEntry>,
    onOpen: (AppEntry) -> Unit,
) {
    val bars = WindowInsets.systemBars.asPaddingValues()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Phosphor.background),
        contentPadding = PaddingValues(
            top = bars.calculateTopPadding() + 20.dp,
            bottom = bars.calculateBottomPadding() + 40.dp,
        ),
    ) {
        items(apps, key = { it.key }) { app ->
            AppRow(app.label, onClick = { onOpen(app) })
        }
    }
}

@Composable
private fun AppRow(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    BasicText(
        text = label.lowercase(),
        style = rowStyle(if (pressed) Phosphor.accent else Phosphor.text),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(RowPadding),
    )
}

private fun rowStyle(color: Color, weight: FontWeight = FontWeight.Normal) = TextStyle(
    color = color,
    fontFamily = Phosphor.mono,
    fontWeight = weight,
    fontSize = 17.sp,
)
