package dev.minimalist.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RowPadding = PaddingValues(horizontal = 28.dp, vertical = 13.dp)

/**
 * The whole home screen: pinned camera, divider, then every other app.
 *
 * [heldKey] is the app currently long-pressed, if any. While one is held,
 * tapping any row only dismisses it.
 */
@Composable
fun LauncherScreen(
    home: HomeApps,
    heldKey: String?,
    onOpen: (AppEntry) -> Unit,
    onHold: (AppEntry) -> Unit,
    onRelease: () -> Unit,
    onRemove: (AppEntry) -> Unit,
) {
    val bars = WindowInsets.systemBars.asPaddingValues()

    @Composable
    fun Entry(app: AppEntry, label: String, pinned: Boolean = false) {
        AppRow(
            app = app,
            label = label,
            pinned = pinned,
            held = app.key == heldKey,
            onClick = { if (heldKey != null) onRelease() else onOpen(app) },
            onLongClick = { onHold(app) },
            onRemove = { onRemove(app) },
            onCancel = onRelease,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Phosphor.background),
        contentPadding = PaddingValues(
            top = bars.calculateTopPadding() + 20.dp,
            bottom = bars.calculateBottomPadding() + 40.dp,
        ),
    ) {
        home.camera?.let { camera ->
            item(key = "camera") { Entry(camera, "> " + camera.label, pinned = true) }
            item(key = "divider") { DashedDivider() }
        }
        items(home.apps, key = { it.key }) { app -> Entry(app, app.label) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppEntry,
    label: String,
    pinned: Boolean,
    held: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val color = when {
        held -> Phosphor.background
        pressed || pinned -> Phosphor.accent
        else -> Phosphor.text
    }

    Column {
        BasicText(
            text = label.lowercase(),
            style = monoStyle(color, weight = if (held) FontWeight.Bold else FontWeight.Normal),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (held) Phosphor.accent else Color.Transparent)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                )
                .padding(RowPadding),
        )
        if (held) {
            // System apps can't be uninstalled, so they get App Info instead.
            ActionLine(
                actionLabel = if (app.isSystem) "[ app info ]" else "[ uninstall ]",
                onAction = onRemove,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun ActionLine(actionLabel: String, onAction: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionText(actionLabel, Phosphor.accent, onAction)
        ActionText("[ cancel ]", Phosphor.dim, onCancel)
        BlinkingCursor()
    }
}

@Composable
private fun ActionText(text: String, color: Color, onClick: () -> Unit) {
    BasicText(
        text = text,
        style = monoStyle(color, size = 14.sp),
        modifier = Modifier
            .clickable(interactionSource = null, indication = null, onClick = onClick)
            // Tall enough to hit comfortably with a thumb.
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun BlinkingCursor() {
    val phase by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1100, easing = LinearEasing)),
        label = "cursor-phase",
    )
    Box(
        Modifier
            .size(width = 9.dp, height = 17.dp)
            .graphicsLayer { alpha = if (phase < 0.5f) 1f else 0f }
            .background(Phosphor.accent),
    )
}

@Composable
private fun DashedDivider() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp)
            .height(1.dp),
    ) {
        drawLine(
            color = Phosphor.dim,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
        )
    }
}

private fun monoStyle(
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    size: TextUnit = 17.sp,
) = TextStyle(
    color = color,
    fontFamily = Phosphor.mono,
    fontWeight = weight,
    fontSize = size,
)
