package com.amazon.ivs.screensharing.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.screensharing.R
import com.amazon.ivs.screensharing.appContext
import com.amazon.ivs.screensharing.core.common.ScreenCaptureService
import com.amazon.ivs.screensharing.core.common.ShareServiceHandler
import com.amazon.ivs.screensharing.core.common.launchDefault
import com.amazon.ivs.screensharing.core.handlers.Destination
import com.amazon.ivs.screensharing.core.handlers.NavigationHandler
import com.amazon.ivs.screensharing.core.handlers.Participant
import com.amazon.ivs.screensharing.core.handlers.StageHandler
import com.amazon.ivs.screensharing.ui.components.ANIMATION_DURATION
import com.amazon.ivs.screensharing.ui.components.LandscapePreview
import com.amazon.ivs.screensharing.ui.components.LandscapePreviewDark
import com.amazon.ivs.screensharing.ui.components.PortraitPreview
import com.amazon.ivs.screensharing.ui.components.PortraitPreviewDark
import com.amazon.ivs.screensharing.ui.components.PreviewSurface
import com.amazon.ivs.screensharing.ui.components.TextButton
import com.amazon.ivs.screensharing.ui.components.animationEasing
import com.amazon.ivs.screensharing.ui.components.isSquareOrLandscapeSize
import com.amazon.ivs.screensharing.ui.components.onClick
import com.amazon.ivs.screensharing.ui.theme.TextStylePrimary
import com.amazonaws.ivs.broadcast.Stage
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun HomeScreen() {
    val destination by NavigationHandler.destination.collectAsStateWithLifecycle()
    val isLoading by NavigationHandler.isLoading.collectAsStateWithLifecycle()
    val participants by StageHandler.participants.collectAsStateWithLifecycle()
    val connectionState by StageHandler.connectionState.collectAsStateWithLifecycle()
    val isSharingScreen by StageHandler.isSharingScreen.collectAsStateWithLifecycle()

    HomeScreenContent(
        isVisible = !isLoading && (destination is Destination.Watch || destination is Destination.Share),
        isShareTab = destination is Destination.Share,
        participants = participants,
        connectionState = connectionState,
        isSharingScreen = isSharingScreen,
    )
}

@Composable
private fun HomeScreenContent(
    isVisible: Boolean = true,
    isShareTab: Boolean = false,
    isSharingScreen: Boolean = false,
    connectionState: Stage.ConnectionState,
    participants: List<Participant> = emptyList()
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackButton(
                isShareTab = isShareTab,
                isSharingScreen = isSharingScreen,
            )
            ConnectionState(connectionState = connectionState)
            TabBar(isShareTab = isShareTab)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                WatchScreen(
                    isVisible = !isShareTab,
                    participants = participants
                )
                ShareScreen(
                    isVisible = isShareTab,
                    isSharingScreen = isSharingScreen,
                )
            }
        }
    }
}

@Composable
private fun ConnectionState(
    connectionState: Stage.ConnectionState
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.connection_state),
            style = TextStylePrimary,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = connectionState.name,
            style = TextStylePrimary,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun WatchScreen(
    isVisible: Boolean,
    participants: List<Participant>
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            columns = GridCells.Fixed(if (isSquareOrLandscapeSize()) 2 else 1),
        ) {
            items(items = participants, key = { it.id }) { participant ->
                ParticipantView(participant = participant)
            }
        }
    }
}

@Composable
private fun ParticipantView(
    participant: Participant,
) {
    val shape = RoundedCornerShape(8.dp)
    var video by remember { mutableStateOf(participant.preview) }

    LaunchedEffect(key1 = video) {
        if (video != null) return@LaunchedEffect
        delay(200)
        Timber.d("Video view - video is null")
        video = participant.preview
    }

    LaunchedEffect(key1 = participant.isMuted) {
        Timber.d("Participant mute state changed - requesting new preview")
        video = participant.preview
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(
                color = MaterialTheme.colorScheme.onBackground,
                shape = shape
            )
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.video_off),
            style = TextStylePrimary,
            color = MaterialTheme.colorScheme.background,
        )

        AnimatedVisibility(
            visible = video != null && !participant.isMuted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    FrameLayout(context).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    }
                }, update = { layout ->
                    val preview = if (!participant.isMuted) {
                        video ?: return@AndroidView
                    } else {
                        return@AndroidView
                    }

                    Timber.d("Video view - updating view: $preview")
                    layout.removeView(preview)
                    (preview.parent as? ViewGroup)?.removeView(preview)
                    layout.addView(preview)
                }
            )
        }
    }
}

