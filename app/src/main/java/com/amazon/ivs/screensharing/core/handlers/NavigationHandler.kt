package com.amazon.ivs.screensharing.core.handlers

import com.amazon.ivs.screensharing.core.common.launchDefault
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

object NavigationHandler {
    private val _destination = MutableStateFlow<Destination>(Destination.Token)
    private val _isLoading = MutableStateFlow(false)

    val destination = _destination.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    fun setLoading(isLoading: Boolean) {
        _isLoading.update { isLoading }
    }

    fun goTo(destination: Destination) {
        if (_destination.value == destination) return

        Timber.d("Going to: $destination")
        _destination.update { destination }
    }

    fun goBack() = launchDefault {
        if (_destination.value == Destination.Share) {
            Timber.d("Going back to Watch")
            _destination.update { Destination.Watch }
            return@launchDefault
        }
        if (_destination.value == Destination.Watch) {
            Timber.d("Going back to Token")
            StageHandler.leaveStage()
            _destination.update { Destination.Token }
            return@launchDefault
        }

        _destination.update { Destination.Finish }
        delay(200)
        _destination.update { Destination.Token }
    }
}

sealed interface Destination {
    object Finish : Destination
    object Token : Destination
    object Watch : Destination
    object Share : Destination
}
