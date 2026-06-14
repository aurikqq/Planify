package com.aurikqq.planify.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun PlansUnit(isDone: Boolean, text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val color by animateColorAsState(
        if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "color"
    )
    val strikethrough by animateFloatAsState(if (isDone) 1f else 0f, label = "strikethrough")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Checkbox(
            checked = isDone,
            enabled = enabled,
            onCheckedChange = { onClick() }
        )
        Text(
            text,
            color = color,
            modifier = Modifier.drawWithContent {
                drawContent()

                if (strikethrough > 0f) {
                    val width = 1.5.dp.toPx()
                    val y = size.height / 2f + 1.dp.toPx()

                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(size.width * strikethrough, y),
                        strokeWidth = width
                    )
                }
            }
        )
    }
}
