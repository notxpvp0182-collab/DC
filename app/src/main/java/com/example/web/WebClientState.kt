package com.example.web

import android.webkit.PermissionRequest

data class WebClientState(
    val currentUrl: String = "https://discord.com/app",
    val title: String = "Discord",
    val isLoading: Boolean = true,
    val progress: Int = 0,
    val isError: Boolean = false,
    val errorCode: Int = 0,
    val errorDescription: String? = null,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val pendingPermissionRequest: PermissionRequest? = null
)
