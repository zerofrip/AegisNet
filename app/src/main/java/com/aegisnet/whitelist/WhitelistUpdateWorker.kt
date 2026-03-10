package com.aegisnet.whitelist

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aegisnet.database.dao.WhitelistListDao
import com.aegisnet.database.dao.WhitelistRuleDao
import com.aegisnet.database.entity.WhitelistRule
import com.aegisnet.filter.FilterDownloader
import com.aegisnet.settings.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WhitelistUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val whitelistListDao: WhitelistListDao,
    private val whitelistRuleDao: WhitelistRuleDao,
    private val filterDownloader: FilterDownloader,
    private val settingsManager: SettingsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val currentTime = System.currentTimeMillis()
        return try {
            val lists = whitelistListDao.getAll().first()
            for (list in lists) {
                if (!list.isEnabled || list.url.isEmpty()) continue
                
                if (currentTime - list.lastUpdated < list.updateInterval) {
                   continue // Interval hasn't passed, skip
                }

                val content = filterDownloader.downloadList(list.url)
                if (content != null) {
                    val newRules = filterDownloader.parseRules(content).map { 
                        WhitelistRule(domain = it, isEnabled = true) 
                    }

                    newRules.forEach {
                        whitelistRuleDao.insert(it)
                    }
                    whitelistListDao.insert(list.copy(lastUpdated = currentTime))
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
