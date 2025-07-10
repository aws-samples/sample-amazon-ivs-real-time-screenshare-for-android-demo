package com.amazon.ivs.screensharing.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalWindowInfo
import timber.log.Timber
import kotlin.math.abs

private const val SQUARE_SHAPE_DELTA_PX = 100

@Composable
fun isSquareOrLandscapeSize(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    val isSquare = abs(size.width - size.height) < SQUARE_SHAPE_DELTA_PX
    val isLandscapeSize = size.width >= size.height
    Timber.d("Container size: $size, is square: $isSquare, is landscape: $isLandscapeSize")
    return isSquare || isLandscapeSize
}

inline fun Modifier.thenOptional(
    enabled: Boolean,
    apply: Modifier.() -> Modifier,
): Modifier {
    return if (enabled) {
        apply()
    } else {
        this
    }
}

inline fun Modifier.onClick(
    isClickable: Boolean = true,
    showIndication: Boolean = true,
    crossinline onClick: () -> Unit,
) = composed {
    clickable(
        enabled = isClickable,
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showIndication) ripple() else null,
        onClick = {
            onClick()
        },
    )
}
