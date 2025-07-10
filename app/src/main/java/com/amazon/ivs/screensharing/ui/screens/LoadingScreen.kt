package com.amazon.ivs.screensharing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.screensharing.core.handlers.NavigationHandler
import com.amazon.ivs.screensharing.ui.components.PreviewSurface

@Composable
fun LoadingScreen() {
    val isLoading by NavigationHandler.isLoading.collectAsStateWithLifecycle()

    LoadingScreenContent(
        isVisible = isLoading,
    )
}

@Composable
private fun LoadingScreenContent(
    isVisible: Boolean,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview
@Composable
private fun LoadingScreenContentPreview() {
    PreviewSurface {
        LoadingScreenContent(
            isVisible = true
        )
    }
}
