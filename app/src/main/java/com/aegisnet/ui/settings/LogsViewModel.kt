package com.aegisnet.ui.settings

import androidx.lifecycle.ViewModel
import com.aegisnet.singbox.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val logManager: LogManager
) : ViewModel() {
    val logs = logManager.logs

    fun clearLogs() {
        logManager.clearLogs()
    }
}
