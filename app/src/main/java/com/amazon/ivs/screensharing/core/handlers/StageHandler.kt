package com.amazon.ivs.screensharing.core.handlers

import android.Manifest
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.window.layout.WindowMetricsCalculator
import com.amazon.ivs.screensharing.appContext
import com.amazon.ivs.screensharing.core.common.ShareServiceHandler
import com.amazon.ivs.screensharing.core.common.launchDefault
import com.amazon.ivs.screensharing.core.common.launchMain
import com.amazonaws.ivs.broadcast.AudioDevice
import com.amazonaws.ivs.broadcast.AudioLocalStageStream
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.BroadcastConfiguration.AspectMode
import com.amazonaws.ivs.broadcast.BroadcastException
import com.amazonaws.ivs.broadcast.CustomAudioSource
import com.amazonaws.ivs.broadcast.Device
import com.amazonaws.ivs.broadcast.Device.Descriptor.DeviceType
import com.amazonaws.ivs.broadcast.Device.Descriptor.Position
import com.amazonaws.ivs.broadcast.DeviceDiscovery
import com.amazonaws.ivs.broadcast.ImageLocalStageStream
import com.amazonaws.ivs.broadcast.LocalStageStream
import com.amazonaws.ivs.broadcast.ParticipantInfo
import com.amazonaws.ivs.broadcast.Stage
import com.amazonaws.ivs.broadcast.Stage.Strategy
import com.amazonaws.ivs.broadcast.StageRenderer
import com.amazonaws.ivs.broadcast.StageStream
import com.amazonaws.ivs.broadcast.SurfaceSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

private const val SCREEN_SHARE_ID = "ScreenShareStream"

object StageHandler {
    private val _deviceDiscovery by lazy { DeviceDiscovery(appContext) }

    private val _onError = MutableStateFlow("")
    private val _isInvalidToken = MutableStateFlow(false)
    private val _isSharingScreen = MutableStateFlow(false)
    private val _participants = MutableStateFlow(emptyList<Participant>())
    private val _connectionState = MutableStateFlow(Stage.ConnectionState.DISCONNECTED)

    private val _shareStreams = mutableListOf<LocalStageStream>()
    private var _stage: Stage? = null

    private val _stageStrategy = object : Strategy {
        override fun stageStreamsToPublishForParticipant(stage: Stage, info: ParticipantInfo) =
            if (_isSharingScreen.value) _shareStreams else emptyList()
        override fun shouldPublishFromParticipant(stage: Stage, info: ParticipantInfo) = _isSharingScreen.value
        override fun shouldSubscribeToParticipant(stage: Stage, info: ParticipantInfo) = Stage.SubscribeType.AUDIO_VIDEO
    }
    private val _stageRenderer = object : StageRenderer {
        override fun onConnectionStateChanged(
            stage: Stage,
            state: Stage.ConnectionState,
            exception: BroadcastException?
        ) {
            super.onConnectionStateChanged(stage, state, exception)
            _connectionState.update { state }
        }
        override fun onStreamsAdded(stage: Stage, participantInfo: ParticipantInfo, streams: List<StageStream?>) {
            super.onStreamsAdded(stage, participantInfo, streams)
            if (participantInfo.isLocal) {
                Timber.d("Self streams added: ${streams.map { it?.streamType }}")
            }
            if (participantInfo.isLocal) return
            if (_participants.value.any { it.id == participantInfo.participantId }) return

            val stream = streams.find { it?.streamType == StageStream.Type.VIDEO }
            Timber.d("Stream added for: ${participantInfo.participantId}")
            val participants = _participants.value.toMutableList().apply {
                add(
                    Participant(
                        id = participantInfo.participantId,
                        stream = stream,
                        isMuted = stream == null || stream.muted,
                    )
                )
            }
            _participants.update { participants }
        }
        override fun onStreamsRemoved(stage: Stage, participantInfo: ParticipantInfo, streams: List<StageStream?>) {
            super.onStreamsRemoved(stage, participantInfo, streams)
            if (participantInfo.isLocal) return
            if (_participants.value.none { it.id == participantInfo.participantId }) return

            streams.find { it?.streamType == StageStream.Type.VIDEO }?.let { stream ->
                Timber.d("Stream removed for: ${participantInfo.participantId}")
                val participants = _participants.value.filter { it.id != participantInfo.participantId }
                _participants.update { participants }
            }
        }

        override fun onStreamsMutedChanged(stage: Stage, info: ParticipantInfo, streams: List<StageStream?>) {
            super.onStreamsMutedChanged(stage, info, streams)
            if (info.isLocal) return
            if (_participants.value.none { it.id == info.participantId }) return

            streams.find { it?.streamType == StageStream.Type.VIDEO }?.let { stream ->
                Timber.d("Stream muted changed for: ${info.participantId}, is muted: ${stream.muted}")
                val participants = _participants.value.map { participant ->
                    if (participant.id == info.participantId) {
                        participant.copy(
                            stream = stream,
                            isMuted = stream.muted
                        )
                    } else {
                        participant.copy()
                    }
                }
                _participants.update { participants }
            }
        }

        override fun onError(exception: BroadcastException) {
            super.onError(exception)
            Timber.d("Stage error: ${exception.message}")
            _onError.update { exception.message ?: "Unknown error" }
        }
    }