@Composable
private fun ShareScreen(
    isVisible: Boolean,
    isSharingScreen: Boolean,
) {
    val isServiceReady by ShareServiceHandler.isReady.collectAsStateWithLifecycle(false)
    val error by StageHandler.onError.collectAsStateWithLifecycle()
    val mediaProjectionManager: MediaProjectionManager? = if (LocalInspectionMode.current) null else remember {
        appContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    var shareIntent by remember { mutableStateOf<Intent?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContract<Unit, Pair<Int, Intent?>?>() {
            override fun createIntent(context: Context, input: Unit) =
                mediaProjectionManager?.createScreenCaptureIntent() ?: Intent()
            override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Intent?>? =
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    Pair(resultCode, intent)
                } else {
                    null
                }
        }
    ) { result ->
        result?.let { (resultCode, intent) ->
            if (resultCode == Activity.RESULT_OK && intent != null) {
                try {
                    shareIntent = intent
                    val serviceIntent = Intent(appContext, ScreenCaptureService::class.java)
                    ContextCompat.startForegroundService(appContext, serviceIntent)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get media projection")
                    StageHandler.stopScreenShare()
                }
            } else {
                StageHandler.stopScreenShare()
            }
        }
    }

    LaunchedEffect(key1 = isServiceReady) {
        val intent = shareIntent ?: return@LaunchedEffect
        if (!isServiceReady) return@LaunchedEffect
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return@LaunchedEffect
        Timber.d("Service is ready - starting media projection")
        val mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, intent)
        StageHandler.startScreenShare(mediaProjection = mediaProjection)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(
                    if (isSharingScreen) {
                        R.string.stop_screen_share
                    } else {
                        R.string.start_screen_share
                    }
                ),
                onClick = {
                    if (isSharingScreen) {
                        StageHandler.stopScreenShare()
                    } else {
                        launcher.launch(Unit)
                    }
                }
            )
            AnimatedVisibility(
                visible = error.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = error,
                    style = TextStylePrimary,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun BackButton(
    isShareTab: Boolean,
    isSharingScreen: Boolean,
) {
    var isClicked by remember { mutableStateOf(false) }
    var isTextChanged by remember { mutableStateOf(false) }
    var isFirstSkipped by remember { mutableStateOf(false) }
    val padding by animateDpAsState(
        targetValue = when {
            isClicked -> 16.dp
            isTextChanged -> 10.dp
            else -> 8.dp
        },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = animationEasing,
        )
    )
    val buttonText = if (isSharingScreen) {
        R.string.stop_screen_share
    } else if (isShareTab) {
        R.string.go_back
    } else {
        R.string.leave_stage
    }

    LaunchedEffect(key1 = buttonText) {
        if (!isFirstSkipped) {
            isFirstSkipped = true
            return@LaunchedEffect
        }
        if (isTextChanged) return@LaunchedEffect
        isTextChanged = true
        delay(ANIMATION_DURATION.toLong())
        isTextChanged = false
    }

    LaunchedEffect(key1 = isClicked) {
        if (!isClicked) return@LaunchedEffect
        delay(ANIMATION_DURATION.toLong())
        isClicked = false
    }

    Row(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .onClick {
                launchDefault {
                    isClicked = true
                    if (isSharingScreen) {
                        StageHandler.stopScreenShare()
                        return@launchDefault
                    }
                    // TODO: Remove this
                    StageHandler.stopScreenShare()
                    delay(ANIMATION_DURATION.toLong())
                    NavigationHandler.goBack()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(padding)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(buttonText),
            style = TextStylePrimary,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun TabBar(
    isShareTab: Boolean
) {
    var animationState by remember { mutableIntStateOf(0) }
    val cornerSize by animateDpAsState(
        targetValue = when (animationState) {
            0, 2 -> 8.dp
            else -> 64.dp
        },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION * 2,
            easing = animationEasing,
        ),
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(cornerSize)
            )
    ) {
        var directionForward by remember { mutableStateOf(true) }
        var alignment by remember { mutableStateOf(if (isShareTab) Alignment.CenterEnd else Alignment.CenterStart) }
        val tabWidth by animateFloatAsState(
            targetValue = when (animationState) {
                0, 2 -> 0.5f
                1 -> 1f
                else -> 0.5f
            },
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION,
                easing = animationEasing,
            ),
            finishedListener = {
                if (directionForward) {
                    when (animationState) {
                        0 -> animationState = 1
                        1 -> animationState = 2
                    }
                } else {
                    when (animationState) {
                        2 -> animationState = 1
                        1 -> animationState = 0
                    }
                }
            }
        )

        LaunchedEffect(key1 = isShareTab) {
            if (!isShareTab && animationState == 0) return@LaunchedEffect
            animationState = 1
            directionForward = isShareTab
        }

        LaunchedEffect(key1 = tabWidth) {
            if (tabWidth == 1f) {
                alignment = if (directionForward) {
                    Alignment.CenterEnd
                } else {
                    Alignment.CenterStart
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(tabWidth)
                .align(alignment)
                .padding(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(cornerSize / 2)
                )
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val watchTextColor by animateColorAsState(
                targetValue = if (isShareTab){
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                animationSpec = tween(
                    delayMillis = if (!isShareTab) 0 else ANIMATION_DURATION,
                    easing = animationEasing,
                ),
            )
            val shareTextColor by animateColorAsState(
                targetValue = if (isShareTab) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                animationSpec = tween(
                    delayMillis = if (isShareTab) 0 else ANIMATION_DURATION,
                    easing = animationEasing,
                ),
            )

            TextButton(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.watch),
                textColor = watchTextColor,
                isClickable = animationState != 1,
                showIndication = false,
                drawBackground = false,
                onClick = {
                    NavigationHandler.goTo(Destination.Watch)
                },
            )
            TextButton(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.screen_share),
                textColor = shareTextColor,
                isClickable = animationState != 1,
                showIndication = false,
                drawBackground = false,
                onClick = {
                    NavigationHandler.goTo(Destination.Share)
                },
            )
        }
    }
}

@PortraitPreview
@Composable
private fun WatchPreviewEmptyLight() {
    HomeScreenContentPreview()
}

@PortraitPreviewDark
@Composable
private fun WatchPreviewEmptyDark() {
    HomeScreenContentPreview()
}

@PortraitPreview
@Composable
private fun WatchPreviewFullLight() {
    HomeScreenContentPreview(
        participants = listOf(
            Participant(id = "0"),
            Participant(id = "1"),
            Participant(id = "2"),
        ),
        isSharingScreen = true,
        connectionState = Stage.ConnectionState.CONNECTED,
    )
}

@PortraitPreviewDark
@Composable
private fun WatchPreviewFullDark() {
    HomeScreenContentPreview(
        participants = listOf(
            Participant(id = "0"),
            Participant(id = "1"),
            Participant(id = "2"),
        ),
        isSharingScreen = true,
        connectionState = Stage.ConnectionState.CONNECTED,
    )
}

@PortraitPreview
@Composable
private fun SharePreviewLight() {
    HomeScreenContentPreview(
        isShareTab = true,
        connectionState = Stage.ConnectionState.CONNECTING,
    )
}

@PortraitPreviewDark
@Composable
private fun SharePreviewDark() {
    HomeScreenContentPreview(
        isShareTab = true,
        connectionState = Stage.ConnectionState.CONNECTING,
    )
}

@LandscapePreview
@Composable
private fun WatchPreviewEmptyLightLandscape() {
    HomeScreenContentPreview()
}

@LandscapePreviewDark
@Composable
private fun WatchPreviewEmptyDarkLandscape() {
    HomeScreenContentPreview()
}

@LandscapePreview
@Composable
private fun WatchPreviewFullLightLandscape() {
    HomeScreenContentPreview(
        participants = listOf(
            Participant(id = "0"),
            Participant(id = "1"),
            Participant(id = "2"),
        ),
        isSharingScreen = true,
        connectionState = Stage.ConnectionState.CONNECTED,
    )
}

@LandscapePreviewDark
@Composable
private fun WatchPreviewFullDarkLandscape() {
    HomeScreenContentPreview(
        participants = listOf(
            Participant(id = "0"),
            Participant(id = "1"),
            Participant(id = "2"),
        ),
        isSharingScreen = true,
        connectionState = Stage.ConnectionState.CONNECTED,
    )
}

@LandscapePreview
@Composable
private fun SharePreviewLightLandscape() {
    HomeScreenContentPreview(
        isShareTab = true,
        connectionState = Stage.ConnectionState.CONNECTING,
    )
}

@LandscapePreviewDark
@Composable
private fun SharePreviewDarkLandscape() {
    HomeScreenContentPreview(
        isShareTab = true,
        connectionState = Stage.ConnectionState.CONNECTING,
    )
}

@Composable
private fun HomeScreenContentPreview(
    isShareTab: Boolean = false,
    isSharingScreen: Boolean = false,
    participants: List<Participant> = emptyList(),
    connectionState: Stage.ConnectionState = Stage.ConnectionState.DISCONNECTED,
) {
    PreviewSurface {
        HomeScreenContent(
            isShareTab = isShareTab,
            isSharingScreen = isSharingScreen,
            participants = participants,
            connectionState = connectionState,
        )
    }
}
