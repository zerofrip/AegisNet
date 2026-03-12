package com.aegisnet.filter

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.dao.UserRuleDao
import com.aegisnet.database.entity.UserRule
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterManager @Inject constructor(
    private val filterListDao: FilterListDao,
    private val workManager: WorkManager
) {

    // Retrieve active parsed domains for SingBox config
    suspend fun getActiveBlockDomains(): List<String> {
        return emptyList()
    }

    fun scheduleAutoUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val updateWorkReq = PeriodicWorkRequestBuilder<FilterUpdateWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniquePeriodicWork(
            "FilterUpdateWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWorkReq
        )
    }
}
