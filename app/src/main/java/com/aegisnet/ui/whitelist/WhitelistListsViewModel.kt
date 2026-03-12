package com.aegisnet.ui.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aegisnet.database.dao.WhitelistListDao
import com.aegisnet.database.entity.WhitelistList
import com.aegisnet.whitelist.WhitelistUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhitelistListsViewModel @Inject constructor(
    private val whitelistListDao: WhitelistListDao,
    private val workManager: WorkManager
) : ViewModel() {

    val lists = whitelistListDao.getAll()

    fun addList(name: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = WhitelistList(
                name = name,
                url = url,
                isEnabled = true
            )
            whitelistListDao.insert(newList)
        }
    }

    fun toggleList(list: WhitelistList, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            whitelistListDao.insert(list.copy(isEnabled = isEnabled))
        }
    }

    fun deleteList(list: WhitelistList) {
        viewModelScope.launch(Dispatchers.IO) {
            whitelistListDao.delete(list)
        }
    }

    fun triggerUpdate(list: WhitelistList) {
        viewModelScope.launch(Dispatchers.IO) {
            // Reset lastUpdated so the worker will not skip it due to the interval check
            whitelistListDao.insert(list.copy(lastUpdated = 0))
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<WhitelistUpdateWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            "WhitelistUpdateWorker_manual_${list.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
