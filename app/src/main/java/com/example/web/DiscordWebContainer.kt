package com.example.web

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.core.AppLogger
import com.example.theme.ThemeDefinition

private const val DISCORD_APP_URL = "https://discord.com/app"
private const val USER_AGENT_OVERRIDE =
    "Mozilla/5.0 (Linux; Android 14; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0 Chrome/128.0.0.0 Mobile Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DiscordWebContainer(
    theme: ThemeDefinition,
    accentOverride: String?,
    isHardwareAccelerationEnabled: Boolean,
    isCompactMode: Boolean,
    onOpenClientCategory: (String) -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
    onPermissionRequested: (PermissionRequest) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var webViewState by remember { mutableStateOf(WebClientState()) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Inject CSS & Scripts dynamically whenever Theme, Accent, or Compact Mode changes
    LaunchedEffect(theme, accentOverride, isCompactMode, webViewState.isLoading) {
        if (!webViewState.isLoading && webViewInstance != null) {
            val css = generateCustomCss(theme, accentOverride, isCompactMode)
            injectCss(webViewInstance, css)
            injectDiscordSettingsBridge(webViewInstance)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Configure Cookie Manager for persistent session
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Configure Hardware Acceleration
                    // Use LAYER_TYPE_NONE for normal hardware acceleration to avoid forcing offscreen rendernode allocations
                    if (isHardwareAccelerationEnabled) {
                        setLayerType(WebView.LAYER_TYPE_NONE, null)
                    } else {
                        setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
                    }

                    // WebSettings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                        mediaPlaybackRequiresUserGesture = false
                        userAgentString = USER_AGENT_OVERRIDE
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        allowContentAccess = true
                        allowFileAccess = false
                    }

                    // Register ModuX JavaScript Bridge
                    val bridge = ModuxWebBridge(
                        onOpenClientCategory = onOpenClientCategory,
                        onReloadRequested = { reload() }
                    )
                    addJavascriptInterface(bridge, "ModuxNative")
                    addJavascriptInterface(bridge, "VendroidNative")

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            webViewState = webViewState.copy(
                                progress = newProgress,
                                isLoading = newProgress < 100
                            )
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            webViewState = webViewState.copy(title = title ?: "Discord")
                        }

                        override fun onPermissionRequest(request: PermissionRequest?) {
                            if (request == null) return
                            AppLogger.i("DiscordWebView", "Permission requested by Web: ${request.resources.joinToString()}")
                            
                            val hasAudio = request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                            val hasVideo = request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)

                            val audioGranted = !hasAudio || ContextCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            val videoGranted = !hasVideo || ContextCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (audioGranted && videoGranted) {
                                request.grant(request.resources)
                                AppLogger.i("DiscordWebView", "Granted Web permissions immediately.")
                            } else {
                                onPermissionRequested(request)
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            webViewState = webViewState.copy(
                                isLoading = true,
                                isError = false,
                                currentUrl = url ?: DISCORD_APP_URL,
                                canGoBack = view?.canGoBack() == true,
                                canGoForward = view?.canGoForward() == true
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webViewState = webViewState.copy(
                                isLoading = false,
                                currentUrl = url ?: DISCORD_APP_URL,
                                canGoBack = view?.canGoBack() == true,
                                canGoForward = view?.canGoForward() == true
                            )
                            // Inject custom Theme & Compact styling
                            val css = generateCustomCss(theme, accentOverride, isCompactMode)
                            injectCss(view, css)
                            // Inject Discord Settings Bridge for Vencord/Vendroid style client category
                            injectDiscordSettingsBridge(view)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    error?.description?.toString()
                                } else "Connection error"
                                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    error?.errorCode ?: -1
                                } else -1

                                webViewState = webViewState.copy(
                                    isError = true,
                                    errorCode = code,
                                    errorDescription = desc
                                )
                                AppLogger.w("DiscordWebView", "Main frame load error: $desc")
                            }
                        }
                    }

                    loadUrl(DISCORD_APP_URL)
                    webViewInstance = this
                    onWebViewCreated(this)
                }
            },
            update = { view ->
                webViewInstance = view
            }
        )

        // Loading Bar at top
        if (webViewState.isLoading) {
            LinearProgressIndicator(
                progress = { webViewState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }

        // Friendly Discord Error / Offline Screen
        if (webViewState.isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme.toBackgroundColor().copy(alpha = 0.96f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Discord could not be loaded",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.toTextPrimaryColor(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = webViewState.errorDescription ?: "Please check your network connection and try again.",
                        fontSize = 14.sp,
                        color = theme.toTextSecondaryColor(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            webViewState = webViewState.copy(isError = false, isLoading = true)
                            webViewInstance?.reload()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Retry Connection", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}

private fun generateCustomCss(
    theme: ThemeDefinition,
    accentOverride: String?,
    isCompactMode: Boolean
): String {
    val accent = accentOverride ?: theme.accentHex
    val compactSpacing = if (isCompactMode) {
        """
        [class*="message_"], [class*="listItem_"] {
            padding-top: 2px !important;
            padding-bottom: 2px !important;
        }
        [class*="container_"] {
            gap: 4px !important;
        }
        """.trimIndent()
    } else ""

    return """
        :root {
            --brand-experiment: $accent !important;
            --brand-500: $accent !important;
            --brand-560: $accent !important;
            --brand-600: $accent !important;
            --background-primary: ${theme.backgroundHex} !important;
            --background-secondary: ${theme.surfaceHex} !important;
            --background-secondary-alt: ${theme.cardHex} !important;
            --background-tertiary: ${theme.surfaceHex} !important;
            --background-floating: ${theme.cardHex} !important;
            --header-primary: ${theme.textPrimaryHex} !important;
            --header-secondary: ${theme.textSecondaryHex} !important;
            --text-normal: ${theme.textPrimaryHex} !important;
            --text-muted: ${theme.textSecondaryHex} !important;
            --interactive-normal: ${theme.textSecondaryHex} !important;
            --interactive-hover: ${theme.textPrimaryHex} !important;
            --interactive-active: #ffffff !important;
            --border-subtle: ${theme.borderHex} !important;
        }
        
        /* ModuX Custom Scrollbars */
        ::-webkit-scrollbar {
            width: 6px !important;
            height: 6px !important;
        }
        ::-webkit-scrollbar-thumb {
            background: $accent !important;
            border-radius: 4px !important;
        }
        ::-webkit-scrollbar-track {
            background: transparent !important;
        }

        /* ModuX Injected Settings UI Styling */
        .modux-settings-header {
            padding: 10px 12px 6px 12px !important;
            font-size: 11px !important;
            font-weight: 800 !important;
            letter-spacing: 0.04em !important;
            text-transform: uppercase !important;
            color: #949ba4 !important;
            display: flex !important;
            align-items: center !important;
            justify-content: space-between !important;
        }
        .modux-settings-header-badge {
            background: $accent !important;
            color: #ffffff !important;
            font-size: 9px !important;
            font-weight: 700 !important;
            padding: 2px 6px !important;
            border-radius: 8px !important;
            text-transform: uppercase !important;
        }
        .modux-settings-item {
            display: flex !important;
            align-items: center !important;
            padding: 8px 12px !important;
            margin: 2px 8px !important;
            border-radius: 6px !important;
            cursor: pointer !important;
            font-size: 14px !important;
            font-weight: 500 !important;
            color: #dbdee1 !important;
            transition: background 0.15s ease, color 0.15s ease !important;
            user-select: none !important;
        }
        .modux-settings-item:hover, .modux-settings-item:active {
            background: rgba(255, 255, 255, 0.07) !important;
            color: #ffffff !important;
        }
        .modux-settings-icon {
            margin-right: 10px !important;
            font-size: 16px !important;
            width: 20px !important;
            text-align: center !important;
        }
        .modux-separator {
            height: 1px !important;
            background: rgba(255, 255, 255, 0.08) !important;
            margin: 8px 12px !important;
        }

        $compactSpacing
    """.trimIndent().replace("\n", " ")
}

private fun injectCss(webView: WebView?, css: String) {
    if (webView == null) return
    val encoded = android.util.Base64.encodeToString(css.toByteArray(), android.util.Base64.NO_WRAP)
    val script = """
        (function() {
            var el = document.getElementById('modux-custom-css');
            if (!el) {
                el = document.createElement('style');
                el.id = 'modux-custom-css';
                el.type = 'text/css';
                document.head.appendChild(el);
            }
            el.innerHTML = window.atob('$encoded');
        })();
    """.trimIndent()
    webView.evaluateJavascript(script, null)
}

/**
 * Injects the Vencord / Vendroid style settings observer script into Discord Web.
 * Watches for Discord's settings sidebar rendering and inserts the "CLIENT" / "VENCORD" custom categories.
 */
private fun injectDiscordSettingsBridge(webView: WebView?) {
    if (webView == null) return

    val script = """
        (function() {
            if (window.__moduxSettingsBridgeInjected) return;
            window.__moduxSettingsBridgeInjected = true;

            const CLIENT_ITEMS = [
                { id: 'general', title: 'General', icon: '⚙️' },
                { id: 'plugins', title: 'Plugins', icon: '🧩' },
                { id: 'themes', title: 'Themes', icon: '🎨' },
                { id: 'background', title: 'Background', icon: '🖼️' },
                { id: 'appearance', title: 'Appearance', icon: '✨' },
                { id: 'messages', title: 'Message Tools', icon: '💬' },
                { id: 'notifications', title: 'Notifications', icon: '🔔' },
                { id: 'voice', title: 'Voice', icon: '🎙️' },
                { id: 'video', title: 'Video', icon: '📹' },
                { id: 'screenshare', title: 'Screen Share', icon: '🖥️' },
                { id: 'permissions', title: 'Permissions', icon: '🛡️' },
                { id: 'keybinds', title: 'Keybinds', icon: '⌨️' },
                { id: 'performance', title: 'Performance', icon: '⚡' },
                { id: 'developer', title: 'Developer', icon: '🛠️' },
                { id: 'backup', title: 'Backup & Restore', icon: '💾' },
                { id: 'about', title: 'About', icon: 'ℹ️' }
            ];

            function injectClientSection(sidebar) {
                if (!sidebar || sidebar.querySelector('#modux-settings-section')) return;

                const container = document.createElement('div');
                container.id = 'modux-settings-section';

                const separator = document.createElement('div');
                separator.className = 'modux-separator';
                container.appendChild(separator);

                const header = document.createElement('div');
                header.className = 'modux-settings-header';
                header.innerHTML = '<span>CLIENT</span><span class="modux-settings-header-badge">ModuX</span>';
                container.appendChild(header);

                CLIENT_ITEMS.forEach(item => {
                    const row = document.createElement('div');
                    row.className = 'modux-settings-item';
                    row.innerHTML = '<span class="modux-settings-icon">' + item.icon + '</span><span>' + item.title + '</span>';
                    row.onclick = function(e) {
                        e.stopPropagation();
                        e.preventDefault();
                        if (window.ModuxNative && window.ModuxNative.openClientSettings) {
                            window.ModuxNative.openClientSettings(item.id);
                        } else if (window.VendroidNative && window.VendroidNative.openClientSettings) {
                            window.VendroidNative.openClientSettings(item.id);
                        }
                    };
                    container.appendChild(row);
                });

                // Find good insert spot before Log Out or at end of App Settings
                const logOutItem = Array.from(sidebar.children).find(el => el.textContent && el.textContent.toLowerCase().includes('log out'));
                if (logOutItem) {
                    sidebar.insertBefore(container, logOutItem);
                } else {
                    sidebar.appendChild(container);
                }
            }

            // Hook user profile area gear icon to also give access
            function hookUserProfilePanel() {
                const userPanel = document.querySelector('section[class*="panels_"], div[class*="container_"] > div[class*="avatarWrapper_"]');
                if (userPanel && !document.getElementById('modux-panel-btn')) {
                    // Panel is present
                }
            }

            // Observer to detect Discord User Settings modal / sidebar
            const observer = new MutationObserver(function(mutations) {
                // Check for sidebar in settings
                const sidebars = document.querySelectorAll('[class*="standardSidebarView_"] [class*="sidebar_"], [role="tablist"], nav[class*="sidebar_"], div[class*="side_"]');
                sidebars.forEach(sidebar => {
                    if (sidebar && sidebar.children.length > 2) {
                        injectClientSection(sidebar);
                    }
                });
            });

            observer.observe(document.body, { childList: true, subtree: true });

            // Initial check
            setTimeout(() => {
                const sidebars = document.querySelectorAll('[class*="standardSidebarView_"] [class*="sidebar_"], [role="tablist"], nav[class*="sidebar_"]');
                sidebars.forEach(sidebar => injectClientSection(sidebar));
            }, 1000);
        })();
    """.trimIndent()

    webView.evaluateJavascript(script, null)
}
