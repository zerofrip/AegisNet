package com.aegisnet.firewall

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    var bypassVpn: Boolean = false,
    var blockInternet: Boolean = false // If true, routing engine will drop connections from this app
)

@Singleton
class FirewallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager
    
    // Memory cache of app rules
    private val appRules = mutableMapOf<String, AppInfo>()

    fun getInstalledApps(): List<AppInfo> {
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        return packages.mapNotNull { pi ->
            val ai = pi.applicationInfo ?: return@mapNotNull null
            val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = packageManager.getApplicationLabel(ai).toString()
            
            appRules.getOrPut(pi.packageName) {
                AppInfo(
                    packageName = pi.packageName,
                    appName = appName,
                    isSystemApp = isSystem
                )
            }
        }.sortedBy { it.appName.lowercase() }
    }

    fun updateAppRule(packageName: String, bypassVpn: Boolean, blockInternet: Boolean) {
        val appInfo = appRules[packageName]
        if (appInfo != null) {
            appInfo.bypassVpn = bypassVpn
            appInfo.blockInternet = blockInternet
        } else {
            // Need to retrieve appName if not queried yet
            appRules[packageName] = AppInfo(packageName, packageName, false, bypassVpn, blockInternet)
        }
        // TODO: Persist rules to Database
    }

    fun getBypassedApps(): List<String> {
        return appRules.values.filter { it.bypassVpn }.map { it.packageName }
    }

    fun getBlockedApps(): List<String> {
        return appRules.values.filter { it.blockInternet }.map { it.packageName }
    }
}
