package com.amazon.ivs.screensharing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.screensharing.R
import com.amazon.ivs.screensharing.core.common.launchDefault
import com.amazon.ivs.screensharing.core.handlers.Destination
import com.amazon.ivs.screensharing.core.handlers.NavigationHandler
import com.amazon.ivs.screensharing.core.handlers.PreferencesHandler
import com.amazon.ivs.screensharing.core.handlers.StageHandler
import com.amazon.ivs.screensharing.ui.components.ANIMATION_DURATION
import com.amazon.ivs.screensharing.ui.components.LandscapePreview
import com.amazon.ivs.screensharing.ui.components.LandscapePreviewDark
import com.amazon.ivs.screensharing.ui.components.PortraitPreview
import com.amazon.ivs.screensharing.ui.components.PortraitPreviewDark
import com.amazon.ivs.screensharing.ui.components.PreviewSurface
import com.amazon.ivs.screensharing.ui.components.TextButton
import com.amazon.ivs.screensharing.ui.components.TextInput
import com.amazon.ivs.screensharing.ui.components.animationEasing
import kotlinx.coroutines.delay

@Composable
fun AuthScreen() {
    val destination by NavigationHandler.destination.collectAsStateWithLifecycle()
    val isLoading by NavigationHandler.isLoading.collectAsStateWithLifecycle()
    val token = if (LocalInspectionMode.current) "" else PreferencesHandler.token ?: ""

    AuthScreenContent(
        isVisible = !isLoading && destination is Destination.Token,
        token = token,
    )
}

@Composable
private fun AuthScreenContent(
    isVisible: Boolean,
    token: String,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val isInvalidToken by StageHandler.isInvalidToken.collectAsStateWithLifecycle()
        var currentToken by remember {
            mutableStateOf(
                TextFieldValue(
                    text = token,
                    selection = TextRange(token.length),
                )
            )
        }

        var isClicked by remember { mutableStateOf(false) }
        val borderColor by animateColorAsState(
            targetValue = if (isInvalidToken) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
        )
        val cornerSize by animateDpAsState(
            targetValue = if (isClicked) 16.dp else 8.dp,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION,
                easing = animationEasing,
            ),
        )
        val borderWidth by animateDpAsState(
            targetValue = if (isInvalidToken) 2.dp else 1.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = currentToken,
                hint = stringResource(R.string.enter_token),
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                shape = RoundedCornerShape(cornerSize),
                borderColor = borderColor,
                borderWidth = borderWidth,
                onValueChanged = {
                    StageHandler.clearInvalidTokenState()
                    currentToken = it
                },
                onImeAction = {
                    isClicked = true
                    launchDefault {
                        delay(ANIMATION_DURATION / 2L)
                        isClicked = false
                        StageHandler.joinStage(currentToken.text)
                    }
                }
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.join_stage),
                onClick = {
                    launchDefault {
                        delay(ANIMATION_DURATION / 2L)
                        StageHandler.joinStage(currentToken.text)
                    }
                }
            )
        }
    }
}

@PortraitPreview
@Composable
private fun AuthScreenContentPreviewLight() {
    AuthScreenContentPreview()
}

@PortraitPreviewDark
@Composable
private fun AuthScreenContentPreviewDark() {
    AuthScreenContentPreview()
}

@LandscapePreview
@Composable
private fun AuthScreenContentPreviewLightLandscape() {
    AuthScreenContentPreview()
}

@LandscapePreviewDark
@Composable
private fun AuthScreenContentPreviewDarkLandscape() {
    AuthScreenContentPreview()
}

@Composable
private fun AuthScreenContentPreview() {
    PreviewSurface {
        AuthScreenContent(
            isVisible = true,
            token = ""
        )
    }
}
