package com.amazon.ivs.screensharing.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.screensharing.core.handlers.Destination
import com.amazon.ivs.screensharing.core.handlers.NavigationHandler
import com.amazon.ivs.screensharing.core.handlers.StageHandler
import com.amazon.ivs.screensharing.ui.components.LandscapePreview
import com.amazon.ivs.screensharing.ui.components.LandscapePreviewDark
import com.amazon.ivs.screensharing.ui.components.PermissionRequester
import com.amazon.ivs.screensharing.ui.components.PortraitPreview
import com.amazon.ivs.screensharing.ui.components.PortraitPreviewDark
import com.amazon.ivs.screensharing.ui.components.PreviewSurface
import com.amazon.ivs.screensharing.ui.screens.AuthScreen
import com.amazon.ivs.screensharing.ui.screens.HomeScreen
import com.amazon.ivs.screensharing.ui.screens.LoadingScreen
import com.amazon.ivs.screensharing.ui.theme.ScreenSharingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenSharingTheme {
                val destination by NavigationHandler.destination.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = destination) {
                    if (destination is Destination.Finish) {
                        StageHandler.disposeScreenShare()
                        finish()
                    }
                }

                BackHandler(onBack = NavigationHandler::goBack)
                PermissionRequester()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    MainActivityContent(
                        modifier = Modifier
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainActivityContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AuthScreen()
        HomeScreen()
        LoadingScreen()
    }
}

@PortraitPreview
@Composable
private fun TokenScreenPreviewLight() {
    MainActivityPreview(
        destination = Destination.Token
    )
}

@PortraitPreview
@Composable
private fun WatchScreenPreviewLight() {
    MainActivityPreview(
        destination = Destination.Watch
    )
}

@PortraitPreview
@Composable
private fun ShareScreenPreviewLight() {
    MainActivityPreview(
        destination = Destination.Share
    )
}

@PortraitPreviewDark
@Composable
private fun TokenScreenPreviewDark() {
    MainActivityPreview(
        destination = Destination.Token
    )
}

@PortraitPreviewDark
@Composable
private fun WatchScreenPreviewDark() {
    MainActivityPreview(
        destination = Destination.Watch
    )
}

@PortraitPreviewDark
@Composable
private fun ShareScreenPreviewDark() {
    MainActivityPreview(
        destination = Destination.Share
    )
}

@LandscapePreview
@Composable
private fun TokenScreenPreviewLightLandscape() {
    MainActivityPreview(
        destination = Destination.Token
    )
}

@LandscapePreview
@Composable
private fun WatchScreenPreviewLightLandscape() {
    MainActivityPreview(
        destination = Destination.Watch
    )
}

@LandscapePreview
@Composable
private fun ShareScreenPreviewLightLandscape() {
    MainActivityPreview(
        destination = Destination.Share
    )
}

@LandscapePreviewDark
@Composable
private fun TokenScreenPreviewDarkLandscape() {
    MainActivityPreview(
        destination = Destination.Token
    )
}

@LandscapePreviewDark
@Composable
private fun WatchScreenPreviewDarkLandscape() {
    MainActivityPreview(
        destination = Destination.Watch
    )
}

@LandscapePreviewDark
@Composable
private fun ShareScreenPreviewDarkLandscape() {
    MainActivityPreview(
        destination = Destination.Share
    )
}

@Composable
fun MainActivityPreview(
    destination: Destination
) {
    PreviewSurface {
        NavigationHandler.goTo(destination)
        MainActivityContent()
    }
}
