package com.aegisnet.ui.firewall

import androidx.lifecycle.ViewModel
import com.aegisnet.database.dao.AppInfoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    appInfoDao: AppInfoDao
) : ViewModel() {
    val apps = appInfoDao.getAll()
}
