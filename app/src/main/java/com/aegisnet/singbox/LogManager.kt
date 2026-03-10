package com.aegisnet.singbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogManager @Inject constructor() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val maxLogLines = 500

    fun addLog(message: String) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(message)
        if (currentLogs.size > maxLogLines) {
            currentLogs.removeAt(0)
        }
        _logs.value = currentLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
