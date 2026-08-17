package com.example.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

object AppConfig {
    const val APP_NAME = "ModuX"
    const val APP_VERSION = "1.2.0-hobby"
    const val APP_VERSION_CODE = 120
    const val MIN_SUPPORTED_PLUGIN_VERSION = "1.0.0"
    const val GITHUB_REPO_URL = "https://github.com/example/modux"
    const val DEVELOPER_TAP_THRESHOLD = 7
}

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
) {
    fun format(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        return "[${sdf.format(Date(timestamp))}] [${level.name}] [$tag] $message"
    }
}

object AppLogger {
    private const val MAX_LOGS = 500
    private val _logs = CopyOnWriteArrayList<LogEntry>()
    private val _logFlow = MutableSharedFlow<LogEntry>(extraBufferCapacity = 64)
    val logFlow = _logFlow.asSharedFlow()

    fun log(level: LogLevel, tag: String, message: String) {
        // Redact any accidental tokens or sensitive patterns
        val sanitized = sanitize(message)
        val entry = LogEntry(level = level, tag = tag, message = sanitized)
        
        if (_logs.size >= MAX_LOGS) {
            _logs.removeAt(0)
        }
        _logs.add(entry)
        _logFlow.tryEmit(entry)
        
        when (level) {
            LogLevel.DEBUG -> android.util.Log.d(tag, sanitized)
            LogLevel.INFO -> android.util.Log.i(tag, sanitized)
            LogLevel.WARNING -> android.util.Log.w(tag, sanitized)
            LogLevel.ERROR -> android.util.Log.e(tag, sanitized)
        }
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARNING, tag, message)
    fun e(tag: String, message: String) = log(LogLevel.ERROR, tag, message)

    fun getLogs(): List<LogEntry> = _logs.toList()

    fun clearLogs() {
        _logs.clear()
    }

    fun exportLogs(): String {
        return _logs.joinToString("\n") { it.format() }
    }

    private fun sanitize(input: String): String {
        return input.replace(Regex("(?i)(password|token|secret|key)\\s*[:=]\\s*[^\\s]+"), "$1=[REDACTED]")
    }
}

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}
