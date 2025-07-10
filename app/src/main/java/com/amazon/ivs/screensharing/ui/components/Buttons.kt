package com.amazon.ivs.screensharing.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amazon.ivs.screensharing.ui.theme.BlackColorPrimary
import com.amazon.ivs.screensharing.ui.theme.TextStylePrimary
import com.amazon.ivs.screensharing.ui.theme.WhiteColorPrimary
import kotlinx.coroutines.delay

@Composable
fun TextButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    drawBackground: Boolean = true,
    isClickable: Boolean = true,
    showIndication: Boolean = true,
    onClick: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    var isClicked by remember { mutableStateOf(false) }
    val cornerSize by animateDpAsState(
        targetValue = if (isClicked && drawBackground) 64.dp else 8.dp,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION * 2,
            easing = animationEasing,
        ),
    )
    val background by animateColorAsState(
        targetValue = if (isDarkTheme) WhiteColorPrimary else BlackColorPrimary
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isDarkTheme) BlackColorPrimary else WhiteColorPrimary
    )
    val shape = RoundedCornerShape(cornerSize)

    LaunchedEffect(key1 = isClicked) {
        if (!isClicked) return@LaunchedEffect
        delay(ANIMATION_DURATION.toLong())
        isClicked = false
    }

    Box(
        modifier = modifier
            .thenOptional(enabled = drawBackground) {
                background(
                    color = background,
                    shape = shape,
                )
            }
            .clip(shape)
            .onClick(
                isClickable = isClickable,
                showIndication = showIndication,
                onClick = {
                    isClicked = true
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            text = text,
            style = TextStylePrimary,
            textAlign = TextAlign.Center,
            color = textColor ?: animatedTextColor,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ButtonPreviewDark() {
    ButtonPreview()
}

@Preview
@Composable
private fun ButtonPreviewLight() {
    ButtonPreview()
}

@Composable
private fun ButtonPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Some text",
                onClick = {}
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Click me",
                onClick = {}
            )
        }
    }
}
