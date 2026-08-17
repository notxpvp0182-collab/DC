package com.example.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ModuxEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val source: String,
    val eventType: String,
    val payload: Map<String, String> = emptyMap()
)

object EventBus {
    private val _events = MutableSharedFlow<ModuxEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<ModuxEvent> = _events.asSharedFlow()

    private val eventHistory = java.util.concurrent.CopyOnWriteArrayList<ModuxEvent>()

    fun emit(source: String, eventType: String, payload: Map<String, String> = emptyMap()) {
        val event = ModuxEvent(source = source, eventType = eventType, payload = payload)
        if (eventHistory.size >= 200) {
            eventHistory.removeAt(0)
        }
        eventHistory.add(event)
        _events.tryEmit(event)
        AppLogger.d("EventBus", "[$source] -> $eventType: $payload")
    }

    fun getHistory(): List<ModuxEvent> = eventHistory.toList()

    fun clear() {
        eventHistory.clear()
    }
}
