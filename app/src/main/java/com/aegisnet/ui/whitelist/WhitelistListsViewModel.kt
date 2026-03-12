package com.aegisnet.ui.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.WhitelistListDao
import com.aegisnet.database.entity.WhitelistList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhitelistListsViewModel @Inject constructor(
    private val whitelistListDao: WhitelistListDao
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
}