    private var _mediaProjection: MediaProjection? = null
    private var _virtualDisplay: VirtualDisplay? = null
    private val _mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            val isSharing = _isSharingScreen.value
            Timber.d("Media projection stopped, is sharing: $isSharing")
            if (isSharing) return
            disposeScreenShare()
        }
    }

    val onError = _onError.asStateFlow()
    val isSharingScreen = _isSharingScreen.asStateFlow()
    val isInvalidToken = _isInvalidToken.asStateFlow()
    val participants = _participants.asStateFlow()
    val connectionState = _connectionState.asStateFlow()

    fun clearInvalidTokenState() {
        _isInvalidToken.update { false }
    }

    fun joinStage(token: String) = launchMain {
        if (_stage != null) return@launchMain
        if (token.isBlank()) return@launchMain

        Timber.d("Joining stage: $token")
        PreferencesHandler.token = token
        NavigationHandler.setLoading(true)
        val isJoined = try {
            _stage = Stage(appContext, token, _stageStrategy)
            _stage?.refreshStrategy()
            _stage?.addRenderer(_stageRenderer)
            _stage?.join()
            true
        } catch (e: Exception) {
            Timber.d(e, "Failed to join stage")
            _stage = null
            false
        }
        delay(1000)
        NavigationHandler.setLoading(false)
        _isInvalidToken.update { !isJoined }
        if (isJoined) {
            NavigationHandler.goTo(Destination.Watch)
        }
    }

    fun leaveStage() = launchDefault {
        if (_stage == null) return@launchDefault

        Timber.d("Leaving stage")
        _participants.update { emptyList() }
        _shareStreams.clear()
        _stage?.removeRenderer(_stageRenderer)
        _stage?.leave()
        _stage = null
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startScreenShare(mediaProjection: MediaProjection?) {
        try {
            if (_isSharingScreen.value) return
            val projection = mediaProjection ?: return
            val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(appContext)
            val bounds = metrics.bounds
            val screenWidth = bounds.width()
            val screenHeight = bounds.height()
            val densityDpi = appContext.resources.displayMetrics.densityDpi
            val imageStream = _deviceDiscovery.createImageInputSource(
                BroadcastConfiguration.Vec2(screenWidth.toFloat(), screenHeight.toFloat())
            )
            val audioStream = _deviceDiscovery.createAudioInputSource(
                2,
                BroadcastConfiguration.AudioSampleRate.RATE_44100,
                AudioDevice.Format.INT16_PLANAR
            )

            _mediaProjection = projection
            projection.registerCallback(_mediaProjectionCallback, Handler(Looper.getMainLooper()))
            _virtualDisplay = projection.createVirtualDisplay(
                SCREEN_SHARE_ID,
                screenWidth,
                screenHeight,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageStream.inputSurface,
                null,
                null
            )

            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_STEREO
            val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
            val audioConfiguration = AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setEncoding(audioEncoding)
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .build()
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioEncoding
            )
            val audioRecord = AudioRecord.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBufferSize)
                .setAudioPlaybackCaptureConfig(audioConfiguration)
                .build()

            _shareStreams.add(ImageLocalStageStream(imageStream))
            // TODO: Only one audio stream can be added to the list - this should be optional
            //  also - this doesn't work, there is no documentation on how to use the API and even
            //  if it's possible to stream the audio this way. Uncomment once fixed.
            // _shareStreams.add(AudioLocalStageStream(audioStream))
            getDevice(type = DeviceType.MICROPHONE)?.let { device ->
                val audioDevice = AudioLocalStageStream(device)
                _shareStreams.add(audioDevice)
            }
            _onError.update { "" }
            _isSharingScreen.update { true }
            _stage?.refreshStrategy()

            launchDefault {
                delay(200)
                recordAndStreamAudio(audioRecord, audioStream, minBufferSize)
            }
            Timber.d("Screen share started: [$screenWidth : $screenHeight]")

            if (_participants.value.any { it.id == SCREEN_SHARE_ID }) return
            val participants = _participants.value.toMutableList().apply {
                add(
                    Participant(
                        id = SCREEN_SHARE_ID,
                        stream = ImageLocalStageStream(imageStream),
                    )
                )
            }
            _participants.update { participants }
        } catch (e: Exception) {
            Timber.w(e, "Failed to start screen share")
            _onError.update { "Failed to start screen share: ${e.message}" }
            _isSharingScreen.update { false }
        }
    }

    fun stopScreenShare() = launchDefault {
        ShareServiceHandler.setReady(false)
        val participants = _participants.value.filter { it.id != SCREEN_SHARE_ID }
        _shareStreams.clear()
        _participants.update { participants }
        _onError.update { "" }
        _isSharingScreen.update { false }
        delay(200)
        _stage?.refreshStrategy()
    }

    fun disposeScreenShare() {
        _virtualDisplay?.release()
        _mediaProjection?.unregisterCallback(_mediaProjectionCallback)
        Timber.d("Screen share disposed")
    }

    private fun getDevice(type: DeviceType, position: Position? = null): Device? {
        val devices: List<Device> = _deviceDiscovery.listLocalDevices().sortedBy { it.descriptor.deviceId }
        return devices.find { it.descriptor.type == type && (position == null || it.descriptor.position == position) }
    }

    private fun recordAndStreamAudio(
        audioRecord: AudioRecord,
        audioStream: CustomAudioSource,
        bufferSize: Int,
    ) = launchDefault {
        // TODO: Doesn't work
        Timber.d("Recording audio: ${_isSharingScreen.value}, $bufferSize")
        val buffer = ByteArray(bufferSize)
        audioRecord.startRecording()

        while (_isSharingScreen.value) {
            val bytesRead = audioRecord.read(buffer, 0, buffer.size)
            if (bytesRead > 0) {
                // TODO: Here the bytes read are not zero - so there is data .. but how should we pass it down
                //  to the custom audio source?
                val javaBuffer = java.nio.ByteBuffer.wrap(buffer)
                val timestampNs = System.nanoTime()
                audioStream.appendBuffer(javaBuffer, bytesRead.toLong(), timestampNs)
            }
        }

        Timber.d("Stopping audio recording")
        audioRecord.stop()
        audioRecord.release()
    }
}

data class Participant(
    val id: String,
    val stream: StageStream? = null,
    val isMuted: Boolean = false,
) {
    val preview get() = try {
        if (stream?.muted == true || isMuted) {
            null
        } else {
            (stream?.device as? SurfaceSource)?.getPreviewView(AspectMode.FILL)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to get video preview")
        null
    }
}
