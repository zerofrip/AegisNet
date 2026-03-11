package com.aegisnet.firewall

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.aegisnet.database.dao.AppInfoDao
import com.aegisnet.database.entity.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirewallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appInfoDao: AppInfoDao
) {
    private val packageManager: PackageManager = context.packageManager
    
    init {
        // Sync apps to database on startup
        CoroutineScope(Dispatchers.IO).launch {
            syncInstalledApps()
        }
    }

    private suspend fun syncInstalledApps() {
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val appInfos = packages.mapNotNull { pi ->
            val ai = pi.applicationInfo ?: return@mapNotNull null
            val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = packageManager.getApplicationLabel(ai).toString()
            
            val uid = packageManager.getPackageUid(pi.packageName, 0)
            
            AppInfo(
                uid = uid,
                packageName = pi.packageName,
                appName = appName,
                isSystemApp = isSystem
            )
        }.distinctBy { it.uid } // Ensure unique UIDs
        
        appInfoDao.insertAll(appInfos)
        appInfoDao.deleteRemovedApps(appInfos.map { it.uid })
    }

    fun getInstalledAppsFlow(): Flow<List<AppInfo>> {
        return appInfoDao.getAll()
    }
}
