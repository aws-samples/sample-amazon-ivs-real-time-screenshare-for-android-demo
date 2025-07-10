package com.amazon.ivs.screensharing.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amazon.ivs.screensharing.ui.theme.ScreenSharingTheme

@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ScreenSharingTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

@Preview(
    name = "Portrait Preview",
    widthDp = 392,
    heightDp = 851,
)
annotation class PortraitPreview

@Preview(
    name = "Portrait Preview Dark",
    widthDp = 392,
    heightDp = 851,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PortraitPreviewDark

@Preview(
    name = "Landscape Preview",
    widthDp = 851,
    heightDp = 392,
)
annotation class LandscapePreview

@Preview(
    name = "Landscape Preview Dark",
    widthDp = 851,
    heightDp = 392,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class LandscapePreviewDark
