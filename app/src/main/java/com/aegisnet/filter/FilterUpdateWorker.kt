package com.aegisnet.filter

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.dao.UserRuleDao
import com.aegisnet.database.entity.UserRule
import com.aegisnet.settings.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class FilterUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val filterListDao: FilterListDao,
    private val userRuleDao: UserRuleDao,
    private val filterDownloader: FilterDownloader,
    private val settingsManager: SettingsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val currentTime = System.currentTimeMillis()
        return try {
            val filterLists = filterListDao.getAll().first()
            
            for (list in filterLists) {
                // Check if list is enabled and interval has elapsed
                if (!list.isEnabled || list.url.isEmpty()) continue
                
                if (currentTime - list.lastUpdated < list.updateInterval) {
                   continue // Interval hasn't passed, skip
                }

                val content = filterDownloader.downloadList(list.url)
                if (content != null) {
                    val parsedRules = filterDownloader.parseRules(content)
                    // Store rules locally ...
                    parsedRules.forEach {
                        userRuleDao.insert(UserRule(rule = it, isEnabled = true))
                    }
                    filterListDao.insert(list.copy(lastUpdated = currentTime))
                } 
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Keep previous version if download fails, and retry later
            Result.retry()
        }
    }
}
