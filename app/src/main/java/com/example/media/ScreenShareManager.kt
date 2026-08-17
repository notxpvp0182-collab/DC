package com.example.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.example.core.AppLogger
import com.example.core.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ScreenCaptureState {
    IDLE,
    REQUESTING_PERMISSION,
    ACTIVE,
    ERROR
}

data class ScreenShareInfo(
    val state: ScreenCaptureState = ScreenCaptureState.IDLE,
    val isSharing: Boolean = false,
    val resolution: String = "1080p (FHD)",
    val frameRate: Int = 30,
    val statusMessage: String = "Screen sharing is ready. Requires explicit Android system permission.",
    val webrtcNotice: String = "Captures Android display via official MediaProjection API. Direct WebRTC streaming to WebView is managed via native browser hooks."
)

class ScreenShareManager(private val context: Context) {

    private val mediaProjectionManager: MediaProjectionManager? =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager

    private var activeMediaProjection: MediaProjection? = null

    private val _info = MutableStateFlow(ScreenShareInfo())
    val info: StateFlow<ScreenShareInfo> = _info.asStateFlow()

    fun createScreenCaptureIntent(): Intent? {
        return mediaProjectionManager?.createScreenCaptureIntent()
    }

    fun onCapturePermissionResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                activeMediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
                _info.value = _info.value.copy(
                    state = ScreenCaptureState.ACTIVE,
                    isSharing = true,
                    statusMessage = "Screen capture session is active via MediaProjection."
                )
                AppLogger.i("ScreenShareManager", "MediaProjection session started successfully.")
                EventBus.emit("ScreenShareManager", "SCREEN_SHARE_STARTED", emptyMap())
            } catch (e: Exception) {
                AppLogger.e("ScreenShareManager", "Failed to obtain MediaProjection: ${e.message}")
                _info.value = _info.value.copy(
                    state = ScreenCaptureState.ERROR,
                    isSharing = false,
                    statusMessage = "Failed to start capture: ${e.message}"
                )
            }
        } else {
            _info.value = _info.value.copy(
                state = ScreenCaptureState.IDLE,
                isSharing = false,
                statusMessage = "Screen capture permission was denied by user."
            )
            AppLogger.w("ScreenShareManager", "User denied MediaProjection confirmation.")
        }
    }

    fun stopScreenSharing() {
        try {
            activeMediaProjection?.stop()
            activeMediaProjection = null
        } catch (e: Exception) {
            AppLogger.e("ScreenShareManager", "Error stopping MediaProjection: ${e.message}")
        }
        _info.value = _info.value.copy(
            state = ScreenCaptureState.IDLE,
            isSharing = false,
            statusMessage = "Screen sharing stopped."
        )
        AppLogger.i("ScreenShareManager", "Screen capture session terminated.")
        EventBus.emit("ScreenShareManager", "SCREEN_SHARE_STOPPED", emptyMap())
    }
}
