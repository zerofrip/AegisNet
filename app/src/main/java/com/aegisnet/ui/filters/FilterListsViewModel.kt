package com.aegisnet.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.entity.FilterList
import com.aegisnet.filter.FilterUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterListsViewModel @Inject constructor(
    private val filterListDao: FilterListDao,
    private val workManager: WorkManager
) : ViewModel() {

    val lists = filterListDao.getAll()

    fun addList(name: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = FilterList(
                name = name,
                url = url,
                isEnabled = true
            )
            filterListDao.insert(newList)
        }
    }

    fun toggleList(list: FilterList, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            filterListDao.insert(list.copy(isEnabled = isEnabled))
        }
    }

    fun deleteList(list: FilterList) {
        viewModelScope.launch(Dispatchers.IO) {
            filterListDao.delete(list)
        }
    }

    fun triggerUpdate(list: FilterList) {
        viewModelScope.launch(Dispatchers.IO) {
            // Reset lastUpdated so the worker will not skip it due to the interval check
            filterListDao.insert(list.copy(lastUpdated = 0))
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<FilterUpdateWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            "FilterUpdateWorker_manual_${list.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
