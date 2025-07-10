package com.amazon.ivs.screensharing.core.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.amazon.ivs.screensharing.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

object ShareServiceHandler {
    private val _isReady = MutableStateFlow(false)

    val isReady = _isReady.asStateFlow()

    fun setReady(isReady: Boolean) {
        Timber.d("Setting service ready: $isReady")
        _isReady.update { isReady }
    }
}

class ScreenCaptureService : Service() {
    private var _isStarted = false

    override fun onCreate() {
        super.onCreate()
        launchDefault {
            ShareServiceHandler.isReady.collect { isReady ->
                if (isReady || !_isStarted) return@collect
                Timber.d("Stopping screen capture service")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "screen_capture"
        val channel = NotificationChannel(
            channelId,
            "Screen Capture",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.screen_capture_in_progress))
            .build()

        startForeground(1, notification)
        launchDefault {
            delay(200)
            ShareServiceHandler.setReady(true)
            _isStarted = true
            Timber.d("Screen capture service started")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null
}
