package com.example.web

import android.webkit.JavascriptInterface
import com.example.core.AppLogger
import com.example.core.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * JavaScript Interface bridge injected into Discord Web view as `window.ModuxNative`.
 * Enables seamless integration between Discord Web Settings and native client customization modules.
 */
class ModuxWebBridge(
    private val onOpenClientCategory: (String) -> Unit,
    private val onReloadRequested: () -> Unit
) {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    @JavascriptInterface
    fun openClientSettings(category: String = "general") {
        AppLogger.i("ModuxWebBridge", "Discord Web triggered Client Settings category: '$category'")
        mainScope.launch {
            onOpenClientCategory(category.lowercase().trim())
        }
    }

    @JavascriptInterface
    fun openCategory(category: String) {
        openClientSettings(category)
    }

    @JavascriptInterface
    fun reloadClient() {
        AppLogger.i("ModuxWebBridge", "Discord Web requested client reload.")
        mainScope.launch {
            onReloadRequested()
        }
    }

    @JavascriptInterface
    fun log(tag: String, message: String) {
        AppLogger.d("DiscordWeb:$tag", message)
    }

    @JavascriptInterface
    fun getClientVersion(): String = "1.0.0"

    @JavascriptInterface
    fun emitDiscordEvent(name: String, dataJson: String) {
        AppLogger.d("ModuxWebBridge", "Discord event: $name")
        EventBus.emit("DiscordWeb", name, mapOf("data" to dataJson))
    }
}
