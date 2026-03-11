package com.aegisnet.ui.firewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.ConnectionLogDao
import com.aegisnet.database.entity.ConnectionLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionLogViewModel @Inject constructor(
    private val logDao: ConnectionLogDao
) : ViewModel() {

    private val _logs = MutableStateFlow<List<ConnectionLog>>(emptyList())
    val logs: StateFlow<List<ConnectionLog>> = _logs

    fun loadLogs(appUid: Int) {
        viewModelScope.launch {
            // Load latest 100 logs
            logDao.getLogsForApp(appUid, 100).collect {
                _logs.value = it
            }
        }
    }
}
