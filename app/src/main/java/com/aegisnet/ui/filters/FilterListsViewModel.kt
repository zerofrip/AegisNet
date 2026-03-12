package com.aegisnet.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.entity.FilterList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterListsViewModel @Inject constructor(
    private val filterListDao: FilterListDao
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
}
