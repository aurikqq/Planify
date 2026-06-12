package com.aurikqq.planify.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable RowScope.() -> Unit
) {
    val container by animateColorAsState(if (enabled) containerColor else disabledContainerColor, label = "buttonContainer")
    val contentCol by animateColorAsState(if (enabled) contentColor else disabledContentColor, label = "buttonContent")

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentCol,
            disabledContainerColor = container,
            disabledContentColor = contentCol
        ),
        content = content
    )
}

@Composable
fun AnimatedElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.elevatedShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable RowScope.() -> Unit
) {
    val container by animateColorAsState(if (enabled) containerColor else disabledContainerColor, label = "elevatedButtonContainer")
    val contentCol by animateColorAsState(if (enabled) contentColor else disabledContentColor, label = "elevatedButtonContent")

    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = container,
            contentColor = contentCol,
            disabledContainerColor = container,
            disabledContentColor = contentCol
        ),
        content = content
    )
}

@Composable
fun AnimatedTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.filledTonalShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable RowScope.() -> Unit
) {
    val container by animateColorAsState(if (enabled) containerColor else disabledContainerColor, label = "tonalButtonContainer")
    val contentCol by animateColorAsState(if (enabled) contentColor else disabledContentColor, label = "tonalButtonContent")

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = container,
            contentColor = contentCol,
            disabledContainerColor = container,
            disabledContentColor = contentCol
        ),
        content = content
    )
}

@Composable
fun AnimatedTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
    content: @Composable RowScope.() -> Unit
) {
    val contentCol by animateColorAsState(if (enabled) contentColor else disabledContentColor, label = "textButtonContent")

    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentCol,
            disabledContentColor = contentCol
        ),
        content = content
    )
}
