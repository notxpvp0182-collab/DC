package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED
}

data class PermissionCenterState(
    val microphoneStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val cameraStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val notificationStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val mediaPhotosStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val screenCaptureStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED
)

class PermissionManager(private val context: Context) {

    private val _state = MutableStateFlow(PermissionCenterState())
    val state: StateFlow<PermissionCenterState> = _state.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val mic = checkPermission(Manifest.permission.RECORD_AUDIO)
        val cam = checkPermission(Manifest.permission.CAMERA)
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PermissionStatus.GRANTED
        }
        val media = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            PermissionStatus.GRANTED
        }

        _state.value = _state.value.copy(
            microphoneStatus = mic,
            cameraStatus = cam,
            notificationStatus = notif,
            mediaPhotosStatus = media
        )
    }

    fun setScreenCaptureStatus(status: PermissionStatus) {
        _state.value = _state.value.copy(screenCaptureStatus = status)
    }

    private fun checkPermission(permission: String): PermissionStatus {
        return if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
